/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.request;

import com.scotiabank.oauth2.clientcredentials.ClientCredentialsConfigurationException;
import com.scotiabank.oauth2.clientcredentials.CurrentTimeGenerator;
import com.google.common.base.Charsets;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.util.Base64URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientAssertionGeneratorTest {

    @Mock
    JWSSigner signer;

    @Mock
    CurrentTimeGenerator currentTimeGenerator;

    ClientAssertionGenerator sut;

    @Before
    public void init() throws ClientCredentialsConfigurationException {
        ClientCredentialsAssertionGenerationProperties props = new ClientCredentialsAssertionGenerationProperties();
        props.setClientId("client id");
        props.setTokenUrl("token url");
        props.setAssertionTokenExpiryTimeMs((int)Duration.of(10, ChronoUnit.MINUTES).toMillis());

        sut = new ClientAssertionGenerator(props, signer, currentTimeGenerator);
    }

    @Test
    public void getAssertionToken_whenNoExceptionsAreThrown_generatesAndSignsToken() throws JOSEException {
        Instant staticInstant = Instant.parse("2017-11-17T13:00:00.000Z");
        when(currentTimeGenerator.getCurrentTime()).thenReturn(staticInstant);

        when(signer.supportedJWSAlgorithms()).thenReturn(new HashSet<JWSAlgorithm>(Arrays.asList(JWSAlgorithm.RS256)));

        String expectedPayload = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJjbGllbnQgaWQiLCJhdWQiOiJ0b2tlbiB1cmwiLCJpc3MiOiJjbGllbnQgaWQiLCJleHAiOjE1MTA5MjQyMDB9";

        when(signer.sign(argThat(arg -> arg.getAlgorithm() == JWSAlgorithm.RS256),
                         eq(expectedPayload.getBytes(Charsets.UTF_8))))
            .thenReturn(new Base64URL("signature"));

        String result = sut.getAssertionToken();

        assertThat(result).isEqualTo(expectedPayload + ".signature");
    }

    @Test
    public void getAssertionToken_whenExceptionIsThrown_returnsNull() throws JOSEException {
        Instant staticInstant = Instant.parse("2017-11-17T13:00:00.000Z");
        when(currentTimeGenerator.getCurrentTime()).thenReturn(staticInstant);

        when(signer.supportedJWSAlgorithms()).thenReturn(new HashSet<JWSAlgorithm>(Arrays.asList(JWSAlgorithm.RS256)));

        String expectedPayload = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJjbGllbnQgaWQiLCJhdWQiOiJ0b2tlbiB1cmwiLCJpc3MiOiJjbGllbnQgaWQiLCJleHAiOjE1MTA5MjQyMDB9";

        when(signer.sign(argThat(arg -> arg.getAlgorithm() == JWSAlgorithm.RS256),
            eq(expectedPayload.getBytes(Charsets.UTF_8))))
            .thenThrow(new JOSEException("some message"));

        String result = sut.getAssertionToken();

        assertThat(result).isNull();
    }
}
