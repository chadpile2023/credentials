/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.request;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientAssertionRequestEnhancerTest {

    public static final String EXPECTED_CLIENT_ASSERTION_FIELD = "client_assertion";
    public static final String EXPECTED_CLIENT_ASSERTION_TYPE_FIELD = "client_assertion_type";
    public static final String EXPECTED_CLIENT_ASSERTION_TYPE_VALUE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    @InjectMocks
    private ClientAssertionRequestEnhancer sut;

    @Mock
    private ClientAssertionGenerator assertionGenerator;

    @Test
    public void enhance_always_addsSignedAssertionAndTypeToForm() {
        when(assertionGenerator.getAssertionToken()).thenReturn("123");

        AccessTokenRequest req = mock(AccessTokenRequest.class);
        OAuth2ProtectedResourceDetails details = mock(OAuth2ProtectedResourceDetails.class);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        sut.enhance(req, details, form, new HttpHeaders());

        assertThat(form.getFirst(EXPECTED_CLIENT_ASSERTION_FIELD)).isEqualTo("123");
        assertThat(form.getFirst(EXPECTED_CLIENT_ASSERTION_TYPE_FIELD)).isEqualTo(EXPECTED_CLIENT_ASSERTION_TYPE_VALUE);

        verify(assertionGenerator).getAssertionToken();
    }
}
