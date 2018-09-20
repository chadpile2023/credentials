/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component("com.scotiabank.oauth2.clientcredentials CurrentTimeGenerator")
public class CurrentTimeGenerator {

    public Instant getCurrentTime() {
        return Instant.now();
    }
}
