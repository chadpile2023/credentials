/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import com.scotiabank.oauth2.clientcredentials.request.ClientAssertionRequestEnhancer;
import com.scotiabank.oauth2.clientcredentials.request.ClientCredentialsAssertionGenerationProperties;
import com.scotiabank.oauth2.clientcredentials.request.ClientCredentialsTokenRequestor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientCredentialsInterceptorConfig {

    @Bean("com.scotiabank.oauth2.clientcredentials ClientCredentialsResourceDetails")
    public ClientCredentialsResourceDetails resource(ClientCredentialsAssertionGenerationProperties properties) {
        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
        resource.setAccessTokenUri(properties.getTokenUrl());
        resource.setGrantType("client_credentials");
        resource.setClientAuthenticationScheme(AuthenticationScheme.form);
        return resource;
    }

    @Bean("com.scotiabank.oauth2.clientcredentials TokenProvider")
    public ExpiringResourceProvider<String> clientCredentialsAccessTokenProvider(
            ClientCredentialsResourceDetails resource,
            @Autowired(required = false)
            @Qualifier("com.scotiabank.oauth2.clientcredentials OAuth2TokenRestTemplate")
                RestTemplate restTemplate,
            ClientAssertionRequestEnhancer enhancer,
            ResourceAutoRenewalProperties tokenManagementProperties,
            CurrentTimeGenerator currentTimeGenerator) {
        ClientCredentialsTokenRequestor tokenRequestor = new ClientCredentialsTokenRequestor(resource, restTemplate);
        tokenRequestor.setTokenRequestEnhancer(enhancer);

        AutoRenewAsyncResourceProvider<String> provider = new AutoRenewAsyncResourceProvider<>(
            tokenRequestor,
            tokenManagementProperties,
            currentTimeGenerator);
        return provider;
    }
}
