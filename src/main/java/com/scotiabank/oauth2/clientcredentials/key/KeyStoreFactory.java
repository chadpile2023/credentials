/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import com.scotiabank.oauth2.clientcredentials.ClientCredentialsConfigurationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Responsible for constructing the key store object using the raw keystore data and password
 */
@RequiredArgsConstructor
@Component("com.scotiabank.oauth2.clientcredentials.key KeyStoreFactory")
public class KeyStoreFactory {

    private static final Logger log = LoggerFactory.getLogger(KeyStoreFactory.class);

    private final KeyStoreProvider keyStoreProvider;

    public KeyStore create(byte[] storeData, char[] password, String keyStoreFormat) throws ClientCredentialsConfigurationException {
        try {
            InputStream storeStream = new ByteArrayInputStream(storeData);

            KeyStore store = keyStoreProvider.getKeyStore(keyStoreFormat);
            store.load(storeStream, password);
            storeStream.close();
            return store;
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            log.error("Unable to load key", e);
            throw new ClientCredentialsConfigurationException("Unable to load key", e);
        } catch (KeyStoreException e) {
            log.error("Unable to create keystore", e);
            throw new ClientCredentialsConfigurationException("Unable to create keystore", e);
        }
    }
}