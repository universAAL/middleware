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

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.Variable;

/**
 * Implementation of XSD Value Restrictions: it contains all individuals that
 * are connected by the specified property to a value that meets the specified
 * conditions. These conditions are either:
 * <ol>
 * <li>min inclusive</li>
 * <li>min exclusive</li>
 * <li>max inclusive</li>
 * <li>max exclusive</li>
 * </ol>
 * 
 * It is possible to define a condition on the min value and on the max value in
 * the same {@link BoundingValueRestriction}.
 * 
 * BoundingValueRestriction will soon be replaced by
 * {@link BoundedValueRestriction} and its subclasses.
 * 
 * @author Carsten Stockloew
 */
public class BoundingValueRestriction extends AbstractRestriction {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "BoundingValueRestriction";

    // substitutions for Double.MIN_NORMAL & Float.MIN_NORMAL from Java 1.6
    private static final double DOUBLE_SMALLEST_POSITIVE_VALUE = Double
	    .longBitsToDouble(0x0010000000000000L);
    private static final float FLOAT_SMALLEST_POSITIVE_VALUE = Float
	    .intBitsToFloat(0x00800000);

    public static final String PROP_VALUE_HAS_MAX_EXCLUSIVE;
    public static final String PROP_VALUE_HAS_MAX_INCLUSIVE;
    public static final String PROP_VALUE_HAS_MIN_EXCLUSIVE;
    public static final String PROP_VALUE_HAS_MIN_INCLUSIVE;

    static {
	// TODO: for now, we use the uaal vocabulary, this has to be changed
	// to xml names.
	PROP_VALUE_HAS_MAX_EXCLUSIVE = uAAL_VOCABULARY_NAMESPACE
		+ "hasMaxExclusive";
	PROP_VALUE_HAS_MAX_INCLUSIVE = uAAL_VOCABULARY_NAMESPACE
		+ "hasMaxInclusive";
	PROP_VALUE_HAS_MIN_EXCLUSIVE = uAAL_VOCABULARY_NAMESPACE
		+ "hasMinExclusive";
	PROP_VALUE_HAS_MIN_INCLUSIVE = uAAL_VOCABULARY_NAMESPACE
		+ "hasMinInclusive";
	register(BoundingValueRestriction.class, null,
		PROP_VALUE_HAS_MAX_EXCLUSIVE, null);
	register(BoundingValueRestriction.class, null,
		PROP_VALUE_HAS_MAX_INCLUSIVE, null);
	register(BoundingValueRestriction.class, null,
		PROP_VALUE_HAS_MIN_EXCLUSIVE, null);
	register(BoundingValueRestriction.class, null,
		PROP_VALUE_HAS_MIN_INCLUSIVE, null);
    }

    /** Standard constructor for exclusive use by serializers. */
    public BoundingValueRestriction() {
    }

    public BoundingValueRestriction(String propURI, Object min,
	    boolean minInclusive, Object max, boolean maxInclusive) {
	if (propURI == null
		|| (min == null && max == null)
		|| (max != null && !(max instanceof Comparable) && !Variable
			.isVarRef(max))
		|| (min != null && !(min instanceof Comparable) && !Variable
			.isVarRef(min)))
	    throw new NullPointerException();

	if (max instanceof Comparable && min instanceof Comparable
		&& ((Comparable) min).compareTo(max) > 0)
	    throw new IllegalArgumentException(
		    "min can not be greater than max.");

	setOnProperty(propURI);

	if (min != null)
	    if (minInclusive)
		props.put(PROP_VALUE_HAS_MIN_INCLUSIVE, min);
	    else
		props.put(PROP_VALUE_HAS_MIN_EXCLUSIVE, min);

	if (max != null)
	    if (maxInclusive)
		props.put(PROP_VALUE_HAS_MAX_INCLUSIVE, max);
	    else
		props.put(PROP_VALUE_HAS_MAX_EXCLUSIVE, max);
    }

    public String getClassURI() {
	return MY_URI;
    }

    public Comparable getLowerbound() {
	Object min = props.get(PROP_VALUE_HAS_MIN_INCLUSIVE);
	if (min == null)
	    min = props.get(PROP_VALUE_HAS_MIN_EXCLUSIVE);
	return (min instanceof Comparable) ? (Comparable) min : null;
    }

