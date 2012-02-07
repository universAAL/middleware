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

import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;

public abstract class ComparableRestriction extends TypeRestriction {

    protected static final String XSD_FACET_MIN_INCLUSIVE;
    protected static final String XSD_FACET_MAX_INCLUSIVE;
    protected static final String XSD_FACET_MIN_EXCLUSIVE;
    protected static final String XSD_FACET_MAX_EXCLUSIVE;

    private Comparable min = null;
    private Comparable max = null;
    private boolean minInclusive;
    private boolean maxInclusive;

    static {
	XSD_FACET_MIN_INCLUSIVE = TypeMapper.XSD_NAMESPACE + "minInclusive";
	XSD_FACET_MAX_INCLUSIVE = TypeMapper.XSD_NAMESPACE + "maxInclusive";
	XSD_FACET_MIN_EXCLUSIVE = TypeMapper.XSD_NAMESPACE + "minExclusive";
	XSD_FACET_MAX_EXCLUSIVE = TypeMapper.XSD_NAMESPACE + "maxExclusive";
    }

    protected ComparableRestriction(String datatypeURI) {
	super(datatypeURI);
    }

    protected ComparableRestriction(String datatypeURI, Comparable min,
	    boolean minInclusive, Comparable max, boolean maxInclusive) {
	super(datatypeURI);

	if (min == null && max == null)
	    throw new NullPointerException(
		    "Either min or max must be not null.");

	if (min != null && max != null) {
	    if (min.compareTo(max) > 0)
		throw new IllegalArgumentException(
			"min can not be greater than max.");
	}

	setFacets(min, minInclusive, max, maxInclusive);
    }

    private void setFacets(Comparable min, boolean minInclusive,
	    Comparable max, boolean maxInclusive) {
	setMinFacet(min, minInclusive);
	setMaxFacet(max, maxInclusive);
    }

    private void setMinFacet(Comparable min, boolean minInclusive) {
	if (min != null) {
	    if (minInclusive)
		addConstrainingFacet(XSD_FACET_MIN_INCLUSIVE, min);
	    else
		addConstrainingFacet(XSD_FACET_MIN_EXCLUSIVE, min);
	}
	this.min = min;
	this.minInclusive = minInclusive;
    }

    private void setMaxFacet(Comparable max, boolean maxInclusive) {
	if (max != null) {
	    if (maxInclusive)
		addConstrainingFacet(XSD_FACET_MAX_INCLUSIVE, max);
	    else
		addConstrainingFacet(XSD_FACET_MAX_EXCLUSIVE, max);
	}
	this.max = max;
	this.maxInclusive = maxInclusive;
    }

    /**
     * Helper method to copy Restrictions.
     * 
     * @see org.universAAL.middleware.owl.ClassExpression#copy()
     */
    protected ClassExpression copyTo(ComparableRestriction copy) {
	copy.setFacets(min, minInclusive, max, maxInclusive);
	return copy;
    }

    public Comparable getLowerbound() {
	return min;
    }

    public Comparable getUpperbound() {
	return max;
    }

    protected Comparable getNext(Comparable c) {
	// if (c instanceof Duration)
	// // unfortunately javax.xml.datatype.Duration does not implement
	// Comparable
	// return ((Duration)
	// c).add(TypeMapper.getDataTypeFactory().newDuration(1000));
	// if (c instanceof XMLGregorianCalendar)
	// // unfortunately javax.xml.datatype.XMLGregorianCalendar does not
	// implement Comparable
	// return TypeMapper.getDataTypeFactory().newXMLGregorianCalendar(
	// ((XMLGregorianCalendar) c).getYear(),
	// ((XMLGregorianCalendar) c).getMonth(),
	// ((XMLGregorianCalendar) c).getDay(),
	// ((XMLGregorianCalendar) c).getHour(),
	// ((XMLGregorianCalendar) c).getMinute(),
	// ((XMLGregorianCalendar) c).getSecond(),
	// ((XMLGregorianCalendar) c).getMillisecond()+1,
	// ((XMLGregorianCalendar) c).getTimezone());
	// for xsd:string, xsd:language, xsd:XMLLiteral & xsd:anyURI no next can
	// be determined
	// for Boolean, nobody uses OrderingRestriction
	return null;
    }

