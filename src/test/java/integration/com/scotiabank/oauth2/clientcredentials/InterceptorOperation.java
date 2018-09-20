/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package integration.com.scotiabank.oauth2.clientcredentials;

import com.scotiabank.oauth2.clientcredentials.EnableClientCredentialsInterceptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { EnableClientCredentialsInterceptor.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("loadsInterceptor")
@AutoConfigureWireMock(port = 0)
public class InterceptorOperation {
    @Autowired
    @Qualifier("clientCredentialsTokenInterceptor")
    ClientHttpRequestInterceptor interceptor;

    @Autowired
    @Value("${testUrl}")
    String testUrl;

    @Test
    public void interceptorInitialization_whenRequestIsMade_accessTokenGetsInjected() {
        stubFor(post(urlEqualTo("/oauth2/v1/token"))
            .withRequestBody(matching(".*client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer.*"))
            .withRequestBody(matching(".*grant_type=client_credentials.*"))
            .withRequestBody(matching(".*client_assertion=eyJhbGciOiJSUzI1NiJ9\\..+"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                "  \"access_token\": \"some-access-token\",\n" +
                "  \"expires_in\": 1000,\n" +
                "  \"token_type\": \"Bearer\"" +
                "}")));

        stubFor(get(urlEqualTo("/test"))
            .withHeader("Authorization", equalTo("Bearer some-access-token"))
            .willReturn(aResponse()
                .withBody("Success")));

        RestTemplate template = new RestTemplate();
        template.setInterceptors(Arrays.asList(interceptor));

        String response = template.getForObject(testUrl, String.class);

        assertThat(response).isEqualTo("Success");
    }
}
