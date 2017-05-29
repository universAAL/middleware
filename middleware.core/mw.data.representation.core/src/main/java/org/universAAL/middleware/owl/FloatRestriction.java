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
import org.universAAL.middleware.rdf.Variable;

/**
 * A {@link TypeExpression} ({@link BoundedValueRestriction}) that contains all
 * literals of type float with a given lower bound and/or upper bound.
 *
 * @author Carsten Stockloew
 */
public final class FloatRestriction extends BoundedValueRestriction {

    /** URI of the data type <i>Float</i>. */
    public static final String DATATYPE_URI = TypeMapper
	    .getDatatypeURI(Float.class);

    // substitutions for Float.MIN_NORMAL
    private static final float FLOAT_SMALLEST_POSITIVE_VALUE = Float
	    .intBitsToFloat(0x00800000);

    /** Standard constructor for exclusive use by serializers. */
    public FloatRestriction() {
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
    public FloatRestriction(float min, boolean minInclusive, float max,
	    boolean maxInclusive) {
	this(new Float(min), minInclusive, new Float(max), maxInclusive);
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
    public FloatRestriction(Float min, boolean minInclusive, Float max,
	    boolean maxInclusive) {
	super(TypeMapper.getDatatypeURI(Float.class), min, minInclusive, max,
		maxInclusive);
    }

    /**
     * Creates a new restriction.
     *
     * @param min
     *            The minimum value, or a {@link Variable} reference, or null if
     *            no minimum is defined.
     * @param minInclusive
     *            True, if the minimum value is included. Ignored, if min is
     *            null.
     * @param max
     *            The maximum value, or a {@link Variable} reference, or null if
     *            no maximum is defined.
     * @param maxInclusive
     *            True, if the maximum value is included. Ignored, if max is
     *            null.
     */
    public FloatRestriction(Object min, boolean minInclusive, Object max,
	    boolean maxInclusive) {
	super(TypeMapper.getDatatypeURI(Float.class), min, minInclusive, max,
		maxInclusive);
    }

    @Override
    protected boolean checkType(Object o) {
	if (o instanceof Float)
	    return true;
	return super.checkType(o);
    }

    @Override
    protected Comparable getNext(Comparable c) {
	return new Float(((Float) c).floatValue()
		+ FLOAT_SMALLEST_POSITIVE_VALUE);
    }

    @Override
    protected Comparable getPrevious(Comparable c) {
	return new Float(((Float) c).floatValue()
		- FLOAT_SMALLEST_POSITIVE_VALUE);
    }

    @Override
    public TypeExpression copy() {
	return copyTo(new FloatRestriction());
    }
}
