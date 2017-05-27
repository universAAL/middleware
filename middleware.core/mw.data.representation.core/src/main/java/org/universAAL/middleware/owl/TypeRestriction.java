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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;
import org.universAAL.middleware.util.MatchLogEntry;

/**
 * Base class for all {@link TypeExpression}s that impose a restriction on a
 * data type. As data types in OWL are taken from XML specification, the
 * restrictions are defined as <i>constraining facets</i>.
 * <p>
 * Most of the type restrictions are defined for literals, with only a few
 * exceptions that define a restriction on an individual or a URI.
 * 
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#defn-coss">XML Schema
 *      Datatypes</a>
 * 
 * @author Carsten Stockloew
 */
/*-
 * Hierarchy:
 * 	TypeRestriction (abstract)
 * 		(Facets: pattern, enumeration(?), whiteSpace)
 * 		BooleanRestriction
 * 		ComparableRestriction (abstract)
 * 			(Facets: maxInclusive, maxExclusive, minInclusive, ..)
 * 			FloatRestriction
 * 			TimeRestriction
 * 			DateRestriction
 * 			...
 * 		LengthRestriction (abstract)
 * 			(Facets: length, minLength, maxLength)
 * 			StringRestriction
 * 			Base64BinaryRestriction
 * 			...
 */
public abstract class TypeRestriction extends TypeExpression {

    /**
     * URI for owl:onDatatype; it holds the data type for which this restriction
     * is defined.
     */
    public static final String PROP_OWL_ON_DATATYPE = OWL_NAMESPACE
	    + "onDatatype";

    /**
     * URI for owl:withRestrictions; it holds the list of restrictions (facets).
     */
    public static final String PROP_OWL_WITH_RESTRICTIONS = OWL_NAMESPACE
	    + "withRestrictions";

    /** URI for the facet xsd:pattern. */
    protected static final String XSD_FACET_PATTERN = TypeMapper.XSD_NAMESPACE
	    + "pattern";

    /**
     * The pattern (regular expression) if the facet 'pattern' is defined for
     * this restriction, or null if no pattern is defined.
     */
    private Pattern pattern = null;

    /**
     * The list of restrictions. This list is set as
     * {@link #PROP_OWL_WITH_RESTRICTIONS}.
     */
    protected ArrayList<Resource> restrictions = new ArrayList<Resource>();

    /** Internal representation of a facet. */
    protected static class Facet {
	/** URI of the facet. */
	String facetURI;

	/** Value of the facet. */
	Object value;
    }

    /**
     * Standard constructor.
     * 
     * @param datatypeURI
     *            URI of the data type for which this restriction is defined.
     *            Must be one of the supported data types.
     * @see TypeMapper
     */
    protected TypeRestriction(String datatypeURI) {
	super.setProperty(PROP_OWL_ON_DATATYPE, new Resource(datatypeURI));
	super.setProperty(PROP_OWL_WITH_RESTRICTIONS, restrictions);
    }

    /**
     * Get the data type for which this restriction is defined.
     * 
     * @return URI of the data type.
     */
    public String getTypeURI() {
	return ((Resource) getProperty(PROP_OWL_ON_DATATYPE)).getURI();
    }

    /**
     * Iterate over a list of facets while checking the value; invalid elements
     * are skipped. An element of the list is not a valid facet if it is not a
     * resource or if it has not exactly one property. This one property is the
     * facet URI.
     * 
     * @param it
     *            An iterator for a list of facets.
     * @return the {@link Facet}.
     */
    protected Facet iterate(ListIterator<?> it) {
	while (it.hasNext()) {
	    Object o = it.next();
	    if (!(o instanceof Resource))
		// TODO: log message?
		continue;
	    Resource r = (Resource) o;
	    if (r.numberOfProperties() != 1)
		// TODO: log message?
		continue;
	    java.util.Enumeration<?> e = r.getPropertyURIs();
	    String propURI = (String) e.nextElement();

	    Facet f = new Facet();
	    f.facetURI = propURI;
	    f.value = r.getProperty(propURI);

	    return f;
	}
	return null;
    }

    /**
     * Add a new facet to the list of facets for this restriction.
     * 
     * @param facetURI
     *            URI of the facet.
     * @param value
     *            Value of the facet.
     */
    protected void addConstrainingFacet(String facetURI, Object value) {
	Resource r = new Resource();
	r.setProperty(facetURI, value);
	restrictions.add(r);
    }

