/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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

public class FloatRestriction extends BoundedValueRestriction {

    public static final String DATATYPE_URI = TypeMapper
	    .getDatatypeURI(Float.class);

    // substitutions for Float.MIN_NORMAL
    private static final float FLOAT_SMALLEST_POSITIVE_VALUE = Float
	    .intBitsToFloat(0x00800000);

    public FloatRestriction() {
	super(DATATYPE_URI);
    }

    public FloatRestriction(float min, boolean minInclusive, float max,
	    boolean maxInclusive) {
	this(new Float(min), minInclusive, new Float(max), maxInclusive);
    }

    public FloatRestriction(Float min, boolean minInclusive, Float max,
	    boolean maxInclusive) {
	super(TypeMapper.getDatatypeURI(Float.class), min, minInclusive, max,
		maxInclusive);
    }

    protected Comparable getNext(Comparable c) {
	return new Float(((Float) c).floatValue()
		+ FLOAT_SMALLEST_POSITIVE_VALUE);
    }

    protected Comparable getPrevious(Comparable c) {
	return new Float(((Float) c).floatValue()
		- FLOAT_SMALLEST_POSITIVE_VALUE);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	return copyTo(new FloatRestriction());
    }
}
