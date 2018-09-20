/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientCredentialsTokenInterceptorTest {

    @InjectMocks
    ClientCredentialsTokenInterceptor sut;

    @Mock
    ExpiringResourceProvider<String> tokenProvider;

    @Mock
    HttpHeaders httpHeaders;

    @Mock
    HttpRequest httpRequest;

    @Mock
    ClientHttpRequestExecution clientHttpRequestExecution;

    @Mock
    ClientHttpResponse clientHttpResponse;

    byte body[] = new byte[0];

    @Test
    public void intercept_whenAuthorizationHeaderExists_doesNotModifyRequest() throws IOException {
        when(httpRequest.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.containsKey("Authorization")).thenReturn(true);

        when(clientHttpRequestExecution.execute(same(httpRequest), same(body))).thenReturn(clientHttpResponse);

        ClientHttpResponse result = sut.intercept(httpRequest, body, clientHttpRequestExecution);

        assertThat(result).isSameAs(clientHttpResponse);

        verify(httpHeaders, never()).add(any(), any());
    }

    @Test
    public void intercept_whenTokenProviderReturnsNull_throwsException() throws IOException {
        when(httpRequest.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.containsKey("Authorization")).thenReturn(false);

        when(tokenProvider.getResource()).thenReturn(null);

        assertThatThrownBy(() -> sut.intercept(httpRequest, body, clientHttpRequestExecution))
            .isExactlyInstanceOf(ClientCredentialsTokenRequestFailedException.class);
    }

    @Test
    public void intercept_whenTokenProviderReturnsToken_doesNotModifyRequest() throws IOException {
        when(httpRequest.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.containsKey("Authorization")).thenReturn(false);

        ExpiringResource<String> token = mock(ExpiringResource.class);
        when(token.getValue()).thenReturn("token");
        when(tokenProvider.getResource()).thenReturn(token);

        when(clientHttpRequestExecution.execute(same(httpRequest), same(body))).thenReturn(clientHttpResponse);

        ClientHttpResponse result = sut.intercept(httpRequest, body, clientHttpRequestExecution);

        assertThat(result).isSameAs(clientHttpResponse);

        verify(httpHeaders).add(eq("Authorization"), eq("Bearer token"));
    }
}
