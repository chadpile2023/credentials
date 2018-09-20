/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import com.scotiabank.oauth2.clientcredentials.request.ClientAssertionRequestEnhancer;
import com.scotiabank.oauth2.clientcredentials.request.ClientCredentialsAssertionGenerationProperties;
import com.scotiabank.oauth2.clientcredentials.request.ClientCredentialsTokenRequestor;
import org.junit.Test;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientCredentialsInterceptorConfigTest {

    ClientCredentialsInterceptorConfig sut = new ClientCredentialsInterceptorConfig();

    @Test
    public void resource_always_generatesResource() {
        ClientCredentialsAssertionGenerationProperties props = new ClientCredentialsAssertionGenerationProperties();
        props.setTokenUrl("some url");

        ClientCredentialsResourceDetails result = sut.resource(props);

        assertThat(result.getAccessTokenUri()).isEqualTo("some url");
        assertThat(result.getGrantType()).isEqualTo("client_credentials");
        assertThat(result.getClientAuthenticationScheme()).isEqualTo(AuthenticationScheme.form);
    }

    @Test
    public void clientCredentialsAccessTokenProvider_always_constructsAsynchAccessTokenProvider() {
        ClientCredentialsResourceDetails resource = mock(ClientCredentialsResourceDetails.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ClientAssertionRequestEnhancer enhancer = mock(ClientAssertionRequestEnhancer.class);
        ResourceAutoRenewalProperties tokenManagementProps = mock(ResourceAutoRenewalProperties.class);
        CurrentTimeGenerator currentTimeGenerator = mock(CurrentTimeGenerator.class);

        when(tokenManagementProps.getRenewalPeriodMs()).thenReturn(Integer.MAX_VALUE);

        ExpiringResourceProvider<String> result = sut.clientCredentialsAccessTokenProvider(resource, restTemplate, enhancer, tokenManagementProps, currentTimeGenerator);

        assertThat(result).isInstanceOf(AutoRenewAsyncResourceProvider.class);
        assertThat(ReflectionTestUtils.getField(result, "management")).isSameAs(tokenManagementProps);
        assertThat(ReflectionTestUtils.getField(result, "currentTimeGenerator")).isSameAs(currentTimeGenerator);

        Object tokenDelegate = ReflectionTestUtils.getField(result, "providerDelegate");
        assertThat(tokenDelegate).isInstanceOf(ClientCredentialsTokenRequestor.class);

        ClientCredentialsTokenRequestor delegate = (ClientCredentialsTokenRequestor)tokenDelegate;
        assertThat(ReflectionTestUtils.getField(delegate, "restTemplate")).isSameAs(restTemplate);
        assertThat(ReflectionTestUtils.getField(delegate, "resource")).isSameAs(resource);
    }
}
