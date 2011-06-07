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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.Variable;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class OrderingRestriction extends Restriction {
	// substitutions for Double.MIN_NORMAL & Float.MIN_NORMAL from Java 1.6
	private static final double DOUBLE_SMALLEST_POSITIVE_VALUE = Double.longBitsToDouble(0x0010000000000000L);
	private static final float FLOAT_SMALLEST_POSITIVE_VALUE = Float.intBitsToFloat(0x00800000);
	
	public static final String MY_URI;
	public static final String PROP_VALUE_HAS_MAX_EXCLUSIVE;
	public static final String PROP_VALUE_HAS_MAX_INCLUSIVE;
	public static final String PROP_VALUE_HAS_MIN_EXCLUSIVE;
	public static final String PROP_VALUE_HAS_MIN_INCLUSIVE;
	public static final String VARIABLE_MAX_VALUE;
	public static final String VARIABLE_MIN_VALUE;
	static {
		MY_URI = uAAL_VOCABULARY_NAMESPACE + "OrderingRestriction";
		PROP_VALUE_HAS_MAX_EXCLUSIVE = uAAL_VOCABULARY_NAMESPACE + "hasMaxExclusive";
		PROP_VALUE_HAS_MAX_INCLUSIVE = uAAL_VOCABULARY_NAMESPACE + "hasMaxInclusive";
		PROP_VALUE_HAS_MIN_EXCLUSIVE = uAAL_VOCABULARY_NAMESPACE + "hasMinExclusive";
		PROP_VALUE_HAS_MIN_INCLUSIVE = uAAL_VOCABULARY_NAMESPACE + "hasMinInclusive";
		VARIABLE_MAX_VALUE = uAAL_VOCABULARY_NAMESPACE + "maxValue";
		VARIABLE_MIN_VALUE = uAAL_VOCABULARY_NAMESPACE + "minValue";
		register(OrderingRestriction.class, null, null, MY_URI);
	}
	
	public static OrderingRestriction newOrderingRestriction(Object max, Object min,
			boolean maxInclusive, boolean minInclusive, String propURI) {
		if (propURI == null
				|| (!(max instanceof Comparable)
						&& !Variable.isVarRef(max)
						&& !(min instanceof Comparable)
						&& !Variable.isVarRef(min)))
			return null;
		
		if (max instanceof Comparable
				&& min instanceof Comparable
				&& ((Comparable) min).compareTo(max) > 0)
			return null;
		
		OrderingRestriction result = new OrderingRestriction();
		result.setProperty(PROP_OWL_ON_PROPERTY, propURI);
		if (max != null)
			if (maxInclusive)
				result.props.put(PROP_VALUE_HAS_MAX_INCLUSIVE, max);
			else
				result.props.put(PROP_VALUE_HAS_MAX_EXCLUSIVE, max);
		if (min != null)
			if (minInclusive)
				result.props.put(PROP_VALUE_HAS_MIN_INCLUSIVE, min);
			else
				result.props.put(PROP_VALUE_HAS_MIN_EXCLUSIVE, min);
		return result;
	}
	
	public static OrderingRestriction newOrderingRestriction(Object max, Object min,
			boolean maxInclusive, boolean minInclusive, Restriction toMerge) {
		if (toMerge == null  ||  toMerge.getOnProperty() == null
				|| (max == null  &&  min == null))
			return null;
		OrderingRestriction result = newOrderingRestriction(max, min,
				maxInclusive, minInclusive, toMerge.getOnProperty());
		for (Enumeration e=toMerge.getPropertyURIs(); e.hasMoreElements();) {
			String propURI = (String) e.nextElement();
			result.setProperty(propURI, toMerge.getProperty(propURI));
		}
		return result;
	}
	
	public OrderingRestriction() {
		super();
		ArrayList l = new ArrayList(1);
		l.add(new Resource(MY_URI));
		props.put(PROP_RDF_TYPE, l);
	}

	public String getExpressionTypeURI() {
		return MY_URI;
	}
	
	public Comparable getLowerbound() {
		Object o = props.get(PROP_VALUE_HAS_MIN_INCLUSIVE);
		if (o instanceof Comparable)
			return (Comparable) o;
		o = props.get(PROP_VALUE_HAS_MIN_EXCLUSIVE);
		return (o instanceof Comparable)? (Comparable) o : null;
	}
	
	private Comparable getNext(Comparable c) {
		if (c instanceof ComparableIndividual)
			return ((ComparableIndividual) c).getNext();
		if (c instanceof Double)
			return new Double(((Double) c).doubleValue() + DOUBLE_SMALLEST_POSITIVE_VALUE);
//		if (c instanceof Duration)
//			// unfortunately javax.xml.datatype.Duration does not implement Comparable
//			return ((Duration) c).add(TypeMapper.getDataTypeFactory().newDuration(1000));
		if (c instanceof Float)
			return new Float(((Float) c).floatValue() + FLOAT_SMALLEST_POSITIVE_VALUE);
		if (c instanceof Integer)
			return new Integer(((Integer) c).intValue() + 1);
		if (c instanceof Long)
			return new Long(((Long) c).longValue() + 1);
//		if (c instanceof XMLGregorianCalendar)
//			// unfortunately javax.xml.datatype.XMLGregorianCalendar does not implement Comparable
//			return TypeMapper.getDataTypeFactory().newXMLGregorianCalendar(
//					((XMLGregorianCalendar) c).getYear(),
//					((XMLGregorianCalendar) c).getMonth(),
//					((XMLGregorianCalendar) c).getDay(),
//					((XMLGregorianCalendar) c).getHour(),
//					((XMLGregorianCalendar) c).getMinute(),
//					((XMLGregorianCalendar) c).getSecond(),
//					((XMLGregorianCalendar) c).getMillisecond()+1,
//					((XMLGregorianCalendar) c).getTimezone());
		// for xsd:string, xsd:language, xsd:XMLLiteral & xsd:anyURI no next can be determined
		// for Boolean, nobody uses OrderingRestriction
		return null;
	}
	
	private Comparable getPrevious(Comparable c) {
		if (c instanceof ComparableIndividual)
			return ((ComparableIndividual) c).getPrevious();
		if (c instanceof Double)
			return new Double(((Double) c).doubleValue() - DOUBLE_SMALLEST_POSITIVE_VALUE);
//		if (c instanceof Duration)
//			// unfortunately javax.xml.datatype.Duration does not implement Comparable
//			return ((Duration) c).subtract(TypeMapper.getDataTypeFactory().newDuration(1000));
		if (c instanceof Float)
			return new Float(((Float) c).floatValue() - FLOAT_SMALLEST_POSITIVE_VALUE);
		if (c instanceof Integer)
			return new Integer(((Integer) c).intValue() - 1);
		if (c instanceof Long)
			return new Long(((Long) c).longValue() - 1);
//		if (c instanceof XMLGregorianCalendar)
//			// unfortunately javax.xml.datatype.XMLGregorianCalendar does not implement Comparable
//			return TypeMapper.getDataTypeFactory().newXMLGregorianCalendar(
//					((XMLGregorianCalendar) c).getYear(),
//					((XMLGregorianCalendar) c).getMonth(),
//					((XMLGregorianCalendar) c).getDay(),
//					((XMLGregorianCalendar) c).getHour(),
//					((XMLGregorianCalendar) c).getMinute(),
//					((XMLGregorianCalendar) c).getSecond(),
//					((XMLGregorianCalendar) c).getMillisecond()-1,
//					((XMLGregorianCalendar) c).getTimezone());
		// for xsd:string, xsd:language, xsd:XMLLiteral & xsd:anyURI no next can be determined
		// for Boolean, nobody uses OrderingRestriction
		return null;
	}
	
	public Comparable getUpperbound() {
		Object o = props.get(PROP_VALUE_HAS_MAX_INCLUSIVE);
		if (o instanceof Comparable)
			return (Comparable) o;
		o = props.get(PROP_VALUE_HAS_MAX_EXCLUSIVE);
		return (o instanceof Comparable)? (Comparable) o : null;
	}

	public boolean hasMember(Object o, Hashtable context) {
		if (o == null)
			return true;
		
		if (super.isWellFormed()  &&  !super.hasMember(o, context))
			return false;
		
		// because it has passed super, it must be a Resource
		o = Variable.resolveVarRef(((Resource) o).getProperty(getOnProperty()), context);
		if (!(o instanceof Comparable))
			return false;
		
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		
		Object aux = Variable.resolveVarRef(props.get(PROP_VALUE_HAS_MAX_EXCLUSIVE), context);
		if (aux == null) {
			aux = Variable.resolveVarRef(props.get(PROP_VALUE_HAS_MAX_INCLUSIVE), context);
			if (aux != null)
				if (aux instanceof Variable) {
					// we can assign any value greater than o (or event o itself) to aux so that o is a member of this ordering restriction
					// so we try first with the next value and if it cannot be determined we take o
					Comparable next = getNext((Comparable) o);
					if (next == null)
						next = (Comparable) o;
					cloned.put(aux.toString(), next);
				} else if (!(aux instanceof Comparable) || ((Comparable) o).compareTo(aux) > 0)
					return false;
		} else if (aux instanceof Variable) {
			// we can assign any value greater than o to aux so that o is a member of this ordering restriction
			// so we try with the next value 
			Comparable next = getNext((Comparable) o);
			if (next == null)
				return false;
			cloned.put(aux.toString(), next);
		} else if (!(aux instanceof Comparable) || ((Comparable) o).compareTo(aux) > -1)
			return false;
		
		aux = Variable.resolveVarRef(props.get(PROP_VALUE_HAS_MIN_EXCLUSIVE), context);
		if (aux == null) {
			aux = Variable.resolveVarRef(props.get(PROP_VALUE_HAS_MIN_INCLUSIVE), context);
			if (aux instanceof Variable) {
				// we can assign any value less than o (or event o itself) to aux so that o is a member of this ordering restriction
				// so we try first with the previous value and if it cannot be determined we take o
				Comparable prev = getPrevious((Comparable) o);
				if (prev == null)
					prev = (Comparable) o;
				cloned.put(aux.toString(), prev);
			} else if (aux != null
					&& (!(aux instanceof Comparable) || ((Comparable) aux).compareTo(o) > 0))
				return false;
		} else if (aux instanceof Variable) {
			// we can assign any value less than o to aux so that o is a member of this ordering restriction
			// so we try with the previous value 
			Comparable prev = getPrevious((Comparable) o);
			if (prev == null)
				return false;
			cloned.put(aux.toString(), prev);
		} else if (!(aux instanceof Comparable) || ((Comparable) aux).compareTo(o) > -1)
			return false;

		synchronize(context, cloned);
		return true;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression, Hashtable) */
	public boolean isDisjointWith(ClassExpression other, Hashtable context) {
		if (super.isDisjointWith(other, context))
			return true;
		
		if (other instanceof OrderingRestriction
				&& getOnProperty().equals(((OrderingRestriction) other).getOnProperty())) {
			boolean max1Incl = true, max2Incl = true, min1Incl = true, min2Incl = true;
			Object max1 = Variable.resolveVarRef(
					props.get(PROP_VALUE_HAS_MAX_EXCLUSIVE), context);
			if (max1 == null)
				max1 = Variable.resolveVarRef(
						props.get(PROP_VALUE_HAS_MAX_INCLUSIVE), context);
			else
				max1Incl = false;
			Object max2 = Variable.resolveVarRef(((OrderingRestriction) other
					).props.get(PROP_VALUE_HAS_MAX_EXCLUSIVE), context);
			if (max2 == null)
				max2 = Variable.resolveVarRef(((OrderingRestriction) other
						).props.get(PROP_VALUE_HAS_MAX_INCLUSIVE), context);
			else
				max2Incl = false;
			Object min1 = Variable.resolveVarRef(props.get(
					PROP_VALUE_HAS_MIN_EXCLUSIVE), context);
			if (min1 == null)
				min1 = Variable.resolveVarRef(props.get(
						PROP_VALUE_HAS_MIN_INCLUSIVE), context);
			else
				min1Incl = false;
			Object min2 = Variable.resolveVarRef(((OrderingRestriction) other
					).props.get(PROP_VALUE_HAS_MIN_EXCLUSIVE), context);
			if (min2 == null)
				min2 = Variable.resolveVarRef(((OrderingRestriction) other
						).props.get(PROP_VALUE_HAS_MIN_INCLUSIVE), context);
			else
				min2Incl = false;
			
			return (max1 instanceof Comparable  &&  min2 instanceof Comparable
					&&  (((Comparable) max1).compareTo(min2) < 0
							|| (((Comparable) max1).compareTo(min2) == 0
									&& (!max1Incl  ||  !min2Incl))))
				|| (max2 instanceof Comparable  &&  min1 instanceof Comparable
						&&  (((Comparable) max2).compareTo(min1) < 0
								|| (((Comparable) max2).compareTo(min1) == 0
										&& (!max2Incl  ||  !min1Incl))));
		}
		
		return false;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
	public boolean isWellFormed() {
		if (props.containsKey(PROP_VALUE_HAS_MAX_EXCLUSIVE)
				&&  props.containsKey(PROP_VALUE_HAS_MAX_INCLUSIVE))
			return false;

		if (props.containsKey(PROP_VALUE_HAS_MIN_EXCLUSIVE)
				&&  props.containsKey(PROP_VALUE_HAS_MIN_INCLUSIVE))
			return false;
		
		return props.containsKey(PROP_OWL_ON_PROPERTY)
			&& !props.containsKey(PROP_OWL_HAS_VALUE)
			&& (props.containsKey(PROP_VALUE_HAS_MAX_EXCLUSIVE)
					|| props.containsKey(PROP_VALUE_HAS_MAX_INCLUSIVE)
					|| props.containsKey(PROP_VALUE_HAS_MIN_EXCLUSIVE)
					|| props.containsKey(PROP_VALUE_HAS_MIN_INCLUSIVE));
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression, Hashtable) */
	public boolean matches(ClassExpression subtype, Hashtable context) {
		if (super.isWellFormed()  &&  !super.matches(subtype, context))
			return false;
		
		if (subtype instanceof OrderingRestriction) {
			OrderingRestriction other = (OrderingRestriction) subtype;
			if (!isWellFormed()
					|| !other.isWellFormed()
					|| !getOnProperty().equals(other.getOnProperty()))
				return false;
			
			Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
			
			// note: the below calls to resolveVarRef will shell var refs to the underlying process parameter
			//       if no corresponding value can be found in the context, otherwise the associated value will be
			//       returned; if the passed value is not a var ref, then we'll get it back without any change 
			Object myex = Variable.resolveVarRef(props.get(PROP_VALUE_HAS_MAX_EXCLUSIVE), cloned);
			Object ex = Variable.resolveVarRef(other.props.get(PROP_VALUE_HAS_MAX_EXCLUSIVE), cloned);
			Object myin = Variable.resolveVarRef(props.get(PROP_VALUE_HAS_MAX_INCLUSIVE), cloned);
			Object in = Variable.resolveVarRef(other.props.get(PROP_VALUE_HAS_MAX_INCLUSIVE), cloned);
			if (myex != null) {
				if (myex instanceof Variable) {
					if (ex instanceof Comparable  &&  cloned != null) {
						// myex is a Variable -> toString returns its URI
						cloned.put(myex.toString(), ex);
					} else if (ex == null  &&  in instanceof Comparable  &&  cloned != null) {
						// any value greater than 'in' can be used as value for 'myex'
						// we try to take the 'next' value to 'in'
						Comparable next = getNext((Comparable) in);
						if (next == null)
							return false;
						cloned.put(myex.toString(), next);
					} else
						return false;
				} else if (ex instanceof Variable) {
					// if the parameter value is <= myex, they match -> conditional match: parameter value == myex
					if (cloned == null)
						return false;
					cloned.put(ex.toString(), myex);
				} else if (ex instanceof Comparable) {
					if (((Comparable) ex).compareTo(myex) > 0)
						return false;
				} else if (in instanceof Variable  &&  cloned != null) {
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
					if (in instanceof Comparable  &&  cloned != null) {
						// myin is a Variable -> toString returns its URI
						cloned.put(myin.toString(), in);
					} else if (in == null  &&  ex instanceof Comparable  &&  cloned != null) {
						// any value greater than or equal to the previous value of 'ex' can be used as value for 'myin'
						// we try to take the 'previous' value of 'ex'
						Comparable prev = getPrevious((Comparable) ex);
						if (prev == null)
							return false;
						cloned.put(myin.toString(), prev);
					} else
						return false;
				} else if (in instanceof Variable) {
					// if the parameter value is <= myin, they match -> conditional match: parameter value == myin
					if (cloned == null)
						return false;
					cloned.put(in.toString(), myin);
				} else if (in instanceof Comparable) {
					if (((Comparable) in).compareTo(myin) > 0)
						return false;
				} else if (ex instanceof Variable  &&  cloned != null) {
					// any value less than the next value of 'myin' can be used as value for 'ex'
					// we try to take the 'next' value of 'myin'
					Comparable next = getNext((Comparable) myin);
					if (next == null)
						return false;
					cloned.put(ex.toString(), next);
				} else if (ex instanceof Comparable) {
					// the maximum allowed value for ex is the next value of myin so that all values that are member of 'other' are also member of 'this'
					Comparable next = getNext((Comparable) myin);
					if (next == null)
						next = (Comparable) myin;
					if (((Comparable) ex).compareTo(next) > 0)
						return false;
				} else
					return false;
			} else if (ex != null  ||  in != null)
				return false;
			
			// note: the below calls to resolveVarRef will shell var refs to the underlying process parameter
			//       if no corresponding value can be found in the context, otherwise the associated value will be
			//       returned; if the passed value is not a var ref, then we'll get it back without any change 
			myex = Variable.resolveVarRef(props.get(PROP_VALUE_HAS_MIN_EXCLUSIVE), cloned);
			ex = Variable.resolveVarRef(other.props.get(PROP_VALUE_HAS_MIN_EXCLUSIVE), cloned);
			myin = Variable.resolveVarRef(props.get(PROP_VALUE_HAS_MIN_INCLUSIVE), cloned);
			in = Variable.resolveVarRef(other.props.get(PROP_VALUE_HAS_MIN_INCLUSIVE), cloned);
			if (myex != null) {
				if (myex instanceof Variable) {
					if (ex instanceof Comparable  &&  cloned != null) {
						// myex is a Variable -> toString returns its URI
						cloned.put(myex.toString(), ex);
					} else if (ex == null  &&  in instanceof Comparable  &&  cloned != null) {
						// any value less than 'in' can be used as value for 'myex'
						// we try to take the 'previous' value of 'in'
						Comparable prev = getPrevious((Comparable) in);
						if (prev == null)
							return false;
						cloned.put(myex.toString(), prev);
					} else
						return false;
				} else if (ex instanceof Variable) {
					// if the parameter value is >= myex, they match -> conditional match: parameter value == myex
					if (cloned == null)
						return false;
					cloned.put(ex.toString(), myex);
				} else if (ex instanceof Comparable) {
					if (((Comparable) myex).compareTo(ex) > 0)
						return false;
				} else if (in instanceof Variable  &&  cloned != null) {
					// any value greater than 'myex' can be used as value for 'in'
					// we try to take the 'next' value of 'myex'
					Comparable next = getNext((Comparable) myex);
					if (next == null)
						return false;
					cloned.put(in.toString(), next);
				} else if (in instanceof Comparable) {
					// the minimum allowed value for in is the next value of myex so that all values that are member of 'other' are also member of 'this'
					Comparable next = getNext((Comparable) myex);
					if (next == null)
						next = (Comparable) myex;
					if (((Comparable) next).compareTo(in) > 0)
						return false;
				} else
					return false;
			} else if (myin != null) {
				if (myin instanceof Variable) {
					if (in instanceof Comparable  &&  cloned != null) {
						// myin is a Variable -> toString returns its URI
						cloned.put(myin.toString(), in);
					} else if (in == null  &&  ex instanceof Comparable  &&  cloned != null) {
						// any value less than or equal to the next value of 'ex' can be used as value for 'myin'
						// we try to take the 'next' value of 'ex'
						Comparable next = getNext((Comparable) ex);
						if (next == null)
							return false;
						cloned.put(myin.toString(), next);
					} else
						return false;
				} else if (in instanceof Variable) {
					// if the parameter value is >= myin, they match -> conditional match: parameter value == myin
					if (cloned == null)
						return false;
					cloned.put(in.toString(), myin);
				} else if (in instanceof Comparable) {
					if (((Comparable) myin).compareTo(in) > 0)
						return false;
				} else if (ex instanceof Variable  &&  cloned != null) {
					// any value greater than the previous value of 'myin' can be used as value for 'ex'
					// we try to take the 'previous' value of 'myin'
					Comparable prev = getPrevious((Comparable) myin);
					if (prev == null)
						return false;
					cloned.put(ex.toString(), prev);
				} else if (ex instanceof Comparable) {
					// the minimum allowed value for ex is the previous value of myin so that all values that are member of 'other' are also member of 'this'
					Comparable prev = getPrevious((Comparable) myin);
					if (prev == null)
						prev = (Comparable) myin;
					if (((Comparable) prev).compareTo(ex) > 0)
						return false;
				} else
					return false;
			} else if (ex != null  ||  in != null)
				return false;

			synchronize(context, cloned);
			return true;
		}
		
		// all other cases are already handled correctly in super.matches()
		return true;
	}

	/** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
	public void setProperty(String propURI, Object o) {
		if (propURI == null  ||  o == null  ||  propURI.equals(PROP_OWL_HAS_VALUE)
				|| props.containsKey(propURI))
			return;
		
		if (o instanceof Comparable  ||  Variable.isVarRef(o)) {
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
			props.put(propURI, o);
		} else
			super.setProperty(propURI, o);
	}
}
