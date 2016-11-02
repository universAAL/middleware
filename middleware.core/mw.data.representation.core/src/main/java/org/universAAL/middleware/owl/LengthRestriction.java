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

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.util.MatchLogEntry;
import org.universAAL.middleware.xsd.NonNegativeInteger;

/**
 * Base class for all {@link TypeRestriction}s that define a restriction on the
 * length of a value of a given data type. Implementation of XSD Value
 * Restrictions: it contains all literals that meet the specified conditions.
 * These conditions are either:
 * <ol>
 * <li>length</li>
 * <li>min length</li>
 * <li>max length</li>
 * </ol>
 * 
 * It is possible to define a condition on the minimum length and on the maximum
 * length in the same {@link LengthRestriction}. If minimum and maximum length
 * are equal, only the <i>length</i> facet should be set.
 * 
 * @author Carsten Stockloew
 */
public abstract class LengthRestriction extends TypeRestriction {

    /** URI for the facet xsd:length. */
    protected static final String XSD_FACET_LENGTH;

    /** URI for the facet xsd:minLength. */
    protected static final String XSD_FACET_MIN_LENGTH;

    /** URI for the facet xsd:maxLength. */
    protected static final String XSD_FACET_MAX_LENGTH;

    /** The length value, or null if no length is defined. */
    private NonNegativeInteger len = null;

    /** The minLength value, or null if no minLength is defined. */
    private NonNegativeInteger min = null;

    /** The maxLength value, or null if no maxLength is defined. */
    private NonNegativeInteger max = null;

    static {
	XSD_FACET_LENGTH = TypeMapper.XSD_NAMESPACE + "length";
	XSD_FACET_MIN_LENGTH = TypeMapper.XSD_NAMESPACE + "minLength";
	XSD_FACET_MAX_LENGTH = TypeMapper.XSD_NAMESPACE + "maxLength";
    }

    /**
     * Standard constructor.
     * 
     * @param datatypeURI
     *            URI of the data type for which this restriction is defined.
     *            Must be one of the supported data types.
     */
    protected LengthRestriction(String datatypeURI) {
	super(datatypeURI);
    }

    /**
     * Set the length.
     * 
     * @param len
     *            the length.
     */
    public void setLen(int len) {
	setLen(new NonNegativeInteger(len));
    }

    /**
     * Set the length.
     * 
     * @param len
     *            the length.
     */
    public void setLen(NonNegativeInteger len) {
	// TODO: check with other facets
	if (len != null)
	    addConstrainingFacet(XSD_FACET_MIN_LENGTH, len);
	this.len = len;
    }

    /**
     * Set the minimum length.
     * 
     * @param min
     *            the minimum length.
     */
    public void setMin(int min) {
	setMin(new NonNegativeInteger(min));
    }

    /**
     * Set the minimum length.
     * 
     * @param min
     *            the minimum length.
     */
    public void setMin(NonNegativeInteger min) {
	// TODO: check with other facets
	if (min != null)
	    addConstrainingFacet(XSD_FACET_MIN_LENGTH, min);
	this.min = min;
    }

    /**
     * Set the maximum length.
     * 
     * @param max
     *            the maximum length.
     */
    public void setMax(int max) {
	setMax(new NonNegativeInteger(max));
    }

    /**
     * Set the maximum length.
     * 
     * @param max
     *            the maximum length.
     */
    public void setMax(NonNegativeInteger max) {
	// TODO: check with other facets
	if (max != null)
	    addConstrainingFacet(XSD_FACET_MIN_LENGTH, max);
	this.max = max;
    }

    /**
     * Get the length.
     * 
     * @return the length, or null if not defined.
     */
    public NonNegativeInteger getLen() {
	return len;
    }

    /**
     * Get the minimum length.
     * 
     * @return the minimum length, or null if not defined.
     */
    public NonNegativeInteger getMin() {
	return min;
    }

    /**
     * Get the maximum length.
     * 
     * @return the maximum length, or null if not defined.
     */
    public NonNegativeInteger getMax() {
	return max;
    }

    /**
     * Helper method to copy Restrictions.
     * 
     * @see org.universAAL.middleware.owl.TypeExpression#copy()
     */
    /**
     * Copy the facets to a different {@link LengthRestriction}.
     * 
     * @param copy
     *            The object to which to copy the facets.
     * @return the value given as parameter, but with the restrictions copied.
     * @see org.universAAL.middleware.owl.TypeExpression#copy()
     */
    protected TypeExpression copyTo(LengthRestriction copy) {
	copy.setLen(len);
	copy.setMin(min);
	copy.setMax(max);
	return copy;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#isWellFormed() */
    @Override
    public boolean isWellFormed() {
	return restrictions.size() > 0;
    }

    /**
     * @see org.universAAL.middleware.owl.TypeExpression#hasMember(Object,
     *      HashMap, int, List)
     */
    @Override
    public boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	// TODO: extend with variables

	// check the length
	int memlen = member == null ? 0 : member.toString().length();
	if (len != null) {
	    if (len.intValue() != memlen) {
		return false;
	    } else {
		// check other facets
		return super.hasMember(member, context, ttl, log);
	    }
	}

	if (min != null)
	    if (min.intValue() > memlen)
		return false;
	if (max != null)
	    if (max.intValue() < memlen)
		return false;

	// check other facets
	return super.hasMember(member, context, ttl, log);
    }

    /**
     * Not supported. Always returns false.
     */
    @Override
    public boolean matches(TypeExpression subset, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * Not supported. Always returns false.
     */
    @Override
    public boolean isDisjointWith(TypeExpression other, HashMap context,
	    int ttl, List<MatchLogEntry> log) {
	// TODO Auto-generated method stub
	return false;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public boolean setProperty(String propURI, Object o) {
	if (o == null || propURI == null)
	    return false;

	if (PROP_OWL_WITH_RESTRICTIONS.equals(propURI)) {
	    if (min == null && max == null) {
		// values are not set yet
		// retrieve the values from the given object and set them
		// o must be a list
		if (o instanceof List) {
		    // each element of the list is a constraining facet and is
		    // one (anonymous) Resource with only one property
		    ListIterator it = ((List) o).listIterator();
		    Facet facet;

		    while ((facet = iterate(it)) != null) {
			// process the facet
			if (XSD_FACET_LENGTH.equals(facet.facetURI)) {
			    setLen((NonNegativeInteger) facet.value);
			} else if (XSD_FACET_MIN_LENGTH.equals(facet.facetURI)) {
			    setMin((NonNegativeInteger) facet.value);
			} else if (XSD_FACET_MAX_LENGTH.equals(facet.facetURI)) {
			    setMax((NonNegativeInteger) facet.value);
			} else {
			    super.setFacet(facet);
			}
		    }
		    return true;
		}
	    }
	    return false;
	}

	// call super for other properties (or for more general facets)
	return super.setProperty(propURI, o);
    }
}
