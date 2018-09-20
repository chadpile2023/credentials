/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth2.clientcredentials")
public class ClientCredentialsAssertionGenerationProperties {
    public ClientCredentialsAssertionGenerationProperties() {
        assertionTokenExpiryTimeMs = (int)Duration.of(60, ChronoUnit.MINUTES).toMillis();
    }

    @Size(min = 1, message = "clientId must be populated with the OAuth2 client id")
    private String clientId;

    @Size(min = 1, message = "tokenUrl must be populated with the OAuth2 token url")
    private String tokenUrl;

    @Min(value = 1, message = "assertionTokenExpiryTimeMs must be a positive, non-zero value")
    private int assertionTokenExpiryTimeMs;
}
