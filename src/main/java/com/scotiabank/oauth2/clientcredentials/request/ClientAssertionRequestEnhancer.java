/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.request;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import static com.google.common.base.Preconditions.checkNotNull;

@Component("com.scotiabank.oauth2.clientcredentials ClientAssertionRequestEnhancer")
public class ClientAssertionRequestEnhancer implements RequestEnhancer {

    public static final String CLIENT_ASSERTION_FIELD = "client_assertion";
    public static final String CLIENT_ASSERTION_TYPE_FIELD = "client_assertion_type";
    public static final String CLIENT_ASSERTION_TYPE_VALUE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    private final ClientAssertionGenerator generator;

    public ClientAssertionRequestEnhancer(ClientAssertionGenerator generator) {
        this.generator = checkNotNull(generator);
    }

    @Override
    public void enhance(AccessTokenRequest request, OAuth2ProtectedResourceDetails resource,
                        MultiValueMap<String, String> form, HttpHeaders headers) {
        form.add(CLIENT_ASSERTION_FIELD, generator.getAssertionToken());
        form.add(CLIENT_ASSERTION_TYPE_FIELD, CLIENT_ASSERTION_TYPE_VALUE);
    }
}