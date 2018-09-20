/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component("clientCredentialsTokenInterceptor")
@RequiredArgsConstructor
public class ClientCredentialsTokenInterceptor implements ClientHttpRequestInterceptor {
    private static final String TOKEN_KEY = "Authorization";
    private static final String TOKEN_VALUE = "Bearer %s";

    private final ExpiringResourceProvider<String> tokenProvider;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ExpiringResource<String> token;
        if (!request.getHeaders().containsKey(TOKEN_KEY)) {
            if ((token = tokenProvider.getResource()) == null) {
                throw new ClientCredentialsTokenRequestFailedException("Client credentials token request failed");
            }

            request.getHeaders().add(TOKEN_KEY, String.format(TOKEN_VALUE, token.getValue()));
        } else {
            log.info("Skipping setting the client credentials token on request as authorization header has already been set");
        }

        return execution.execute(request, body);
    }
}
