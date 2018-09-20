/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Size;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth2.clientcredentials")
public class ClientCredentialsSignatureProperties {
    @Size(min = 1, message = "keyStoreBase64 must be populated with a base64 encoded keystore")
    private String keyStoreBase64;

    @Size(min = 1, message = "keyStorePasswordBase64 must be populated with the keystore's password base64 encoded")
    private String keyStorePasswordBase64;

    @Size(min = 1, message = "keyStoreFormat must be populated with the keystore's format")
    private String keyStoreFormat;

    @Size(min = 1, message = "keyName must be populated with the client credentials key name")
    private String keyName;

    @Size(min = 1, message = "keyPasswordBase64 must be populated with the client credential key's password base64 encoded")
    private String keyPasswordBase64;
}
