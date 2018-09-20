/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.request;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientCredentialsAssertionGenerationPropertiesTest {

    @Test
    public void constructor_always_setsDefaultAssertionTokenExpiryTime() {
        assertThat(new ClientCredentialsAssertionGenerationProperties().getAssertionTokenExpiryTimeMs())
            .isEqualTo(3600000);
    }
}
