/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut f�r Graphische Datenverarbeitung
	
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

public class IntRestriction extends BoundedValueRestriction {

    public static final String DATATYPE_URI = TypeMapper
	    .getDatatypeURI(Integer.class);

    public IntRestriction() {
	super(DATATYPE_URI);
    }

    public IntRestriction(int min, boolean minInclusive, int max,
	    boolean maxInclusive) {
	this(new Integer(min), minInclusive, new Integer(max), maxInclusive);
    }

    public IntRestriction(Integer min, boolean minInclusive, Integer max,
	    boolean maxInclusive) {
	super(TypeMapper.getDatatypeURI(Integer.class), min, minInclusive, max,
		maxInclusive);
    }

    protected Comparable getNext(Comparable c) {
	return new Integer(((Integer) c).intValue() + 1);
    }

    protected Comparable getPrevious(Comparable c) {
	return new Integer(((Integer) c).intValue() - 1);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	return copyTo(new IntRestriction());
    }
}
