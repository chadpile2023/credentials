/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import com.scotiabank.oauth2.clientcredentials.ClientCredentialsConfigurationException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;

@Configuration
public class ClientCredentialsSignatureConfig {

    @Bean("com.scotiabank.oauth2.clientcredentials KeyStore")
    KeyStore keyStore(KeyStoreDecoder keyStoreDecoder,
                      ClientCredentialsSignatureProperties properties) throws ClientCredentialsConfigurationException {
        return keyStoreDecoder.decode(
            properties.getKeyStoreBase64(),
            properties.getKeyStorePasswordBase64(),
            properties.getKeyStoreFormat());
    }

    @Bean("com.scotiabank.oauth2.clientcredentials JwsSigner")
    public JWSSigner jwsSigner(Base64Decoder base64Decoder,
                               @Qualifier("com.scotiabank.oauth2.clientcredentials KeyStore") KeyStore keyStore,
                               ClientCredentialsSignatureProperties properties) throws GeneralSecurityException, IOException, ClientCredentialsConfigurationException {
        PrivateKey key =  (PrivateKey) keyStore.getKey(
            properties.getKeyName(),
            base64Decoder.decodeBase64CharArray(properties.getKeyPasswordBase64()));
        return new RSASSASigner(key);
    }
}
