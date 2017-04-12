/*	
Copyright 2017-2020 Carsten Stockloew

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
package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;
import org.universAAL.middleware.xsd.Base64Binary;
import org.universAAL.middleware.xsd.NonNegativeInteger;

/**
 * A {@link TypeExpression} ({@link LengthRestriction}) that contains all
 * {@link Base64Binary} values with a given minimum and/or maximum length.
 * 
 * @author Carsten Stockloew
 */
public class Base64Restriction extends LengthRestriction {

    /** URI of the data type. */
    public static final String DATATYPE_URI = TypeMapper.getDatatypeURI(Base64Binary.class);

    /** Standard constructor. */
    public Base64Restriction() {
	super(DATATYPE_URI);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    @Override
    public TypeExpression copy() {
	return copyTo(new Base64Restriction());
    }

    /**
     * Calculate the length of a member. Overriden to provide the length of the
     * <i>decoded</i> value instead of the encoded value as the default
     * implementation of the super class would provide.
     * 
     * @param member
     *            the member for which to calculate the length.
     * @return the length as NonNegativeInteger or a {@link Variable} if the
     *         member is a {@link Variable}.
     */
    @Override
    protected Object getMemberLen(Object member) {
	if (member instanceof Base64Binary) {
	    return new NonNegativeInteger(((Base64Binary)member).getDecodedLength());
	}
	return super.getMemberLen(member);
    }
}
