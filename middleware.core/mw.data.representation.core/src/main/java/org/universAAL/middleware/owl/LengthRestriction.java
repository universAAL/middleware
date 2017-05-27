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

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;
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

    /**
     * The length value, or null if no length is defined. Must be either a
     * {@link NonNegativeInteger} or a {@link Variable} reference.
     */
    private Object len = null;

    /**
     * The minLength value, or null if no minLength is defined. Must be either a
     * {@link NonNegativeInteger} or a {@link Variable} reference.
     */
    private Object min = null;

    /**
     * The maxLength value, or null if no maxLength is defined. Must be either a
     * {@link NonNegativeInteger} or a {@link Variable} reference.
     */
    private Object max = null;

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
    public boolean setLen(int len) {
	return setLen(new NonNegativeInteger(len));
    }

    /**
     * Set the length.
     * 
     * @param len
     *            the length.
     */
    public boolean setLen(NonNegativeInteger len) {
	if (len == null)
	    throw new NullPointerException();

	if (this.len != null || min != null || max != null) {
	    LogUtils.logError(SharedResources.moduleContext, LengthRestriction.class, "setLen",
		    "Length for LengthRestriction already set.");
	    return false;
	}

	addConstrainingFacet(XSD_FACET_LENGTH, len);
	this.len = len;
	return true;
    }

    /**
     * Set the length.
     * 
     * @param len
     *            the length.
     */
    public boolean setLen(Resource len) {
	if (!Variable.isVarRef(len)) {
	    throw new IllegalArgumentException("Cannot set len for a resource other than Variable: " + len);
	}
	if (this.len != null || min != null || max != null) {
	    LogUtils.logError(SharedResources.moduleContext, LengthRestriction.class, "setLen",
		    "Setting length for LengthRestriction not possible, it already contains a length restriction.");
	    return false;
	}

	addConstrainingFacet(XSD_FACET_LENGTH, len);
	this.len = len;
	return true;
    }

    /**
     * Set the length.
     * 
     * @param len
     *            the length.
     */
    private boolean setLen(Object len) {
	if (len instanceof NonNegativeInteger)
	    return setLen((NonNegativeInteger) len);
	if (len instanceof Resource)
	    return setLen((Resource) len);

	throw new IllegalArgumentException("Cannot set len for: " + len);
    }

    /**
     * Set the minimum length.
     * 
     * @param min
     *            the minimum length.
     */
    public boolean setMin(int min) {
	return setMin(new NonNegativeInteger(min));
    }

    /**
     * Set the minimum length.
     * 
     * @param min
     *            the minimum length.
     */
    public boolean setMin(NonNegativeInteger min) {
	if (min == null)
	    throw new NullPointerException();

	if (len != null || this.min != null) {
	    LogUtils.logError(SharedResources.moduleContext, LengthRestriction.class, "setMin",
		    "Length for LengthRestriction already set.");
	    return false;
	}
	
	if (max != null && max instanceof NonNegativeInteger) {
	    if (min.compareTo((NonNegativeInteger) max) > 0)
		throw new IllegalArgumentException(
			"Cannot set a min value that is greater than the already set max value");
	}

	addConstrainingFacet(XSD_FACET_MIN_LENGTH, min);
	this.min = min;
	return true;
    }
    
    /**
     * Set the minimum length.
     * 
     * @param min
     *            the minimum length.
     */
    public boolean setMin(Resource min) {
	if (!Variable.isVarRef(len)) {
	    throw new IllegalArgumentException("Cannot set min len for a resource other than Variable: " + min);
	}
	if (len != null || this.min != null) {
	    LogUtils.logError(SharedResources.moduleContext, LengthRestriction.class, "setMin",
		    "Setting length for LengthRestriction not possible, it already contains a length restriction.");
	    return false;
	}

	addConstrainingFacet(XSD_FACET_MIN_LENGTH, min);
	this.min = min;
	return true;
    }

    /**
     * Set the minimum length.
     * 
     * @param min
     *            the minimum length.
     */
    private boolean setMin(Object min) {
	if (min instanceof NonNegativeInteger)
	    return setMin((NonNegativeInteger) min);
	if (min instanceof Resource)
	    return setMin((Resource) min);

	throw new IllegalArgumentException("Cannot set min for: " + min);
    }
    
    /**
     * Set the maximum length.
     * 
     * @param max
     *            the maximum length.
     */
    public boolean setMax(int max) {
	return setMax(new NonNegativeInteger(max));
    }

    /**
     * Set the maximum length.
     * 
     * @param max
     *            the maximum length.
     */
    public boolean setMax(NonNegativeInteger max) {
	if (max == null)
	    throw new NullPointerException();

	if (len != null || this.max != null) {
	    LogUtils.logError(SharedResources.moduleContext, LengthRestriction.class, "setMax",
		    "Length for LengthRestriction already set.");
	    return false;
	}
	
	if (min != null && min instanceof NonNegativeInteger) {
	    if (((NonNegativeInteger) min).compareTo(max) > 0)
		throw new IllegalArgumentException(
			"Cannot set a max value that is smaller than the already set min value");
	}

	addConstrainingFacet(XSD_FACET_MAX_LENGTH, max);
	this.max = max;
	return true;
    }

    /**
     * Set the maximum length.
     * 
     * @param max
     *            the maximum length.
     */
    public boolean setMax(Resource max) {
	if (!Variable.isVarRef(max)) {
	    throw new IllegalArgumentException("Cannot set max len for a resource other than Variable: " + max);
	}
	if (len != null || this.max != null) {
	    LogUtils.logError(SharedResources.moduleContext, LengthRestriction.class, "setMax",
		    "Setting length for LengthRestriction not possible, it already contains a length restriction.");
	    return false;
	}

	addConstrainingFacet(XSD_FACET_MAX_LENGTH, max);
	this.max = max;
	return true;
    }

    /**
     * Set the maximum length.
     * 
     * @param max
     *            the maximum length.
     */
    private boolean setMax(Object max) {
	if (max instanceof NonNegativeInteger)
	    return setMax((NonNegativeInteger) max);
	if (max instanceof Resource)
	    return setMax((Resource) max);

	throw new IllegalArgumentException("Cannot set max for: " + max);
    }

    /**
     * Get the length.
     * 
     * @return the length, or null if not defined. Can also be a
     *         {@link Variable} reference.
     */
    public Object getLen() {
	return len;
    }

    /**
     * Get the minimum length.
     * 
     * @return the minimum length, or null if not defined. Can also be a
     *         {@link Variable} reference.
     */
    public Object getMin() {
	return min;
    }

    /**
     * Get the maximum length.
     * 
     * @return the maximum length, or null if not defined. Can also be a
     *         {@link Variable} reference.
     */
    public Object getMax() {
	return max;
    }

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
	return super.copyTo(copy);
    }

    @Override
    public boolean isWellFormed() {
	return restrictions.size() > 0;
    }

    @Override
    protected Comparable getNext(Comparable c) {
	return new NonNegativeInteger(((NonNegativeInteger) c).intValue() + 1);
    }

    @Override
    protected Comparable getPrevious(Comparable c) {
	return new NonNegativeInteger(((NonNegativeInteger) c).intValue() - 1);
    }

    /**
     * Calculate the length of a member. The default implementation just returns
     * the length of the String value. Sub classes may override this method.
     * 
     * @param member
     *            the member for which to calculate the length.
     * @return the length as NonNegativeInteger or a {@link Variable} if the
     *         member is a {@link Variable}.
     */
    protected Object getMemberLen(Object member) {
	if (member instanceof Variable)
	    return member;
	return new NonNegativeInteger(member.toString().length());
    }
    
    @Override
    protected Object getMemberValueToCheck(Object member) {
	return getMemberLen(member);
    }

    @Override
    public boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	HashMap cloned = (context == null) ? null : (HashMap) context.clone();

	Object min = this.min;
	Object max = this.max;
	if (len != null) {
	    min = len;
	    max = len;
	}

	if (min != null || max != null)
	    if (!hasMember(member, cloned, ttl, log, min, true, max, true))
		return false;

	if (!super.hasMember(member, cloned, ttl, log))
	    return false;

	synchronize(context, cloned);
	return true;
    }

    @Override
    public boolean matches(TypeExpression subset, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	// ttl =
	checkTTL(ttl);
	// TODO: check other ClassExpressions (e.g. Union..)

	if (!(subset instanceof LengthRestriction))
	    return false;

	LengthRestriction other = (LengthRestriction) subset;
	if (!isWellFormed() || !other.isWellFormed())
	    return false;

	Object min = this.min;
	Object max = this.max;
	if (len != null) {
	    min = len;
	    max = len;
	}

	Object other_min = other.min;
	Object other_max = other.max;
	if (other.len != null) {
	    other_min = other.len;
	    other_max = other.len;
	}

	return matches(other_min, true, other_max, true, context, ttl, log, min, true, max, true);
    }

    /**
     * Not supported. Always returns false.
     */
    @Override
    public boolean isDisjointWith(TypeExpression other, HashMap context,
	    int ttl, List<MatchLogEntry> log) {
	return false;
    }

    @Override
    public boolean setProperty(String propURI, Object o) {
	if (o == null || propURI == null)
	    return false;

	if (PROP_OWL_WITH_RESTRICTIONS.equals(propURI)) {
	    if (min == null && max == null && len == null) {
		// values are not set yet
		// retrieve the values from the given object and set them
		// o must be a list
		if (o instanceof List) {
		    // each element of the list is a constraining facet and is
		    // one (anonymous) Resource with only one property
		    ListIterator<?> it = ((List<?>) o).listIterator();
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
