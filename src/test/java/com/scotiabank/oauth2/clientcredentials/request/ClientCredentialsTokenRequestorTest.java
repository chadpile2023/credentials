/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.request;

import com.scotiabank.oauth2.clientcredentials.ExpiringResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientCredentialsTokenRequestorTest {

    ClientCredentialsTokenRequestor sut;

    @Mock
    RestTemplate restTemplate;

    @Mock
    ClientCredentialsResourceDetails resource;

    @Test
    public void getToken_always_makesRestCallToTokenUrl() {
        when(restTemplate.getMessageConverters()).thenReturn(new ArrayList<>());

        sut = new ClientCredentialsTokenRequestor(resource, restTemplate);

        OAuth2AccessToken token = mock(OAuth2AccessToken.class);
        when(token.getValue()).thenReturn("some token");

        when(resource.getAccessTokenUri()).thenReturn("some uri");
        when(restTemplate.execute(eq("some uri"), eq(HttpMethod.POST), any(), any(), anyMap()))
            .thenReturn(token);

        ExpiringResource<String> result = sut.getResource();

        assertThat(result.getValue()).isEqualTo("some token");

        verify(resource).getAccessTokenUri();
        verify(restTemplate).execute(eq("some uri"), eq(HttpMethod.POST), any(), any(), anyMap());
    }

    @Test
    public void getRestTemplate_whenNotPassedIn_returnsGeneratedTemplate() {
        sut = new ClientCredentialsTokenRequestor(resource, null);

        RestOperations result = sut.getRestTemplate();

        assertThat(result).isNotNull();
    }
}
