/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.request;

import com.scotiabank.oauth2.clientcredentials.ExpiringResource;
import com.scotiabank.oauth2.clientcredentials.ExpiringResourceProvider;
import com.scotiabank.oauth2.clientcredentials.TokenResource;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

public class ClientCredentialsTokenRequestor extends ClientCredentialsAccessTokenProvider implements ExpiringResourceProvider<String> {

    private final ClientCredentialsResourceDetails resource;
    private final RestTemplate restTemplate;

    public ClientCredentialsTokenRequestor(ClientCredentialsResourceDetails resource,
                                           RestTemplate restTemplate) {
        this.resource = resource;
        this.restTemplate = restTemplate;

        if (restTemplate != null) {
            setMessageConverters(restTemplate.getMessageConverters());
        }
    }

    @Override
    protected RestOperations getRestTemplate() {
        if (restTemplate != null) {
            return restTemplate;
        } else {
            return super.getRestTemplate();
        }
    }

    @Override
    public ExpiringResource<String> getResource() {
        OAuth2AccessToken token = obtainAccessToken(resource, new DefaultAccessTokenRequest());
        if (token != null && token.getValue() != null) {
            return new TokenResource(token);
        }
        return null;
    }
}