    /**
     * Copy the facets to a different {@link TypeRestriction}.
     * 
     * @param copy
     *            The object to which to copy the facets.
     * @return the value given as parameter, but with the restrictions copied.
     * @see org.universAAL.middleware.owl.TypeExpression#copy()
     */
    protected TypeExpression copyTo(TypeRestriction copy) {
	if (pattern != null)
	    copy.setPattern(pattern);
	return copy;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getNamedSuperclasses() */
    @Override
    public String[] getNamedSuperclasses() {
	return new String[0];
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getUpperEnumeration() */
    @Override
    public Object[] getUpperEnumeration() {
	return new Object[0];
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    @Override
    public boolean setProperty(String propURI, Object o) {
	if (o == null || propURI == null)
	    return false;

	// do not handle our properties
	if (PROP_OWL_ON_DATATYPE.equals(propURI)) {
	    if (o instanceof Resource
		    && getTypeURI().equals(((Resource) o).getURI()))
		return true;
	    return false;
	} else if (PROP_OWL_WITH_RESTRICTIONS.equals(propURI))
	    return false;

	return super.setProperty(propURI, o);
    }

    /**
     * Set a pattern (regular expression). This pattern is added to the list of
     * constraining facets for this restriction.
     * 
     * @param pattern
     *            The pattern as defined in {@link Pattern}.
     * @return true, if the pattern could be set, i.e. the pattern must be valid
     *         and no other pattern was set before.
     */
    public boolean setPattern(String pattern) {
	if (this.pattern != null)
	    return false;

	Pattern compiledPattern;
	try {
	    compiledPattern = Pattern.compile(pattern);
	} catch (PatternSyntaxException e) {
	    // TODO: log message?
	    return false;
	}

	return setPattern(compiledPattern);
    }

    /**
     * Set a pattern (regular expression). This pattern is added to the list of
     * constraining facets for this restriction.
     * 
     * @param pattern
     *            The pattern as defined in {@link Pattern}.
     * @return true, if the pattern could be set, i.e. the pattern must be valid
     *         and no other pattern was set before.
     */
    private boolean setPattern(Pattern compiledPattern) {
	addConstrainingFacet(XSD_FACET_PATTERN, pattern);
	this.pattern = compiledPattern;
	return true;
    }

    @Override
    /**
     * @see org.universAAL.middleware.owl.TypeExpression#hasMember(Object,
     *      HashMap, int, List)
     */
    public boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	// test only the pattern constraining facet, everything else should be
	// checked in subclasses

	if (pattern == null) {
	    // there is no pattern defined
	    return true;
	}

	String m = member == null ? "" : member.toString();
	Matcher matcher = pattern.matcher(m);
	if (matcher.matches())
	    return true;

	return false;
    }

    /**
     * Set a facet. For this class, only 'pattern' is allowed.
     * 
     * @param facet
     *            The facet.
     */
    protected void setFacet(Facet facet) {
	if (XSD_FACET_PATTERN.equals(facet.facetURI)) {
	    setPattern((String) facet.value);
	}
    }
    
    /**
     * Returns the next element, i.e. the value given as parameter plus the
     * smallest possible value that can be represented for the data type. For
     * example, if the data type is int getNext(x) would return x+1. Sub classes
     * should override this method.
     * 
     * @param c
     *            A value for which to get the next value.
     * 
     * @return the next value.
     */
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

    /**
     * Returns the previous element, i.e. the value given as parameter minus the
     * smallest possible value that can be represented for the data type. For
     * example, if the data type is int getPrevious(x) would return x-1. Sub
     * classes should override this method.
     * 
     * @param c
     *            A value for which to get the previous value.
     * 
     * @return the previous value.
     */
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

    protected Comparable resolveVarByGreaterEqual(Variable v,
	    Comparable lowerbound, boolean canBeEqual, HashMap context) {
	Comparable resolution = canBeEqual ? lowerbound : getNext(lowerbound);
	// consider that we might fail because getNext() does not work always
	if (resolution != null)
	    // add the variable resolution to the context
	    context.put(v.toString(), resolution);
	return resolution;
    }

    protected Comparable resolveVarByLessEqual(Variable v, Comparable upperbound,
	    boolean canBeEqual, HashMap context) {
	Comparable resolution = canBeEqual ? upperbound
		: getPrevious(upperbound);
	// consider that we might fail because getPrevious() does not work
	// always
	if (resolution != null)
	    // add the variable resolution to the context
	    context.put(v.toString(), resolution);
	return resolution;
    }

    /**
     * Calculate the value for the member that needs to be checked during
     * {@link #hasMember(Object)}. Sub classes may override this method.
     * 
     * @param member
     *            the member.
     * @return the value to check.
     */
    protected Object getMemberValueToCheck(Object member) {
	return member;
    }
    
    protected boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log, Object min, boolean minInclusive, Object max, boolean maxInclusive) {
	// ttl =
	checkTTL(ttl);
	if (member == null)
	    return true;

	member = Variable.resolveVarRef(member, context);

	// get the value to be checked against the lower- and upperbounds
	Object valueToCheck = getMemberValueToCheck(member);
	if (!(valueToCheck instanceof Comparable))
	    return false;

	if (valueToCheck instanceof Variable)
	    // check if there is any value already assumed for it in the given
	    // context
	    valueToCheck = Variable.resolveVarRef(valueToCheck, context);
	if (!(valueToCheck instanceof Variable)
		&& !(valueToCheck instanceof Comparable))
	    return false;

	Object lowerBound = min;
	Object upperBound = max;

	// also, any of upperBound / lowerBound might be a variable => we must
	// check the
	// context if they are already bound to any value; if yes, the value
	// must be an instance of Comparable
	lowerBound = Variable.resolveVarRef(lowerBound, context);
	upperBound = Variable.resolveVarRef(upperBound, context);
	if ((!(lowerBound instanceof Variable) && !(lowerBound instanceof Comparable))
		|| (!(upperBound instanceof Variable) && !(upperBound instanceof Comparable)))
	    return false;

	// it is still possible that any of upperBound, lowerBound, or
	// valueToCheck is a variable, if the variables were not conditioned
	// previously
	// => it is possible that we can suggest a conditional match, e.g.,
	// there is a match if var-1 is set to value-1 but this will be possible
	// if context is not null AND not all the three are variables
	if (context == null) {
	    if (valueToCheck instanceof Variable
		    || lowerBound instanceof Variable
		    || upperBound instanceof Variable)
		return false;
	} else if (valueToCheck instanceof Variable
		&& lowerBound instanceof Variable
		&& upperBound instanceof Variable)
	    return false;

	// any condition must be stored in the "context", but we might have to
	// add more than one condition
	// => we'd better clone the context and manipulate only the clone until
	// we are sure that the conditions will lead to a match
	HashMap cloned = (context == null) ? null : (HashMap) context.clone();

	// check the conditions; this means:
	//
	// a) "valueToCheck < upperBound" must hold in order for the 'member' to
	// .. be a member of this BoundingValueRestriction
	//
	// b) "valueToCheck == upperBound" is also valid, if
	// .. "maxInclusive == true"
	//
	// c) "valueToCheck > lowerBound" must hold in order for the 'member' to
	// .. be a member of this BoundingValueRestriction
	//
	// d) "valueToCheck == lowerBound" is also valid, if
	// .. "minInclusive == true"
	//
	// however, we must differentiate between different combinations of
	// instances of Variable and Comparable
	if (upperBound instanceof Variable) {
	    // because upperBound is a variable, let's call it in all the
	    // comments below "upperBoundVar"

	    // we have upperBoundVar => possible cases for the other two are:
	    //
	    // i) valueToCheck a Variable but lowerBound a Comparable
	    // ii) valueToCheck a Comparable but lowerBound a Variable
	    // iii) both valueToCheck and lowerBound are instances of Comparable
	    if (valueToCheck instanceof Variable) {
		// we are in case 'i)' => we can use lowerBound to suggest a
		// value for valueToCheck
		valueToCheck = resolveVarByGreaterEqual(
			(Variable) valueToCheck, (Comparable) lowerBound,
			minInclusive, cloned);
		if (valueToCheck == null)
		    // deadend
		    return false;
	    } else if (lowerBound instanceof Variable) {
		// we are in case 'ii)' => we can use valueToCheck to suggest a
		// value for lowerBound
		lowerBound = resolveVarByLessEqual((Variable) lowerBound,
			(Comparable) valueToCheck, minInclusive, cloned);
		if (lowerBound == null)
		    // deadend
		    return false;
	    } else if (((Comparable) valueToCheck).compareTo(lowerBound) < 0
		    || (!minInclusive && ((Comparable) valueToCheck)
			    .compareTo(lowerBound) == 0))
		// we are in case 'iii)' but the conditions 'c)' and 'd)' do not
		// hold
		return false;

	    // at this place, we can be sure that valueToCheck is an instance of
	    // Comparable => the conditions 'a)' and 'b)' will hold if we
	    // assume a value greater than valueToCheck for upperBoundVar
	    upperBound = resolveVarByGreaterEqual((Variable) upperBound,
		    (Comparable) valueToCheck, maxInclusive, cloned);
	    if (upperBound == null)
		// deadend
		return false;
	} else if (lowerBound instanceof Variable) {
	    // at this place, we can be sure that upperBound is an instance of
	    // Comparable & lowerBound an instance of Variable => we just have
	    // to differentiate the cases for valueToCheck
	    if (valueToCheck instanceof Variable) {
		// we can use upperBound to suggest a value for valueToCheck
		valueToCheck = resolveVarByLessEqual((Variable) valueToCheck,
			(Comparable) upperBound, maxInclusive, cloned);
		if (valueToCheck == null)
		    // deadend
		    return false;
	    } else if (((Comparable) valueToCheck).compareTo(upperBound) > 0
		    || (!maxInclusive && ((Comparable) valueToCheck)
			    .compareTo(upperBound) == 0))
		// one of the conditions 'a)' / 'b)' does not hold
		return false;

	    // here, valueToCheck is certainly an instance of Comparable
	    // now we can use valueToCheck to suggest a value for lowerBound
	    lowerBound = resolveVarByLessEqual((Variable) lowerBound,
		    (Comparable) valueToCheck, minInclusive, cloned);
	    if (lowerBound == null)
		// deadend
		return false;
	} else if (valueToCheck instanceof Variable) {
	    // we can use lowerBound to suggest a value for valueToCheck
	    valueToCheck = resolveVarByGreaterEqual((Variable) valueToCheck,
		    (Comparable) lowerBound, minInclusive, cloned);
	    if (valueToCheck == null)
		// deadend
		return false;

	    if (((Comparable) valueToCheck).compareTo(upperBound) > 0
		    || (!maxInclusive && ((Comparable) valueToCheck)
			    .compareTo(upperBound) == 0))
		// one of the conditions 'a)' / 'b)' does not hold
		return false;
	} else {
	    try {
		if (((Comparable) valueToCheck).compareTo(upperBound) > 0
			|| (!maxInclusive && ((Comparable) valueToCheck)
				.compareTo(upperBound) == 0)
			|| ((Comparable) valueToCheck).compareTo(lowerBound) < 0
			|| (!minInclusive && ((Comparable) valueToCheck)
				.compareTo(lowerBound) == 0))
		    // one of the conditions 'a)' / 'b)' / 'c)' / 'd)' does not
		    // hold
		    return false;
	    } catch (ClassCastException e) {
		return false;
	    }
	}

	synchronize(context, cloned);
	return true;
    }

    // public boolean isDisjointWith(Object other_min, boolean
    // other_minInclusive, Object other_max,
    // boolean other_maxInclusive, HashMap context, int ttl, List<MatchLogEntry>
    // log, Object min,
    // boolean minInclusive, Object max, boolean maxInclusive) {
    // // ttl =
    // checkTTL(ttl);
    //
    // min = Variable.resolveVarRef(min, context);
    // max = Variable.resolveVarRef(max, context);
    // other_min = Variable.resolveVarRef(other_min, context);
    // other_max = Variable.resolveVarRef(other_max, context);
    //
    // return (max instanceof Comparable && other_min instanceof Comparable
    // && (((Comparable) max).compareTo(other_min) < 0
    // || (((Comparable) max).compareTo(other_min) == 0 && (!maxInclusive ||
    // !other_minInclusive))))
    // || (other_max instanceof Comparable && min instanceof Comparable
    // && (((Comparable) other_max).compareTo(min) < 0 || (((Comparable)
    // other_max).compareTo(min) == 0
    // && (!other_maxInclusive || !minInclusive))));
    // }

    protected boolean matches(Object other_min, boolean other_minInclusive, Object other_max, boolean other_maxInclusive,
	    HashMap context, int ttl, List<MatchLogEntry> log, Object min, boolean minInclusive, Object max,
	    boolean maxInclusive) {
	// ttl =
	checkTTL(ttl);
	// TODO: check other ClassExpressions (e.g. Union..)

	HashMap cloned = (context == null) ? null : (HashMap) context.clone();

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

	if (other_maxInclusive)
	    in = Variable.resolveVarRef(other_max, cloned);
	else
	    ex = Variable.resolveVarRef(other_max, cloned);

	if (myex != null) {
	    if (myex instanceof Variable) {
		if (ex instanceof Comparable && cloned != null) {
		    // myex is a Variable -> toString returns its URI
		    cloned.put(myex.toString(), ex);
		} else if (ex == null && in instanceof Comparable && cloned != null) {
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
		} else if (in == null && ex instanceof Comparable && cloned != null) {
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

	if (other_minInclusive)
	    in = Variable.resolveVarRef(other_min, cloned);
	else
	    ex = Variable.resolveVarRef(other_min, cloned);

	if (myex != null) {
	    if (myex instanceof Variable) {
		if (ex instanceof Comparable && cloned != null) {
		    // myex is a Variable -> toString returns its URI
		    cloned.put(myex.toString(), ex);
		} else if (ex == null && in instanceof Comparable && cloned != null) {
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
		} else if (in == null && ex instanceof Comparable && cloned != null) {
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

}
