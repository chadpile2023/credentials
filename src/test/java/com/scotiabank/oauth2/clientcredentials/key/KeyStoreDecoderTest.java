/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import com.scotiabank.oauth2.clientcredentials.ClientCredentialsConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyStoreDecoderTest {
    private static final String PASSWORD_BASE64 = "password base64";
    private static final char[] PASSWORD = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
    private static final String DATA_BASE64 = "data base64";
    private static final byte[] DATA = "data".getBytes();
    private static final String KEYSTORE_TYPE = "some type";

    @Mock
    Base64Decoder mockDecoder;

    @Mock
    KeyStoreFactory mockFactory;

    @InjectMocks
    KeyStoreDecoder sut;

    @Mock
    KeyStore fakeStore;

    @Test
    public void decode_whenNoExceptionsAreThrown_decodesDataAndPasswordAndDelegatesToFactory() throws UnsupportedEncodingException, ClientCredentialsConfigurationException {
        when(mockDecoder.decodeBase64CharArray(eq(PASSWORD_BASE64))).thenReturn(PASSWORD);
        when(mockDecoder.decodeBase64Bytes(DATA_BASE64)).thenReturn(DATA);
        when(mockFactory.create(eq(DATA), eq(PASSWORD), eq(KEYSTORE_TYPE)))
            .thenReturn(fakeStore);

        KeyStore result = sut.decode(DATA_BASE64, PASSWORD_BASE64, KEYSTORE_TYPE);

        assertThat(result).isSameAs(fakeStore);

        verify(mockDecoder).decodeBase64CharArray(PASSWORD_BASE64);
        verify(mockDecoder).decodeBase64Bytes(DATA_BASE64);
        verify(mockFactory).create(DATA, PASSWORD, KEYSTORE_TYPE);
    }

    @Test
    public void decode_whenExceptionIsThrown_wrapsUnsupportedEncodingException() throws UnsupportedEncodingException {
        when(mockDecoder.decodeBase64CharArray(eq(PASSWORD_BASE64))).thenThrow(new UnsupportedEncodingException());

        assertThatThrownBy(() -> sut.decode(DATA_BASE64, PASSWORD_BASE64, KEYSTORE_TYPE))
            .isExactlyInstanceOf(ClientCredentialsConfigurationException.class)
            .hasCauseExactlyInstanceOf(UnsupportedEncodingException.class);
    }

}
