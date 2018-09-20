/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

/**
 * Thrown when there is some misconfiguration of the TLS authentication for the SOAP client
 */
public class ClientCredentialsConfigurationException extends Exception {
    public ClientCredentialsConfigurationException() {
        super();
    }

    public ClientCredentialsConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientCredentialsConfigurationException(String message) {
        super(message);
    }

    public ClientCredentialsConfigurationException(Throwable cause) {
        super(cause);
    }
}