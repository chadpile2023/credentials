/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

public class ClientCredentialsTokenRequestFailedException extends RuntimeException {
    public ClientCredentialsTokenRequestFailedException() {
        super();
    }

    public ClientCredentialsTokenRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientCredentialsTokenRequestFailedException(String message) {
        super(message);
    }

    public ClientCredentialsTokenRequestFailedException(Throwable cause) {
        super(cause);
    }
}
