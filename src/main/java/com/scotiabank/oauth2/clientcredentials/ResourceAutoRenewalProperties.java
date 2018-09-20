/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth2.clientcredentials")
public class ResourceAutoRenewalProperties {
    public ResourceAutoRenewalProperties() {
        // by default, renew every minute
        renewalPeriodMs = (int)Duration.of(1, ChronoUnit.MINUTES).toMillis();

        // by default, don't use a token in the last minute before expiry
        preemptiveExpiryMs = (int)Duration.of(1, ChronoUnit.MINUTES).toMillis();
    }

    private int renewalPeriodMs;
    private int preemptiveExpiryMs;
}
