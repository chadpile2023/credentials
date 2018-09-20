/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CurrentTimeGeneratorTest {

    @InjectMocks
    CurrentTimeGenerator sut;

    @Test
    public void getCurrentTimeReturnsCurrentTime() {

        Instant result = sut.getCurrentTime();

        Instant current = Instant.now();

        Instant currentLow = current.minusNanos(10000);
        Instant currentHigh = current.plusNanos(1);

        assertThat(result.isAfter(currentLow)).isTrue();
        assertThat(result.isBefore(currentHigh)).isTrue();
    }
}
