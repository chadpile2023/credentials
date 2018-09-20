/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import com.scotiabank.oauth2.clientcredentials.ClientCredentialsConfigurationException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.security.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientCredentialsSignatureConfigTest {

    @InjectMocks
    ClientCredentialsSignatureConfig sut;

    @Mock
    KeyStoreDecoder keyStoreDecoder;

    @Mock
    Base64Decoder base64Decoder;

    @Mock
    KeyStore mockStore;

    @Mock
    PrivateKey privateKey;

    @Mock
    ClientCredentialsSignatureProperties properties;

    @Test
    public void keyStore_always_passesParametersToDecoder() throws ClientCredentialsConfigurationException {
        when(keyStoreDecoder.decode("a", "b", "c"))
            .thenReturn(mockStore);
        when(properties.getKeyStoreBase64()).thenReturn("a");
        when(properties.getKeyStorePasswordBase64()).thenReturn("b");
        when(properties.getKeyStoreFormat()).thenReturn("c");

        KeyStore store = sut.keyStore(keyStoreDecoder, properties);

        assertThat(store).isSameAs(mockStore);

        verify(keyStoreDecoder).decode(eq("a"), eq("b"), eq("c"));
        verify(properties).getKeyStoreBase64();
        verify(properties).getKeyStorePasswordBase64();
        verify(properties).getKeyStoreFormat();
    }

    @Test
    public void jwsSigner_always_extractsPrivateKeyAndBuildsSigner() throws GeneralSecurityException, IOException, ClientCredentialsConfigurationException {
        when(mockStore.getKey(eq("alias"), eq(new char[] {'a', 'b', 'c'}))).thenReturn(privateKey);
        when(base64Decoder.decodeBase64CharArray(eq("pass"))).thenReturn(new char[] {'a', 'b', 'c'});
        when(properties.getKeyName()).thenReturn("alias");
        when(properties.getKeyPasswordBase64()).thenReturn("pass");
        // simpler to do this than to extract generation of RSASSASigner
        when(privateKey.getAlgorithm()).thenReturn("RSA");

        JWSSigner signer = sut.jwsSigner(base64Decoder, mockStore, properties);

        assertThat(signer).isInstanceOf(RSASSASigner.class);
        assertThat(((RSASSASigner)signer).getPrivateKey()).isSameAs(privateKey);

        verify(mockStore).getKey(eq("alias"), eq(new char[] {'a', 'b', 'c'}));
    }
}
