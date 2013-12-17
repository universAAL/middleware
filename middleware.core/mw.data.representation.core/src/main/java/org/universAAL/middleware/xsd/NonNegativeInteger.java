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

import org.universAAL.middleware.rdf.TypeMapper;

/**
 * Support for XSD data type nonNegativeInteger. Currently, only int values are
 * supported (in the range 0 - 2^31).
 * 
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#nonNegativeInteger">XML
 *      Schema</a>
 */
public final class NonNegativeInteger {
    public static final String MY_URI = TypeMapper.XSD_NAMESPACE
	    + "nonNegativeInteger";

    private boolean isInt;

    private int intval;

    // private BigInteger bigintval;

    public NonNegativeInteger(int val) {
	if (val < 0)
	    throw new IllegalArgumentException(
		    "NonNegativeInteger must be non-negative: " + val);
	isInt = true;
	intval = val;
    }

    public NonNegativeInteger(String val) {
	int intval = Integer.valueOf(val).intValue();
	if (intval < 0)
	    throw new IllegalArgumentException(
		    "NonNegativeInteger must be non-negative: " + val);

	isInt = true;
	this.intval = intval;
	// TODO: if the value is too big, use BigInteger
    }

    public int intValue() {
	if (isInt)
	    return intval;
	else
	    return Integer.MAX_VALUE;
    }

    public String toString() {
	return Integer.toString(intval);
    }

    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj instanceof NonNegativeInteger)
	    return this.intval == ((NonNegativeInteger) obj).intval;
	if (obj instanceof Integer)
	    return this.intval == ((Integer) obj).intValue();
	return false;
    }
}