    public Comparable getUpperbound() {
	Object max = props.get(PROP_VALUE_HAS_MAX_INCLUSIVE);
	if (max == null)
	    max = props.get(PROP_VALUE_HAS_MAX_EXCLUSIVE);
	return (max instanceof Comparable) ? (Comparable) max : null;
    }

    private Comparable getNext(Comparable c) {
	if (c instanceof ComparableIndividual)
	    return ((ComparableIndividual) c).getNext();
	if (c instanceof Double)
	    return new Double(((Double) c).doubleValue()
		    + DOUBLE_SMALLEST_POSITIVE_VALUE);
	// if (c instanceof Duration)
	// // unfortunately javax.xml.datatype.Duration does not implement
	// Comparable
	// return ((Duration)
	// c).add(TypeMapper.getDataTypeFactory().newDuration(1000));
	if (c instanceof Float)
	    return new Float(((Float) c).floatValue()
		    + FLOAT_SMALLEST_POSITIVE_VALUE);
	if (c instanceof Integer)
	    return new Integer(((Integer) c).intValue() + 1);
	if (c instanceof Long)
	    return new Long(((Long) c).longValue() + 1);
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

    private Comparable getPrevious(Comparable c) {
	if (c instanceof ComparableIndividual)
	    return ((ComparableIndividual) c).getPrevious();
	if (c instanceof Double)
	    return new Double(((Double) c).doubleValue()
		    - DOUBLE_SMALLEST_POSITIVE_VALUE);
	// if (c instanceof Duration)
	// // unfortunately javax.xml.datatype.Duration does not implement
	// Comparable
	// return ((Duration)
	// c).subtract(TypeMapper.getDataTypeFactory().newDuration(1000));
	if (c instanceof Float)
	    return new Float(((Float) c).floatValue()
		    - FLOAT_SMALLEST_POSITIVE_VALUE);
	if (c instanceof Integer)
	    return new Integer(((Integer) c).intValue() - 1);
	if (c instanceof Long)
	    return new Long(((Long) c).longValue() - 1);
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

    private Comparable resolveVarByGreaterEqual(Variable v,
	    Comparable lowerbound, boolean canBeEqual, Hashtable context) {
	Comparable resolution = canBeEqual ? lowerbound : getNext(lowerbound);
	// consider that we might fail because getNext() does not work always
	if (resolution != null)
	    // add the variable resolution to the context
	    context.put(v.toString(), resolution);
	return resolution;
    }

    private Comparable resolveVarByLessEqual(Variable v, Comparable upperbound,
	    boolean canBeEqual, Hashtable context) {
	Comparable resolution = canBeEqual ? upperbound
		: getPrevious(upperbound);
	// consider that we might fail because getPrevious() does not work
	// always
	if (resolution != null)
	    // add the variable resolution to the context
	    context.put(v.toString(), resolution);
	return resolution;
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#copy() */
    public ClassExpression copy() {
	return copyTo(new BoundingValueRestriction());
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object,
     *      Hashtable)
     */
    public boolean hasMember(Object member, Hashtable context) {
	if (member == null)
	    return true;

	if (!(member instanceof Resource))
	    return false;

	// get the value to be checked against the lower- and upperbounds
	Object valueToCheck = ((Resource) member).getProperty(getOnProperty());
	if (valueToCheck instanceof Variable)
	    // check if there is any value already assumed for it in the given
	    // context
	    valueToCheck = Variable.resolveVarRef(valueToCheck, context);
	if (!(valueToCheck instanceof Variable)
		&& !(valueToCheck instanceof Comparable))
	    return false;

	boolean minInclusive = true;
	Object lowerBound = props.get(PROP_VALUE_HAS_MIN_INCLUSIVE);
	if (lowerBound == null) {
	    lowerBound = props.get(PROP_VALUE_HAS_MIN_EXCLUSIVE);
	    minInclusive = false;
	}

	boolean maxInclusive = true;
	Object upperBound = props.get(PROP_VALUE_HAS_MAX_INCLUSIVE);
	if (upperBound == null) {
	    upperBound = props.get(PROP_VALUE_HAS_MAX_EXCLUSIVE);
	    maxInclusive = false;
	}

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
	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();

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
	} else if (((Comparable) valueToCheck).compareTo(upperBound) > 0
		|| (!maxInclusive && ((Comparable) valueToCheck)
			.compareTo(upperBound) == 0)
		|| ((Comparable) valueToCheck).compareTo(lowerBound) < 0
		|| (!minInclusive && ((Comparable) valueToCheck)
			.compareTo(lowerBound) == 0))
	    // one of the conditions 'a)' / 'b)' / 'c)' / 'd)' does not hold
	    return false;

	synchronize(context, cloned);
	return true;
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression,
     *      Hashtable)
     */
    public boolean isDisjointWith(ClassExpression other, Hashtable context) {
	// TODO: change to min/max instance variables
	if (other instanceof BoundingValueRestriction
		&& getOnProperty().equals(
			((BoundingValueRestriction) other).getOnProperty())) {
	    boolean max1Incl = true, max2Incl = true, min1Incl = true, min2Incl = true;
	    Object max1 = Variable.resolveVarRef(
		    getProperty(PROP_VALUE_HAS_MAX_EXCLUSIVE), context);
	    if (max1 == null)
		max1 = Variable.resolveVarRef(
			getProperty(PROP_VALUE_HAS_MAX_INCLUSIVE), context);
	    else
		max1Incl = false;
	    Object max2 = Variable
		    .resolveVarRef(((BoundingValueRestriction) other)
			    .getProperty(PROP_VALUE_HAS_MAX_EXCLUSIVE), context);
	    if (max2 == null)
		max2 = Variable.resolveVarRef(
			((BoundingValueRestriction) other)
				.getProperty(PROP_VALUE_HAS_MAX_INCLUSIVE),
			context);
	    else
		max2Incl = false;
	    Object min1 = Variable.resolveVarRef(
		    getProperty(PROP_VALUE_HAS_MIN_EXCLUSIVE), context);
	    if (min1 == null)
		min1 = Variable.resolveVarRef(
			getProperty(PROP_VALUE_HAS_MIN_INCLUSIVE), context);
	    else
		min1Incl = false;
	    Object min2 = Variable
		    .resolveVarRef(((BoundingValueRestriction) other)
			    .getProperty(PROP_VALUE_HAS_MIN_EXCLUSIVE), context);
	    if (min2 == null)
		min2 = Variable.resolveVarRef(
			((BoundingValueRestriction) other)
				.getProperty(PROP_VALUE_HAS_MIN_INCLUSIVE),
			context);
	    else
		min2Incl = false;

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
	if (props.containsKey(PROP_VALUE_HAS_MAX_EXCLUSIVE)
		&& props.containsKey(PROP_VALUE_HAS_MAX_INCLUSIVE))
	    return false;

	if (props.containsKey(PROP_VALUE_HAS_MIN_EXCLUSIVE)
		&& props.containsKey(PROP_VALUE_HAS_MIN_INCLUSIVE))
	    return false;

	return props.containsKey(PROP_OWL_ON_PROPERTY)
		&& (props.containsKey(PROP_VALUE_HAS_MAX_EXCLUSIVE)
			|| props.containsKey(PROP_VALUE_HAS_MAX_INCLUSIVE)
			|| props.containsKey(PROP_VALUE_HAS_MIN_EXCLUSIVE) || props
			.containsKey(PROP_VALUE_HAS_MIN_INCLUSIVE));
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression,
     *      Hashtable)
     */
    public boolean matches(ClassExpression subset, Hashtable context) {
	Object noRes = matchesNonRestriction(subset, context);
	if (noRes instanceof Boolean)
	    return ((Boolean) noRes).booleanValue();

	if (subset instanceof BoundingValueRestriction) {
	    BoundingValueRestriction other = (BoundingValueRestriction) subset;
	    if (!isWellFormed() || !other.isWellFormed()
		    || !getOnProperty().equals(other.getOnProperty()))
		return false;

	    Hashtable cloned = (context == null) ? null : (Hashtable) context
		    .clone();

	    // note: the below calls to resolveVarRef will shell var refs to the
	    // underlying process parameter
	    // if no corresponding value can be found in the context, otherwise
	    // the associated value will be
	    // returned; if the passed value is not a var ref, then we'll get it
	    // back without any change
	    Object myex = Variable.resolveVarRef(props
		    .get(PROP_VALUE_HAS_MAX_EXCLUSIVE), cloned);
	    Object ex = Variable.resolveVarRef(other.props
		    .get(PROP_VALUE_HAS_MAX_EXCLUSIVE), cloned);
	    Object myin = Variable.resolveVarRef(props
		    .get(PROP_VALUE_HAS_MAX_INCLUSIVE), cloned);
	    Object in = Variable.resolveVarRef(other.props
		    .get(PROP_VALUE_HAS_MAX_INCLUSIVE), cloned);
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

	    // note: the below calls to resolveVarRef will shell var refs to the
	    // underlying process parameter
	    // if no corresponding value can be found in the context, otherwise
	    // the associated value will be
	    // returned; if the passed value is not a var ref, then we'll get it
	    // back without any change
	    myex = Variable.resolveVarRef(props
		    .get(PROP_VALUE_HAS_MIN_EXCLUSIVE), cloned);
	    ex = Variable.resolveVarRef(other.props
		    .get(PROP_VALUE_HAS_MIN_EXCLUSIVE), cloned);
	    myin = Variable.resolveVarRef(props
		    .get(PROP_VALUE_HAS_MIN_INCLUSIVE), cloned);
	    in = Variable.resolveVarRef(other.props
		    .get(PROP_VALUE_HAS_MIN_INCLUSIVE), cloned);
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

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object o) {
	if (o == null || propURI == null || props.containsKey(propURI))
	    return;

	// handle properties specific to this class
	if (o instanceof Comparable || Variable.isVarRef(o)) {
	    if (propURI.equals(PROP_VALUE_HAS_MAX_EXCLUSIVE)) {
		if (props.containsKey(PROP_VALUE_HAS_MAX_INCLUSIVE))
		    return;
	    } else if (propURI.equals(PROP_VALUE_HAS_MAX_INCLUSIVE)) {
		if (props.containsKey(PROP_VALUE_HAS_MAX_EXCLUSIVE))
		    return;
	    } else if (propURI.equals(PROP_VALUE_HAS_MIN_EXCLUSIVE)) {
		if (propURI.equals(PROP_VALUE_HAS_MIN_INCLUSIVE))
		    return;
	    } else if (propURI.equals(PROP_VALUE_HAS_MIN_INCLUSIVE)) {
		if (propURI.equals(PROP_VALUE_HAS_MIN_EXCLUSIVE))
		    return;
	    } else {
		super.setProperty(propURI, o);
		return;
	    }
	    // if we reach this place, we have a valid prop of this class with a
	    // valid object, without any conflict with existing props
	    props.put(propURI, o);
	}

	// before handing over to the superclass, make sure to avoid properties
	// from other types of restrictions
	if (propURI.equals(HasValueRestriction.PROP_OWL_HAS_VALUE)
		|| propURI
			.equals(MinCardinalityRestriction.PROP_OWL_MIN_CARDINALITY)
		|| propURI
			.equals(MinCardinalityRestriction.PROP_OWL_MIN_QUALIFIED_CARDINALITY)
		|| propURI
			.equals(MaxCardinalityRestriction.PROP_OWL_MAX_CARDINALITY)
		|| propURI
			.equals(MaxCardinalityRestriction.PROP_OWL_MAX_QUALIFIED_CARDINALITY)
		|| propURI
			.equals(ExactCardinalityRestriction.PROP_OWL_CARDINALITY)
		|| propURI
			.equals(ExactCardinalityRestriction.PROP_OWL_QUALIFIED_CARDINALITY)
		|| propURI
			.equals(AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM)
		|| propURI
			.equals(SomeValuesFromRestriction.PROP_OWL_SOME_VALUES_FROM))
	    return;

	// for everything else: call super
	super.setProperty(propURI, o);
    }
}
