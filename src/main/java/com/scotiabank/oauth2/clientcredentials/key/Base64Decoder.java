/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.scotiabank.oauth2.clientcredentials.key;

import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Wrapper for Base64 decoding functionality
 */
@Component("com.scotiabank.oauth2.clientcredentials.key Base64Decoder")
public class Base64Decoder {
    public byte[] decodeBase64Bytes(String value) {
        return DatatypeConverter.parseBase64Binary(value);
    }

    public char[] decodeBase64CharArray(String value) throws UnsupportedEncodingException {
        return Charset.forName("UTF-8").decode(ByteBuffer.wrap(decodeBase64Bytes(value))).array();
    }

}