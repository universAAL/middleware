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
public class Base64Binary {
    public static final String MY_URI = TypeMapper.XSD_NAMESPACE
	    + "base64Binary";

    private String encodedVal = null;
    private byte[] val = null;

    public Base64Binary(byte[] val) {
	this.val = val;
    }

    public Base64Binary(String encodedVal) {
	this.encodedVal = encodedVal;
    }

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

    public static final String encode(byte[] val) {
	return Base64.encodeBytes(val);
    }

    public static final byte[] decode(String encodedVal) {
	try {
	    return Base64.decode(encodedVal);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }

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
