/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class AutoRenewAsyncResourceProvider<ResourceType> implements ExpiringResourceProvider<ResourceType> {

    private ScheduledFuture renewalProcess;
    private ScheduledFuture decommissionProcess;

    private final ResourceAutoRenewalProperties management;

    private final ExpiringResourceProvider<ResourceType> providerDelegate;
    private final CurrentTimeGenerator currentTimeGenerator;

    private ExpiringResource<ResourceType> currentResource;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // barrier used for initialization - will eventually be set to null to avoid re-initialization
    private CountDownLatch initBarrier = new CountDownLatch(1);

    public AutoRenewAsyncResourceProvider(
            ExpiringResourceProvider<ResourceType> providerDelegate,
            ResourceAutoRenewalProperties management,
            CurrentTimeGenerator currentTimeGenerator) {
        this.providerDelegate = providerDelegate;
        this.management = management;
        this.currentTimeGenerator = currentTimeGenerator;

        log.info("Auto renewal initializing");

        init(0);
        synchronizeInit();
    }

    private boolean synchronizeInit() {
        try {
            log.info("Waiting on auto renewal process to start");
            return initBarrier.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted exception", e);
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private void init(int delay) {
        if (renewalProcess != null) {
            log.info("Stopping auto renewal process");
            renewalProcess.cancel(true);
            renewalProcess = null;
        }

        log.info("Scheduling renewal process");
        renewalProcess = scheduler.scheduleAtFixedRate(this::renewResource, delay, management.getRenewalPeriodMs(), TimeUnit.MILLISECONDS);
    }

    private void decommissionResource() {
        log.info("Decommissioning resource");
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        try {
            if (writeLock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    currentResource = null;

                    init(management.getRenewalPeriodMs());
                    renewResource();
                } finally {
                    writeLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("Interrupted decommission", e);
            Thread.currentThread().interrupt();
        }
    }

    private void updateResource(ExpiringResource<ResourceType> newResource) throws InterruptedException {
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        if (writeLock.tryLock(10, TimeUnit.SECONDS)) {

            try {
                long timeToExpiryMs = Duration.between(currentTimeGenerator.getCurrentTime(), newResource.getExpiration()).toMillis();
                long timeToDecommissionMs = timeToExpiryMs - management.getPreemptiveExpiryMs();

                if (timeToDecommissionMs > 0) {
                    if (decommissionProcess != null) {
                        log.info("Canceling scheduled decommission process");
                        decommissionProcess.cancel(true);
                        decommissionProcess = null;
                    }

                    currentResource = newResource;

                    log.info("Scheduling resource decommission process");
                    // schedule decommission
                    decommissionProcess = scheduler.schedule(this::decommissionResource, timeToDecommissionMs, TimeUnit.MILLISECONDS);
                } else {
                    log.info("Resource already expired or will expire too soon to use. Ignoring resource");
                }

            } finally {
                writeLock.unlock();
            }
        }
    }

    private void tryToRenewResource() throws InterruptedException {
        log.info("Resource renewal process started");
        initBarrier.countDown();

        log.info("Retrieving resource");
        ExpiringResource<ResourceType> newResource = providerDelegate.getResource();

        if (newResource != null) {
            log.info("Resource retrieved - updating current resource");
            updateResource(newResource);
        } else {
            log.info("Resource not retrieved");
        }
    }

    private void tryToRenewResourceWithWriteLock() throws InterruptedException {
        log.info("Pre-emptively locking write lock");
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        if (writeLock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                tryToRenewResource();
            } finally {
                writeLock.unlock();
            }
        }
    }

    private void renewResource() {
        try {
            // if the resource is not initialized then try to renew while holding the lock to prevent
            // readers from reading the uninitialized value
            if (currentResource == null) {
                tryToRenewResourceWithWriteLock();
            } else {
                tryToRenewResource();
            }

        } catch (InterruptedException e) {
            log.error("Interrupted exception", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Exception ignored while retrieving resource", e);
        }
    }

    private ExpiringResource<ResourceType> trySchedulingResourceRetrievalImmediately() throws InterruptedException {
        log.info("Attempting to schedule resource renewal immediately");
        ExpiringResource<ResourceType> updatedResource = null;
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        if (writeLock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                updatedResource = this.currentResource;

                // double check now that you have the write lock
                if (updatedResource == null && initBarrier.getCount() == 0) {
                    // reset the initialization synchronization, then initialize
                    initBarrier = new CountDownLatch(1);
                    init(0);
                }
            } finally {
                writeLock.unlock();
            }

            // try to wait for the updated resource
            if (updatedResource == null) {
                synchronizeInit();
            }
        }
        return updatedResource;
    }

    @Override
    public ExpiringResource<ResourceType> getResource() {
        ExpiringResource<ResourceType> resource = null;
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        try {
            // if you're able to read it immediately it's a special case since you may need to try to get the resource immediately
            if (readLock.tryLock()) {
                resource = this.currentResource;
                readLock.unlock();

                if (resource == null) {
                    resource = trySchedulingResourceRetrievalImmediately();
                }

                // resource was updated with valid value
                if (resource != null) {
                    return resource;
                }
            }

            if (readLock.tryLock(10, TimeUnit.SECONDS)) {
                resource = this.currentResource;
                readLock.unlock();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted exception", e);
            Thread.currentThread().interrupt();
        }
        return resource;
    }
}
