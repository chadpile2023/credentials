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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { EnableClientCredentialsInterceptor.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("loadsInterceptor")
public class InterceptorInitialization {

    @Autowired
    @Qualifier("clientCredentialsTokenInterceptor")
    ClientHttpRequestInterceptor interceptor;

    @Test
    public void interceptorInitialization_whenValidProperties_getsInitialized() {
        assertThat(interceptor).isNotNull();
    }
}
