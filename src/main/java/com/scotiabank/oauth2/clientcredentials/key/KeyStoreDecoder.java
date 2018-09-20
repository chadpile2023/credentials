/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import com.scotiabank.oauth2.clientcredentials.ClientCredentialsConfigurationException;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

/**
 * Responsible for decoding base64 encoded key stores and constructing the key store object using the data and password
 */
@Component("com.scotiabank.oauth2.clientcredentials.key KeyStoreDecoder")
public class KeyStoreDecoder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeyStoreDecoder.class);

    private static final String BASE64_ERROR = "Base64 decoding error";

    private Base64Decoder base64Decoder;
    private KeyStoreFactory keyStoreFactory;

    KeyStoreDecoder(Base64Decoder base64Decoder, KeyStoreFactory keyStoreFactory) {
        this.base64Decoder = base64Decoder;
        this.keyStoreFactory = keyStoreFactory;
    }

    public KeyStore decode(String keyStoreBase64, String keyStorePasswordBase64, String keyStoreFormat)
        throws ClientCredentialsConfigurationException {
        try {
            return keyStoreFactory.create(
                base64Decoder.decodeBase64Bytes(keyStoreBase64),
                base64Decoder.decodeBase64CharArray(keyStorePasswordBase64),
                keyStoreFormat);
        } catch (UnsupportedEncodingException e) {
            log.error(BASE64_ERROR, e);
            throw new ClientCredentialsConfigurationException(BASE64_ERROR, e);
        }
    }
}