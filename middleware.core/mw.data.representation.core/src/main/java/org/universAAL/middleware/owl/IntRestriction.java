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
package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.TypeMapper;

/**
 * A {@link TypeExpression} ({@link BoundedValueRestriction}) that contains all
 * literals of type int with a given lower bound and/or upper bound.
 * <p>
 * For example, <code>IntRestriction(0, true, 4, false)</code> contains exactly
 * the literals 0,1,2, and 3.
 * 
 * @author Carsten Stockloew
 */
public final class IntRestriction extends BoundedValueRestriction {

    /** URI of the data type <i>Integer</i>. */
    public static final String DATATYPE_URI = TypeMapper
	    .getDatatypeURI(Integer.class);

    /** Standard constructor for exclusive use by serializers. */
    public IntRestriction() {
	super(DATATYPE_URI);
    }

    /**
     * Creates a new restriction.
     * 
     * @param min
     *            The minimum value, or null if no minimum is defined.
     * @param minInclusive
     *            True, if the minimum value is included. Ignored, if min is
     *            null.
     * @param max
     *            The maximum value, or null if no maximum is defined.
     * @param maxInclusive
     *            True, if the maximum value is included. Ignored, if max is
     *            null.
     */
    public IntRestriction(int min, boolean minInclusive, int max,
	    boolean maxInclusive) {
	this(Integer.valueOf(min), minInclusive, Integer.valueOf(max), maxInclusive);
    }

    /**
     * Creates a new restriction.
     * 
     * @param min
     *            The minimum value, or null if no minimum is defined.
     * @param minInclusive
     *            True, if the minimum value is included. Ignored, if min is
     *            null.
     * @param max
     *            The maximum value, or null if no maximum is defined.
     * @param maxInclusive
     *            True, if the maximum value is included. Ignored, if max is
     *            null.
     */
    public IntRestriction(Integer min, boolean minInclusive, Integer max,
	    boolean maxInclusive) {
	super(TypeMapper.getDatatypeURI(Integer.class), min, minInclusive, max,
		maxInclusive);
    }

    /** @see BoundedValueRestriction#getNext(Comparable) */
    protected Comparable getNext(Comparable c) {
	return Integer.valueOf(((Integer) c).intValue() + 1);
    }

    /** @see BoundedValueRestriction#getPrevious(Comparable) */
    protected Comparable getPrevious(Comparable c) {
	return Integer.valueOf(((Integer) c).intValue() - 1);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	return copyTo(new IntRestriction());
    }
}
