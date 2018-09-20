/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import com.scotiabank.oauth2.clientcredentials.ClientCredentialsConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyStoreFactoryTest {
    private static final String EXPECTED_STORE_TYPE = "TYPE";
    private static final byte[] TEST_DATA = "data".getBytes();
    private static final char[] TEST_PASSWORD = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};

    @InjectMocks
    private KeyStoreFactory sut;

    @Mock
    KeyStore mockStore;

    @Mock
    KeyStoreProvider provider;

    @Test
    public void create_whenNoExceptionsAreThrown_createsAndLoadsKeyStore() throws Exception {
        when(provider.getKeyStore(eq(EXPECTED_STORE_TYPE))).thenReturn(mockStore);

        KeyStore result = sut.create(TEST_DATA, TEST_PASSWORD, EXPECTED_STORE_TYPE);

        ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
        ArgumentCaptor<char[]> passwordCaptor = ArgumentCaptor.forClass(char[].class);

        assertThat(result).isSameAs(mockStore);
        verify(result).load(streamCaptor.capture(), passwordCaptor.capture());

        byte[] temp = new byte[4];
        streamCaptor.getValue().read(temp);

        assertThat(temp).isEqualTo(TEST_DATA);
        assertThat(passwordCaptor.getValue()).isEqualTo(TEST_PASSWORD);
    }

    @Test
    public void create_whenKeyCreationExceptionIsThrown_wrapsExceptions() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        when(provider.getKeyStore(eq(EXPECTED_STORE_TYPE))).thenThrow(new KeyStoreException());

        assertThatThrownBy(() -> sut.create(TEST_DATA, TEST_PASSWORD, EXPECTED_STORE_TYPE))
            .isExactlyInstanceOf(ClientCredentialsConfigurationException.class)
            .hasCauseExactlyInstanceOf(KeyStoreException.class);
    }

    @Test
    public void create_whenKeyLoadingExceptionIsThrown_wrapsExceptions() throws Exception {
        when(provider.getKeyStore(eq(EXPECTED_STORE_TYPE))).thenReturn(mockStore);
        doThrow(new IOException()).when(mockStore).load(any(), any());

        assertThatThrownBy(() -> sut.create(TEST_DATA, TEST_PASSWORD, EXPECTED_STORE_TYPE))
            .isExactlyInstanceOf(ClientCredentialsConfigurationException.class)
            .hasCauseExactlyInstanceOf(IOException.class);
    }
}
