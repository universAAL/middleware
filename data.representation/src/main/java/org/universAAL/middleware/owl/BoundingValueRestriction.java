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
 * @author Carsten Stockloew
 */
public class BoundingValueRestriction extends AbstractRestriction {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE + "BoundingValueRestriction";
    
    // substitutions for Double.MIN_NORMAL & Float.MIN_NORMAL from Java 1.6
    private static final double DOUBLE_SMALLEST_POSITIVE_VALUE = Double
	    .longBitsToDouble(0x0010000000000000L);
    private static final float FLOAT_SMALLEST_POSITIVE_VALUE = Float
	    .intBitsToFloat(0x00800000);

    public static final String PROP_VALUE_HAS_MAX_EXCLUSIVE;
    public static final String PROP_VALUE_HAS_MAX_INCLUSIVE;
    public static final String PROP_VALUE_HAS_MIN_EXCLUSIVE;
    public static final String PROP_VALUE_HAS_MIN_INCLUSIVE;

    private Object min;
    private Object max;
    private boolean minInclusive;
    private boolean maxInclusive;
    
    
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
    
    public BoundingValueRestriction(String propURI, Object min, boolean minInclusive,
	    Object max, boolean maxInclusive) {
	if (propURI == null
		|| min == null
		|| max == null
		|| (!(max instanceof Comparable) && !Variable.isVarRef(max)
			&& !(min instanceof Comparable) && !Variable
			.isVarRef(min)))
	    throw new NullPointerException();

	if (max instanceof Comparable && min instanceof Comparable
		&& ((Comparable) min).compareTo(max) > 0)
	    throw new IllegalArgumentException("min can not be greater than max.");

	this.min = min;
	this.max = max;
	this.minInclusive = minInclusive;
	this.maxInclusive = maxInclusive;
	
	setOnProperty(propURI);
	if (minInclusive)
	    super.setProperty(PROP_VALUE_HAS_MIN_INCLUSIVE, min);
	else
	    super.setProperty(PROP_VALUE_HAS_MIN_EXCLUSIVE, min);
	if (maxInclusive)
	    super.setProperty(PROP_VALUE_HAS_MAX_INCLUSIVE, max);
	else
	    super.setProperty(PROP_VALUE_HAS_MAX_EXCLUSIVE, max);
    }    
    

    public String getClassURI() {
	return MY_URI;
    }
    
    public Comparable getLowerbound() {
	return (min instanceof Comparable) ? (Comparable) min : null;
    }

    public Comparable getUpperbound() {
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

//	if (super.isWellFormed() && !super.hasMember(member, context))
//	    return false;

	// because it has passed super, it must be a Resource
	member = Variable.resolveVarRef(((Resource) member).getProperty(getOnProperty()),
		context);
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
	    return ((Boolean)noRes).booleanValue();

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
	
	// handle this restriction
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
	}
	
	// do not handle other restrictions
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
