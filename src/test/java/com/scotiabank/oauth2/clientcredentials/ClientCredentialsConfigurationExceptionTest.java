/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientCredentialsConfigurationExceptionTest {
    private final static String testMessage = "some test message";
    private final static String innerExceptionMessage = "other test message";
    private final static String expectedConstructedMessage = "java.lang.Exception: other test message";

    @Test
    public void defaultConstructorInitializes() {
        ClientCredentialsConfigurationException sut = new ClientCredentialsConfigurationException();

        assertThat(sut.getMessage()).isNull();
        assertThat(sut.getCause()).isNull();
    }

    @Test
    public void messageConstructorInitializes() {
        ClientCredentialsConfigurationException sut = new ClientCredentialsConfigurationException(testMessage);

        assertThat(sut.getMessage()).isEqualTo(testMessage);
        assertThat(sut.getCause()).isNull();
    }

    @Test
    public void causeConstructorInitializes() {
        Exception cause = new Exception(innerExceptionMessage);

        ClientCredentialsConfigurationException sut = new ClientCredentialsConfigurationException(cause);

        assertThat(sut.getMessage()).isEqualTo(expectedConstructedMessage);
        assertThat(sut.getCause()).isSameAs(cause);
    }

    @Test
    public void messageAndCauseConstructorInitializes() {
        Exception cause = new Exception(innerExceptionMessage);

        ClientCredentialsConfigurationException sut = new ClientCredentialsConfigurationException(testMessage, cause);

        assertThat(sut.getMessage()).isEqualTo(testMessage);
        assertThat(sut.getCause()).isSameAs(cause);
    }
}
