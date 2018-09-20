/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.request;

import com.scotiabank.oauth2.clientcredentials.ClientCredentialsConfigurationException;
import com.scotiabank.oauth2.clientcredentials.CurrentTimeGenerator;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component("com.scotiabank.oauth2.clientcredentials ClientAssertionGenerator")
public class ClientAssertionGenerator {

    private static final Logger log = LoggerFactory.getLogger(ClientAssertionGenerator.class);

    private final ClientCredentialsAssertionGenerationProperties properties;
    private final JWSSigner signer;
    private final CurrentTimeGenerator currentTimeGenerator;

    public ClientAssertionGenerator(ClientCredentialsAssertionGenerationProperties properties,
                                    JWSSigner signer,
                                    CurrentTimeGenerator currentTimeGenerator) throws ClientCredentialsConfigurationException {
        this.properties = properties;
        this.signer = signer;
        this.currentTimeGenerator = currentTimeGenerator;
    }

    public String getAssertionToken()  {
        log.info("Getting oauth2 assertion token");

        Instant expirationTime = currentTimeGenerator.getCurrentTime()
            .plus(properties.getAssertionTokenExpiryTimeMs(), ChronoUnit.MILLIS);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(properties.getClientId())
            .issuer(properties.getClientId())
            .audience(properties.getTokenUrl())
            .expirationTime(Date.from(expirationTime))
            .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            log.error("It was not possible to sign JWToken", e);
            return null;
        }
        return signedJWT.serialize();
    }

}