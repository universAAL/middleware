/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

	See the NOTICE file distributed with this work for additional
	information regarding copyright ownership

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	  http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.xsd;

import java.io.IOException;

import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.xsd.util.Base64;

/**
 * Support for XSD data type base64Binary. <br>
 * Note: this should not be used for large data sets.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#base64Binary">XML Schema</a>
 */
public final class Base64Binary {
    public static final String MY_URI = TypeMapper.XSD_NAMESPACE + "base64Binary";

    private String encodedVal = null;
    private byte[] val = null;

    /**
     * Constructor for the decoded value (the binary data).
     *
     * @param val
     *            the binary data.
     */
    public Base64Binary(byte[] val) {
	if (val == null)
	    throw new NullPointerException();
	this.val = val;
    }

    /**
     * Constructor for the encoded value (the Base64 value).
     *
     * @param val
     *            the encoded data.
     */
    public Base64Binary(String encodedVal) {
	if (encodedVal == null)
	    throw new NullPointerException();
	this.encodedVal = encodedVal;
    }

    /**
     * Get the length of the decoded value (the binary value).
     *
     * @return the length of the decoded value.
     */
    public int getDecodedLength() {
	if (val == null)
	    val = decode(encodedVal);

	return val.length;
    }

    @Override
    public String toString() {
	if (encodedVal == null)
	    encodedVal = encode(val);
	return encodedVal;
    }

    public byte[] getVal() {
	if (val == null)
	    val = decode(encodedVal);
	return val;
    }

    public static String encode(byte[] val) {
	return Base64.encodeBytes(val);
    }

    public static byte[] decode(String encodedVal) {
	try {
	    return Base64.decode(encodedVal);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!(obj instanceof Base64Binary))
	    return false;
	if (this.toString().equals(obj.toString()))
	    return true;
	return false;
    }
}
