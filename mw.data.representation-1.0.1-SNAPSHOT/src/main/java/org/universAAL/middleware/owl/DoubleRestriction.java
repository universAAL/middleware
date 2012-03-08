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

public class DoubleRestriction extends BoundedValueRestriction {

    public static final String DATATYPE_URI = TypeMapper
	    .getDatatypeURI(Double.class);

    // substitutions for Double.MIN_NORMAL
    private static final double DOUBLE_SMALLEST_POSITIVE_VALUE = Double
	    .longBitsToDouble(0x0010000000000000L);

    public DoubleRestriction() {
	super(DATATYPE_URI);
    }

    public DoubleRestriction(double min, boolean minInclusive, double max,
	    boolean maxInclusive) {
	this(new Double(min), minInclusive, new Double(max), maxInclusive);
    }

    public DoubleRestriction(Double min, boolean minInclusive, Double max,
	    boolean maxInclusive) {
	super(TypeMapper.getDatatypeURI(Double.class), min, minInclusive, max,
		maxInclusive);
    }

    protected Comparable getNext(Comparable c) {
	return new Double(((Double) c).doubleValue()
		+ DOUBLE_SMALLEST_POSITIVE_VALUE);
    }

    protected Comparable getPrevious(Comparable c) {
	return new Double(((Double) c).doubleValue()
		- DOUBLE_SMALLEST_POSITIVE_VALUE);
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#copy() */
    public ClassExpression copy() {
	return copyTo(new DoubleRestriction());
    }
}