    protected Comparable getPrevious(Comparable c) {
	// if (c instanceof Duration)
	// // unfortunately javax.xml.datatype.Duration does not implement
	// Comparable
	// return ((Duration)
	// c).subtract(TypeMapper.getDataTypeFactory().newDuration(1000));
	// if (c instanceof XMLGregorianCalendar)
	// // unfortunately javax.xml.datatype.XMLGregorianCalendar does not
	// implement Comparable
	// return TypeMapper.getDataTypeFactory().newXMLGregorianCalendar(
	// ((XMLGregorianCalendar) c).getYear(),
	// ((XMLGregorianCalendar) c).getMonth(),
	// ((XMLGregorianCalendar) c).getDay(),
	// ((XMLGregorianCalendar) c).getHour(),
	// ((XMLGregorianCalendar) c).getMinute(),
	// ((XMLGregorianCalendar) c).getSecond(),
	// ((XMLGregorianCalendar) c).getMillisecond()-1,
	// ((XMLGregorianCalendar) c).getTimezone());
	// for xsd:string, xsd:language, xsd:XMLLiteral & xsd:anyURI no next can
	// be determined
	// for Boolean, nobody uses OrderingRestriction
	return null;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object o) {
	if (o == null || propURI == null)
	    return;

	if (OWL_WITH_RESTRICTIONS.equals(propURI)) {
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
			// check for correct datatype
			if (getProperty(OWL_ON_DATATYPE).equals(
				TypeMapper.getDatatypeURI(facet.value
					.getClass()))) {

			    // process the facet
			    if (XSD_FACET_MIN_INCLUSIVE.equals(facet.facetURI)) {
				setMinFacet((Comparable) facet.value, true);
			    } else if (XSD_FACET_MIN_EXCLUSIVE
				    .equals(facet.facetURI)) {
				setMinFacet((Comparable) facet.value, false);
			    } else if (XSD_FACET_MAX_INCLUSIVE
				    .equals(facet.facetURI)) {
				setMaxFacet((Comparable) facet.value, true);
			    } else if (XSD_FACET_MAX_EXCLUSIVE
				    .equals(facet.facetURI)) {
				setMaxFacet((Comparable) facet.value, false);
			    }
			}
		    }
		}
	    }
	    return;
	}

	// call super for other properties (or for more general facets)
	super.setProperty(propURI, o);
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object,
     *      Hashtable)
     */
    public boolean hasMember(Object member, Hashtable context) {
	if (member == null)
	    return true;

	if (!(member instanceof Comparable))
	    return false;

	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();

	Object aux = max;
	if (aux != null)
	    if (aux instanceof Variable) {
		Comparable next = getNext((Comparable) member);
		if (maxInclusive) {
		    // we can assign any value greater than member (or event
		    // member itself) to aux so that member is a member of this
		    // restriction
		    // so we try first with the next value and if it cannot be
		    // determined we take member
		    if (next == null)
			next = (Comparable) member;
		} else {
		    // we can assign any value greater than member to aux so
		    // that member is a member of this restriction
		    // so we try with the next value
		    if (next == null)
			return false;
		}
		cloned.put(aux.toString(), next);
	    } else {
		if (maxInclusive) {
		    if (!(aux instanceof Comparable)
			    || ((Comparable) member).compareTo(aux) > 0)
			return false;
		} else {
		    if (!(aux instanceof Comparable)
			    || ((Comparable) member).compareTo(aux) > -1)
			return false;
		}
	    }

	aux = min;
	if (aux != null)
	    if (aux instanceof Variable) {
		Comparable prev = getPrevious((Comparable) member);
		if (minInclusive) {
		    // we can assign any value less than member (or event member
		    // itself) to aux so that member is a member of this
		    // restriction
		    // so we try first with the previous value and if it cannot
		    // be determined we take member
		    if (prev == null)
			prev = (Comparable) member;
		} else {
		    // we can assign any value less than member to aux so that
		    // member is a member of this restriction
		    // so we try with the previous value
		    if (prev == null)
			return false;
		}
		cloned.put(aux.toString(), prev);
	    } else {
		if (minInclusive) {
		    if (aux != null
			    && (!(aux instanceof Comparable) || ((Comparable) aux)
				    .compareTo(member) > 0))
			return false;
		} else {
		    if (!(aux instanceof Comparable)
			    || ((Comparable) aux).compareTo(member) > -1)
			return false;
		}
	    }

	synchronize(context, cloned);
	return true;
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression,
     *      Hashtable)
     */
    public boolean isDisjointWith(ClassExpression other, Hashtable context) {
	if (other instanceof ComparableRestriction) {

	    boolean min1Incl = minInclusive;
	    boolean max1Incl = maxInclusive;
	    boolean min2Incl = ((ComparableRestriction) other).minInclusive;
	    boolean max2Incl = ((ComparableRestriction) other).maxInclusive;

	    Object min1 = Variable.resolveVarRef(min, context);
	    Object max1 = Variable.resolveVarRef(max, context);
	    Object min2 = Variable.resolveVarRef(
		    ((ComparableRestriction) other).min, context);
	    Object max2 = Variable.resolveVarRef(
		    ((ComparableRestriction) other).max, context);

	    return (max1 instanceof Comparable && min2 instanceof Comparable && (((Comparable) max1)
		    .compareTo(min2) < 0 || (((Comparable) max1)
		    .compareTo(min2) == 0 && (!max1Incl || !min2Incl))))
		    || (max2 instanceof Comparable
			    && min1 instanceof Comparable && (((Comparable) max2)
			    .compareTo(min1) < 0 || (((Comparable) max2)
			    .compareTo(min1) == 0 && (!max2Incl || !min1Incl))));
	}

	return false;
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
    public boolean isWellFormed() {
	return restrictions.size() > 0;
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression,
     *      Hashtable)
     */
    public boolean matches(ClassExpression subset, Hashtable context) {
	// TODO: check other ClassExpressions (e.g. Union..)

	if (subset instanceof ComparableRestriction) {
	    ComparableRestriction other = (ComparableRestriction) subset;
	    if (!isWellFormed() || !other.isWellFormed())
		return false;

	    Hashtable cloned = (context == null) ? null : (Hashtable) context
		    .clone();

	    // --- max value ---

	    // note: the below calls to resolveVarRef will shell var refs to the
	    // underlying process parameter
	    // if no corresponding value can be found in the context, otherwise
	    // the associated value will be
	    // returned; if the passed value is not a var ref, then we'll get it
	    // back without any change
	    Object myex = null;
	    Object ex = null;
	    Object myin = null;
	    Object in = null;

	    if (maxInclusive)
		myin = Variable.resolveVarRef(max, cloned);
	    else
		myex = Variable.resolveVarRef(max, cloned);

	    if (other.maxInclusive)
		in = Variable.resolveVarRef(other.max, cloned);
	    else
		ex = Variable.resolveVarRef(other.max, cloned);

	    if (myex != null) {
		if (myex instanceof Variable) {
		    if (ex instanceof Comparable && cloned != null) {
			// myex is a Variable -> toString returns its URI
			cloned.put(myex.toString(), ex);
		    } else if (ex == null && in instanceof Comparable
			    && cloned != null) {
			// any value greater than 'in' can be used as value for
			// 'myex'
			// we try to take the 'next' value to 'in'
			Comparable next = getNext((Comparable) in);
			if (next == null)
			    return false;
			cloned.put(myex.toString(), next);
		    } else
			return false;
		} else if (ex instanceof Variable) {
		    // if the parameter value is <= myex, they match ->
		    // conditional match: parameter value == myex
		    if (cloned == null)
			return false;
		    cloned.put(ex.toString(), myex);
		} else if (ex instanceof Comparable) {
		    if (((Comparable) ex).compareTo(myex) > 0)
			return false;
		} else if (in instanceof Variable && cloned != null) {
		    // any value less than 'myex' can be used as value for 'in'
		    // we try to take the 'previous' value of 'myex'
		    Comparable prev = getPrevious((Comparable) myex);
		    if (prev == null)
			return false;
		    cloned.put(in.toString(), prev);
		} else if (in instanceof Comparable) {
		    if (((Comparable) in).compareTo(myex) >= 0)
			return false;
		} else
		    return false;
	    } else if (myin != null) {
		if (myin instanceof Variable) {
		    if (in instanceof Comparable && cloned != null) {
			// myin is a Variable -> toString returns its URI
			cloned.put(myin.toString(), in);
		    } else if (in == null && ex instanceof Comparable
			    && cloned != null) {
			// any value greater than or equal to the previous value
			// of 'ex' can be used as value for 'myin'
			// we try to take the 'previous' value of 'ex'
			Comparable prev = getPrevious((Comparable) ex);
			if (prev == null)
			    return false;
			cloned.put(myin.toString(), prev);
		    } else
			return false;
		} else if (in instanceof Variable) {
		    // if the parameter value is <= myin, they match ->
		    // conditional match: parameter value == myin
		    if (cloned == null)
			return false;
		    cloned.put(in.toString(), myin);
		} else if (in instanceof Comparable) {
		    if (((Comparable) in).compareTo(myin) > 0)
			return false;
		} else if (ex instanceof Variable && cloned != null) {
		    // any value less than the next value of 'myin' can be used
		    // as value for 'ex'
		    // we try to take the 'next' value of 'myin'
		    Comparable next = getNext((Comparable) myin);
		    if (next == null)
			return false;
		    cloned.put(ex.toString(), next);
		} else if (ex instanceof Comparable) {
		    // the maximum allowed value for ex is the next value of
		    // myin so that all values that are member of 'other' are
		    // also member of 'this'
		    Comparable next = getNext((Comparable) myin);
		    if (next == null)
			next = (Comparable) myin;
		    if (((Comparable) ex).compareTo(next) > 0)
			return false;
		} else
		    return false;
	    } else if (ex != null || in != null)
		return false;

	    // --- min value ---

	    // note: the below calls to resolveVarRef will shell var refs to the
	    // underlying process parameter
	    // if no corresponding value can be found in the context, otherwise
	    // the associated value will be
	    // returned; if the passed value is not a var ref, then we'll get it
	    // back without any change

	    myex = null;
	    ex = null;
	    myin = null;
	    in = null;

	    if (minInclusive)
		myin = Variable.resolveVarRef(min, cloned);
	    else
		myex = Variable.resolveVarRef(min, cloned);

	    if (other.minInclusive)
		in = Variable.resolveVarRef(other.min, cloned);
	    else
		ex = Variable.resolveVarRef(other.min, cloned);

	    if (myex != null) {
		if (myex instanceof Variable) {
		    if (ex instanceof Comparable && cloned != null) {
			// myex is a Variable -> toString returns its URI
			cloned.put(myex.toString(), ex);
		    } else if (ex == null && in instanceof Comparable
			    && cloned != null) {
			// any value less than 'in' can be used as value for
			// 'myex'
			// we try to take the 'previous' value of 'in'
			Comparable prev = getPrevious((Comparable) in);
			if (prev == null)
			    return false;
			cloned.put(myex.toString(), prev);
		    } else
			return false;
		} else if (ex instanceof Variable) {
		    // if the parameter value is >= myex, they match ->
		    // conditional match: parameter value == myex
		    if (cloned == null)
			return false;
		    cloned.put(ex.toString(), myex);
		} else if (ex instanceof Comparable) {
		    if (((Comparable) myex).compareTo(ex) > 0)
			return false;
		} else if (in instanceof Variable && cloned != null) {
		    // any value greater than 'myex' can be used as value for
		    // 'in'
		    // we try to take the 'next' value of 'myex'
		    Comparable next = getNext((Comparable) myex);
		    if (next == null)
			return false;
		    cloned.put(in.toString(), next);
		} else if (in instanceof Comparable) {
		    // the minimum allowed value for in is the next value of
		    // myex so that all values that are member of 'other' are
		    // also member of 'this'
		    Comparable next = getNext((Comparable) myex);
		    if (next == null)
			next = (Comparable) myex;
		    if (((Comparable) next).compareTo(in) > 0)
			return false;
		} else
		    return false;
	    } else if (myin != null) {
		if (myin instanceof Variable) {
		    if (in instanceof Comparable && cloned != null) {
			// myin is a Variable -> toString returns its URI
			cloned.put(myin.toString(), in);
		    } else if (in == null && ex instanceof Comparable
			    && cloned != null) {
			// any value less than or equal to the next value of
			// 'ex' can be used as value for 'myin'
			// we try to take the 'next' value of 'ex'
			Comparable next = getNext((Comparable) ex);
			if (next == null)
			    return false;
			cloned.put(myin.toString(), next);
		    } else
			return false;
		} else if (in instanceof Variable) {
		    // if the parameter value is >= myin, they match ->
		    // conditional match: parameter value == myin
		    if (cloned == null)
			return false;
		    cloned.put(in.toString(), myin);
		} else if (in instanceof Comparable) {
		    if (((Comparable) myin).compareTo(in) > 0)
			return false;
		} else if (ex instanceof Variable && cloned != null) {
		    // any value greater than the previous value of 'myin' can
		    // be used as value for 'ex'
		    // we try to take the 'previous' value of 'myin'
		    Comparable prev = getPrevious((Comparable) myin);
		    if (prev == null)
			return false;
		    cloned.put(ex.toString(), prev);
		} else if (ex instanceof Comparable) {
		    // the minimum allowed value for ex is the previous value of
		    // myin so that all values that are member of 'other' are
		    // also member of 'this'
		    Comparable prev = getPrevious((Comparable) myin);
		    if (prev == null)
			prev = (Comparable) myin;
		    if (((Comparable) prev).compareTo(ex) > 0)
			return false;
		} else
		    return false;
	    } else if (ex != null || in != null)
		return false;

	    synchronize(context, cloned);
	    return true;
	}

	return false;
    }
}
