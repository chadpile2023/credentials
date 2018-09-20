/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
public class TokenResource implements ExpiringResource<String> {
    private final String tokenValue;
    private final Instant expiration;

    public TokenResource(OAuth2AccessToken token) {
        tokenValue = token.getValue();

        // if possible, base it on the actual token,
        // or base it on expiry from response,
        // or consider it expired
        expiration = Optional.ofNullable(token.getValue())
            .map(t -> {
                try {
                    return SignedJWT.parse(t);
                } catch (ParseException e) {
                    log.error("Unable to base expiry on token - parse exception on token", e);
                    return null;
                }
            })
            .map(jwt -> {
                try {
                    return jwt.getJWTClaimsSet();
                } catch (ParseException e) {
                    log.error("Unable to base expiry on token - parse exception on claims", e);
                    return null;
                }
            })
            .map(JWTClaimsSet::getExpirationTime)
            .map(Date::toInstant)
            .orElse(Optional.ofNullable(token.getExpiration())
                .map(Date::toInstant)
                .orElse(Instant.ofEpochMilli(0)));
    }

    @Override
    public Instant getExpiration() {
        return expiration;
    }

    @Override
    public String getValue() {
        return tokenValue;
    }
}
