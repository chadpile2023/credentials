/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import com.scotiabank.oauth2.clientcredentials.key.*;
import com.scotiabank.oauth2.clientcredentials.request.ClientAssertionGenerator;
import com.scotiabank.oauth2.clientcredentials.request.ClientAssertionRequestEnhancer;
import com.scotiabank.oauth2.clientcredentials.request.ClientCredentialsAssertionGenerationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
@Import({
    // key
    Base64Decoder.class,
    KeyStoreDecoder.class,
    KeyStoreFactory.class,
    KeyStoreProvider.class,
    ClientCredentialsSignatureConfig.class,
    // request
    ClientAssertionGenerator.class,
    ClientAssertionRequestEnhancer.class,
    // management
    ClientCredentialsInterceptorConfig.class,
    ClientCredentialsTokenInterceptor.class,
    CurrentTimeGenerator.class
})
@EnableConfigurationProperties({
    ClientCredentialsSignatureProperties.class,
    ClientCredentialsAssertionGenerationProperties.class,
    ResourceAutoRenewalProperties.class
})
@Configuration
public @interface EnableClientCredentialsInterceptor {
}
