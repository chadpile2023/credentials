/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

public class Base64DecoderTest {

    private Base64Decoder sut = new Base64Decoder();

    @Test
    public void decodeBase64Bytes_whenValidBytesArePassed_decodes() {
        byte[] result = sut.decodeBase64Bytes("AQIECBAgQIA=");

        assertThat(result.length).isEqualTo(8);
        for (int i = 0; i < result.length; i++) {
            assertThat(result[i]).isEqualTo((byte) Math.pow(2, i));
        }
    }

    @Test
    public void decodeBase64Bytes_whenValidStringIsPassed_decodes() throws UnsupportedEncodingException {
        String result = new String(sut.decodeBase64CharArray("c29tZSBkYXlzIEkgd2lzaCBJIGRpZG4ndCBoYXZlIHRvIHdyaXRlIHVuaXQgdGVzdHM="));

        assertThat(result).isEqualTo("some days I wish I didn't have to write unit tests");
    }
}
