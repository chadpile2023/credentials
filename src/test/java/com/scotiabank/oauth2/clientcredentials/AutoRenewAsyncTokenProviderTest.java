/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StopWatch;

import java.time.Instant;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AutoRenewAsyncTokenProviderTest {

    AutoRenewAsyncResourceProvider<String> sut;

    @Mock
    ExpiringResourceProvider<String> tokenProvider;

    @Mock
    ResourceAutoRenewalProperties properties;

    @Mock
    CurrentTimeGenerator currentTimeGenerator;

    @Mock
    ExpiringResource<String> resource;

    Instant currentTime = Instant.parse("2018-04-11T12:01:00Z");

    @Before
    public void init() {
        when(properties.getPreemptiveExpiryMs()).thenReturn(300);
        when(properties.getRenewalPeriodMs()).thenReturn(200);

        when(currentTimeGenerator.getCurrentTime()).thenReturn(currentTime);

        when(resource.getExpiration()).thenReturn(currentTime.plusMillis(1000));
    }

    @Test
    public void getToken_whenRequestComesBeforeToken_willBlockThreadUntilTokenAvailable() throws InterruptedException, TimeoutException, BrokenBarrierException {
        CyclicBarrier barrier = new CyclicBarrier(2);
        when(tokenProvider.getResource()).thenAnswer((InvocationOnMock inv) -> {
            barrier.await(10, TimeUnit.SECONDS);
            log.info("sleeping");
            Thread.sleep(100);
            log.info("awake");
            return resource;
        });

        sut = new AutoRenewAsyncResourceProvider<>(tokenProvider, properties, currentTimeGenerator);
        barrier.await(10, TimeUnit.SECONDS);

        StopWatch s = new StopWatch();
        s.start();
        log.info("getting resource");
        ExpiringResource<String> result = sut.getResource();
        log.info("retrieved");
        s.stop();
        assertThat(s.getTotalTimeMillis()).isGreaterThan(75);
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(resource);
    }

    @Test
    public void getResource_whenRequestThrowsException_willBlockThreadUntilTokenAvailable() throws InterruptedException, TimeoutException, BrokenBarrierException {
        CyclicBarrier barrier = new CyclicBarrier(2);
        when(tokenProvider.getResource())
            .thenAnswer((InvocationOnMock inv) -> {
                barrier.await(10, TimeUnit.SECONDS);
                throw new RuntimeException();
            })
            .thenReturn(resource);

        sut = new AutoRenewAsyncResourceProvider<>(tokenProvider, properties, currentTimeGenerator);
        barrier.await(10, TimeUnit.SECONDS);

        // give it a chance to finish
        Thread.sleep(50);

        ExpiringResource<String> result = sut.getResource();

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(resource);
    }

    @Test
    public void getResource_whenTokenRenewalPeriodHasPassed_willReturnRenewedToken() throws InterruptedException {
        ExpiringResource<String> token2 = mock(ExpiringResource.class);
        when(token2.getExpiration()).thenReturn(currentTime.plusMillis(1000));
        when(tokenProvider.getResource()).thenReturn(resource, token2);

        sut = new AutoRenewAsyncResourceProvider<>(tokenProvider, properties, currentTimeGenerator);

        ExpiringResource<String> result = sut.getResource();
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(resource);

        Thread.sleep(240);
        result = sut.getResource();

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(token2);
    }

    @Test
    public void getResource_whenTokenHasNotRenewedAndPreemptiveExpiryElapses_willReturnNullAfterAttemptingRequest() throws InterruptedException, BrokenBarrierException, TimeoutException {
        when(tokenProvider.getResource()).thenReturn(resource, (ExpiringResource<String>) null);

        sut = new AutoRenewAsyncResourceProvider<>(tokenProvider, properties, currentTimeGenerator);

        ExpiringResource<String> result = sut.getResource();
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(resource);

        Thread.sleep(240);

        result = sut.getResource();
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(resource);

        Thread.sleep(700);
        result = sut.getResource();
        assertThat(result).isNull();
    }

    @Test
    public void getResource_whenTokenHasNotRenewedAndPreemptiveExpiryElapsesButWorksAgain_willReturnNewTokenAfterAttemptingRequest() throws InterruptedException, BrokenBarrierException, TimeoutException {
        CyclicBarrier barrier = new CyclicBarrier(2);
        when(tokenProvider.getResource()).thenReturn(resource, null, null, null).thenAnswer((InvocationOnMock inv) -> {
            barrier.await(10, TimeUnit.SECONDS);
            return resource;
        });

        sut = new AutoRenewAsyncResourceProvider<>(tokenProvider, properties, currentTimeGenerator);

        ExpiringResource<String> result = sut.getResource();
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(resource);

        Thread.sleep(240);
        result = sut.getResource();
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(resource);

        Thread.sleep(600);
        assertThat(ReflectionTestUtils.getField(sut, "currentResource")).isNull();

        barrier.await(10, TimeUnit.SECONDS);
        result = sut.getResource();
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(resource);
    }
}
