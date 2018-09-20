/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.sql.Date;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenResourceTest {

    TokenResource sut;

    @Mock
    OAuth2AccessToken token;

    @Test
    public void getResource_always_returnsTokenValue() {
        when(token.getValue()).thenReturn("some value");

        sut = new TokenResource(token);

        assertThat(sut.getValue()).isEqualTo("some value");
    }

    @Test
    public void getResource_whenValueIsNull_returnsNull() {
        when(token.getValue()).thenReturn(null);

        sut = new TokenResource(token);

        assertThat(sut.getValue()).isEqualTo(null);
    }

    @Test
    public void getExpiration_whenTokenIsValidAndContainsExpiry_returnsTokenExpiry() {
        when(token.getValue()).thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MjM0NDgwNjF9.LUxZtIcMfq5AMkWcZUoq3PvVje8A20V-PU_LoYs_awg");

        sut = new TokenResource(token);

        assertThat(sut.getExpiration()).isEqualTo(Instant.parse("2018-04-11T12:01:01Z"));
    }

    @Test
    public void getExpiration_whenTokenIsValidAndDoesNotContainExpiry_returnsExpiryFromResponse() {
        when(token.getExpiration()).thenReturn(Date.from(Instant.parse("2016-04-11T12:01:01Z")));
        when(token.getValue()).thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2o");

        sut = new TokenResource(token);

        assertThat(sut.getExpiration()).isEqualTo(Instant.parse("2016-04-11T12:01:01Z"));
    }

    @Test
    public void getExpiration_whenTokenIsInvalid_returnsExpiryFromResponse() {
        when(token.getExpiration()).thenReturn(Date.from(Instant.parse("2016-04-11T12:01:01Z")));
        when(token.getValue()).thenReturn("invalid token format");

        sut = new TokenResource(token);

        assertThat(sut.getExpiration()).isEqualTo(Instant.parse("2016-04-11T12:01:01Z"));
    }

    @Test
    public void getExpiration_whenTokenIsValidButClaimsInvalid_returnsExpiryFromResponse() {
        when(token.getExpiration()).thenReturn(Date.from(Instant.parse("2016-04-11T12:01:01Z")));
        when(token.getValue()).thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfA.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2o");

        sut = new TokenResource(token);

        assertThat(sut.getExpiration()).isEqualTo(Instant.parse("2016-04-11T12:01:01Z"));

    }

    @Test
    public void getExpiration_whenTokenDoesNotContainExpiryAndResponseDoesNotContainExpiry_returnsDefaultExpiry() {
        sut = new TokenResource(token);

        assertThat(sut.getExpiration()).isEqualTo(Instant.ofEpochMilli(0));
    }
}
