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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.Variable;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class Restriction extends ClassExpression {
	public static final String MY_URI;
	public static final String PROP_OWL_ALL_VALUES_FROM;
	public static final String PROP_OWL_CARDINALITY;
	public static final String PROP_OWL_HAS_VALUE;
	public static final String PROP_OWL_MAX_CARDINALITY;
	public static final String PROP_OWL_MIN_CARDINALITY;
	public static final String PROP_OWL_ON_PROPERTY;
	public static final String PROP_OWL_SOME_VALUES_FROM;
	static {
		MY_URI = OWL_NAMESPACE + "Restriction";
		PROP_OWL_ALL_VALUES_FROM = OWL_NAMESPACE + "allValuesFrom";
		PROP_OWL_CARDINALITY = OWL_NAMESPACE + "cardinality";
		PROP_OWL_HAS_VALUE = OWL_NAMESPACE + "hasValue";
		PROP_OWL_MAX_CARDINALITY = OWL_NAMESPACE + "maxCardinality";
		PROP_OWL_MIN_CARDINALITY = OWL_NAMESPACE + "minCardinality";
		PROP_OWL_ON_PROPERTY = OWL_NAMESPACE + "onProperty";
		PROP_OWL_SOME_VALUES_FROM = OWL_NAMESPACE + "someValuesFrom";
		register(Restriction.class, null, null, MY_URI);
	}
	
	public static final Restriction getAllValuesRestriction(String propURI, ClassExpression expr) {
		if (propURI == null  ||  expr == null)
			return null;

		Restriction r = new Restriction();
		r.setProperty(PROP_OWL_ON_PROPERTY, propURI);
		r.setProperty(PROP_OWL_ALL_VALUES_FROM, expr);
		return r;
	}
	
	public static final Restriction getAllValuesRestriction(String propURI, String typeURI) {
		return getAllValuesRestriction(propURI, TypeURI.asTypeURI(typeURI));
	}
	
	public static final Restriction getAllValuesRestrictionWithCardinality(
			String propURI, ClassExpression expr, int max, int min) {
		if (expr == null)
			return null;
		
		Restriction r = getCardinalityRestriction(propURI, max, min);
		if (r != null)
			r.setProperty(PROP_OWL_ALL_VALUES_FROM, expr);
		return r;
	}
	
	public static final Restriction getAllValuesRestrictionWithCardinality(
			String propURI, String typeURI, int max, int min) {
		return getAllValuesRestrictionWithCardinality(propURI, TypeURI.asTypeURI(typeURI), max, min);
	}
	
	public static final Restriction getCardinalityRestriction(String propURI, int max, int min) {
		if (propURI == null  ||  (max > -1  &&  max < min)  ||  (max < 0  &&  min < 1))
			return null;
		
		Restriction r = new Restriction();
		r.setProperty(PROP_OWL_ON_PROPERTY, propURI);
		
		if (min > 0)
			if (min == max) {
				r.setProperty(PROP_OWL_CARDINALITY, new Integer(min));
				return r;
			} else
				r.setProperty(PROP_OWL_MIN_CARDINALITY, new Integer(min));
		
		if (max > -1)
			r.setProperty(PROP_OWL_MAX_CARDINALITY, new Integer(max));
		
		return r;
	}
	
	public static final Restriction getFixedValueRestriction(String propURI, Object o) {
		if (propURI == null  ||  o == null)
			return null;
		
		if (o instanceof String  &&  isQualifiedName((String) o))
			o = new Resource((String) o);
		
		Restriction r = new Restriction();
		r.setProperty(PROP_OWL_ON_PROPERTY, propURI);
		r.setProperty(PROP_OWL_HAS_VALUE, o);
		
		return r;
	}
	
	public static final Restriction getPropertyBanningRestriction(String propURI) {
		return getCardinalityRestriction(propURI, 0, 0);
	}
	
	public static Restriction getRestrictionOnPath(Restriction r, String[] path) {
		return (r == null)? null : r.getRestrictionOnPath(path);
	}
	
	private boolean hasVarRefAsValue = false;
	
	public Restriction() {
		super();
		ArrayList l = new ArrayList(1);
		l.add(new Resource(MY_URI));
		props.put(PROP_RDF_TYPE, l);
	}
	
	public Restriction appendTo(Restriction root, String[] path) {
		if (path == null  ||  path.length == 0)
			return null;
		if (!getOnProperty().equals(path[path.length-1]))
			return null;
		if (path.length == 1)
			if (root == null)
				return this;
			else
				return null;
		if (root == null) {
			root = new Restriction();
			root.setProperty(PROP_OWL_ON_PROPERTY, path[0]);
		} else if (!root.getOnProperty().equals(path[0]))
			return null;
		Restriction tmp = root;
		for (int i=1; i<path.length-1; i++)
			tmp = tmp.getRestrictionOnProperty(path[i]);
		ClassExpression all = (ClassExpression) tmp.props.get(PROP_OWL_ALL_VALUES_FROM);
		if (!(all instanceof Intersection)) {
			Intersection i = new Intersection();
			if (all != null)
				i.addType(all);
			tmp.props.put(PROP_OWL_ALL_VALUES_FROM, i);
			all = i;
		}
		((Intersection) all).addType(this);
		return root;
	}
	
	// -1 -> incompatible;   0 -> equal;   1 -> compatible
	private int checkValue(Object value, Hashtable context) {
		if (value == null  &&  hasVarRefAsValue
				&& props.containsKey(PROP_OWL_ALL_VALUES_FROM))
			return 1;
		
		Object myValue = props.get(PROP_OWL_HAS_VALUE);
		if (myValue == null)
			// no value restriction => all values are compatible
			return 1;

		if (myValue instanceof List)
			myValue = resolveVariables((List) myValue, context);
		else {
			List aux = new ArrayList(1);
			aux.add(Variable.resolveVarRef(myValue, context));
			myValue = aux;
		}
		
		if (value == null) {
			if (((List) myValue).size() == 1)
				myValue = ((List) myValue).get(0);
			else
				return -1;
			
			// an optional parameter without any existing and / or default value
			// means that null value is accepted; then we remark that under the
			// condition that this parameter remains null, the null value is acceptable;
			// for this purpose rdf:nil is used. An existing remark means that the above
			// was asserted previously
			if (RDF_EMPTY_LIST.equals(myValue))
				return 0;
			if (myValue instanceof Variable
					&& ((Variable) myValue).getMinCardinality() == 0
					&& ((Variable) myValue).getDefaultValue() == null) {
				context.put(((Variable) myValue).getURI(), RDF_EMPTY_LIST);
				return 0;
			}
			return -1;
		}
		
		if (value instanceof List)
			value = resolveVariables((List) value, context);
		else {
			List aux = new ArrayList(1);
			aux.add(Variable.resolveVarRef(value, context));
			value = aux;
		}
		
		return checkValueLists((List) myValue, (List) value, context);
	}
	
	private int checkValueLists(List first, List second, Hashtable context) {
		if (first.size() != second.size())
			return -1;
		Hashtable aux = new Hashtable(second.size());
		for (int i = 0;  i < first.size();  i++) {
			Object o = first.get(i);
			if (o instanceof Variable) {
				if (((Variable) o).getMinCardinality() > 1)
					return -1;
				boolean found = false;
				for (Iterator j=second.iterator(); !found && j.hasNext();) {
					Object oo = j.next();
					if (ManagedIndividual.checkMembership(
							((Variable) o).getParameterType(), oo)) {
						aux.put(((Variable) o).getURI(), oo);
						j.remove();
						found = true;
					}
				}
				if (!found)
					return -1;
			} else if (!second.remove(o)) {
				boolean found = false;
				for (Iterator j=second.iterator(); !found && j.hasNext();) {
					Object oo = j.next();
					if (oo instanceof Variable) {
						if (((Variable) oo).getMinCardinality() > 1)
							return -1;
						if (ManagedIndividual.checkMembership(
								((Variable) oo).getParameterType(), o)) {
							aux.put(((Variable) oo).getURI(), o);
							j.remove();
							found = true;
						}
					}
				}
				if (!found)
					return -1;
			}
		}
		if (!second.isEmpty())
			return -1;
		if (!aux.isEmpty())
			if (context == null)
				return -1;
			else {
				for (Iterator i=aux.keySet().iterator(); i.hasNext();) {
					Object key = i.next();
					context.put(key, aux.get(key));
				}
				return 1;
			}
		return 0;
	}
	
	public Restriction[] collectRestrictionsOnThePath(String[] path) {
		if (path == null  ||  path.length == 0  ||  !getOnProperty().equals(path[0]))
			return null;
		
		Restriction[] result = new Restriction[path.length];
		result[0] = this;
		
		for (int i=1; i<path.length; i++) {
			result[i] = result[i-1].getRestrictionOnPathElement(path[i]);
			if (result[i] == null) {
				while (++i < path.length)
					result[i] = null;
				break;
			}
		}
		
		return result;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#copy() */
	public ClassExpression copy() {
		Restriction copy =  new Restriction();
		for (Iterator i = props.keySet().iterator(); i.hasNext();) {
			String key = i.next().toString();
			Object o = props.get(key);
			if (o instanceof ClassExpression)
				o = ((ClassExpression) o).copy();
			copy.props.put(key, o);
		}
		return copy;
	}
	
	public Restriction copyOnNewProperty(String onProp) {
		Restriction r = (Restriction) copy();
		r.props.put(PROP_OWL_ON_PROPERTY, onProp);
		return r;
	}
	
	public Restriction copyWithNewCardinality(int max, int min) {
		if (max > -1  &&  max < min)
			return null;
		Restriction r = (Restriction) copy();
		if (min == max) {
			props.remove(PROP_OWL_MAX_CARDINALITY);
			props.remove(PROP_OWL_MIN_CARDINALITY);
			if (max > -1)
				props.put(PROP_OWL_CARDINALITY, new Integer(min));
		} else {
			props.remove(PROP_OWL_CARDINALITY);
			if (max < 0)
				props.remove(PROP_OWL_MAX_CARDINALITY);
			else
				props.put(PROP_OWL_MAX_CARDINALITY, new Integer(max));
			if (min < 1)
				props.remove(PROP_OWL_MIN_CARDINALITY);
			else
				props.put(PROP_OWL_MIN_CARDINALITY, new Integer(min));
		}
		
		return r;
	}
	
	public Object[] getEnumeratedValues() {
		ClassExpression all = (ClassExpression) props.get(PROP_OWL_ALL_VALUES_FROM);
		if (all instanceof Enumeration)
			return ((Enumeration) all).getUpperEnumeration();
		else if (all instanceof TypeURI)
			return ManagedIndividual.getEnumerationMembers(all.getURI());
		Object o = props.get(PROP_OWL_HAS_VALUE);
		return (o == null)? null : new Object[]{o};
	}

	public String getExpressionTypeURI() {
		return MY_URI;
	}
	
	public int getMaxCardinality() {
		Integer i = (Integer) props.get(PROP_OWL_MAX_CARDINALITY);
		if (i == null) {
			i = (Integer) props.get(PROP_OWL_CARDINALITY);
			if (i == null)
				return -1;
		}
		return i.intValue();
	}
	
	public int getMinCardinality() {
		Integer i = (Integer) props.get(PROP_OWL_MIN_CARDINALITY);
		if (i == null) {
			i = (Integer) props.get(PROP_OWL_CARDINALITY);
			if (i == null)
				return 0;
		}
		return i.intValue();
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#getNamedSuperclasses() */
	public String[] getNamedSuperclasses() {
		return new String[0];
	}
	
	public String getOnProperty() {
		Object o = props.get(PROP_OWL_ON_PROPERTY);
		return (o == null)? null : o.toString();
	}
	
	public String getPropTypeURI() {
		ClassExpression all = (ClassExpression) props.get(PROP_OWL_ALL_VALUES_FROM);
		return (all instanceof TypeURI)? ((TypeURI) all).getURI() : null;
	}
	
	public Restriction getRestrictionOnPath(String[] path) {
		if (path == null  ||  path.length == 0  ||  !getOnProperty().equals(path[0]))
			return null;
		
		Restriction tmp = this;
		for (int i=1; i<path.length  &&  tmp != null; i++)
			tmp = tmp.getRestrictionOnPathElement(path[i]);
		return tmp;
	}
	
	private Restriction getRestrictionOnPathElement(String pathElement) {
		ClassExpression all = (ClassExpression) props.get(PROP_OWL_ALL_VALUES_FROM);
		if (all instanceof Intersection)
			for (Iterator i = ((Intersection) all).types(); i.hasNext(); ) {
				ClassExpression tmp = (ClassExpression) i.next();
				if (tmp instanceof Restriction
						&&  ((Restriction) tmp).getOnProperty().equals(pathElement))
					return (Restriction) tmp;
			}
		else if (all instanceof TypeURI)
			return ManagedIndividual.getClassRestrictionsOnProperty(all.getURI(), pathElement);
		return (all instanceof Restriction
				&&  ((Restriction) all).getOnProperty().equals(pathElement))? (Restriction) all : null;
	}

	/**
	 * This method is specifically defined for working with property paths, hence whenever it is called
	 * it means that the path is already processed until the 'onProperty' of this Restriction and now 
	 * we are interested in the restrictions defined for the next property in the path (the property
	 * given as input parameter). Hence, the class set for 'allValuesFrom' must be checked.
	 */
	private Restriction getRestrictionOnProperty(String propURI) {
		ClassExpression all = (ClassExpression) props.get(PROP_OWL_ALL_VALUES_FROM);
		if (all instanceof Intersection) {
			for (Iterator i = ((Intersection) all).types(); i.hasNext(); ) {
				ClassExpression tmp = (ClassExpression) i.next();
				if (tmp instanceof Restriction  &&  ((Restriction) tmp).getOnProperty().equals(propURI))
					return (Restriction) tmp;
			}
		} else if (all instanceof Restriction  &&  ((Restriction) all).getOnProperty().equals(propURI)) {
			return (Restriction) all;
		} else {
			Intersection i = new Intersection();
			if (all != null)
				i.addType(all);
			props.put(PROP_OWL_ALL_VALUES_FROM, i);
			all = i;
		}
		Restriction r = new Restriction();
		r.setProperty(PROP_OWL_ON_PROPERTY, propURI);
		((Intersection) all).addType(r);
		return r;
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#getUpperEnumeration() */
	public Object[] getUpperEnumeration() {
		return new Object[0];
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object, Hashtable) */
	public boolean hasMember(Object o, Hashtable context) {
		int max = getMaxCardinality();
		if (max == 0  ||  !(o instanceof Resource))
			return o == null;
		
		o = ((Resource) o).getProperty(getOnProperty());
		switch (checkValue(o, context)) {
		case -1: return false;
		case 0: return true;
		}
		
		if (o == null)
			return getMinCardinality() < 1;
		
		return checkValueRestrictions(o, context, max);
	}

	public boolean hasMemberIgnoreCardinality(Object o) {
		if (!(o instanceof Resource))
			return o == null;
		
		o = ((Resource) o).getProperty(getOnProperty());
		if (o == null)
			return true;
		
		Object aux = props.get(PROP_OWL_HAS_VALUE);
		if (aux instanceof List)
			return (o instanceof List)? ((List) aux).containsAll((List) o)
					: ((List) aux).contains(o);
			
		aux = props.get(PROP_OWL_ALL_VALUES_FROM);
		if (aux instanceof ClassExpression)
			if (o instanceof List) {
				for (Iterator i=((List) o).iterator();  i.hasNext();)
					if (!((ClassExpression) aux).hasMember(i.next(), null))
						return false;
			} else
				return ((ClassExpression) aux).hasMember(o, null);
		
		return true;
	}
	
	private boolean checkValueRestrictions(Object o, Hashtable context, int max) {
		if (!(o instanceof List)) {
			List aux = new ArrayList(1);
			aux.add(o);
			o = aux;
		}
		
		int size = ((List) o).size();
		if ((max > 0  &&  size > max)  ||  size < getMinCardinality())
			return false;
		
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		ClassExpression from = (ClassExpression) props.get(PROP_OWL_ALL_VALUES_FROM);
		if (from != null)
			for (int i = 0;  i < size;  i++)
				if (!from.hasMember(((List) o).get(i), cloned))
					return false;
		
		from = (ClassExpression) props.get(PROP_OWL_SOME_VALUES_FROM);
		if (from != null) {
			for (int i = 0;  i < size;  i++)
				if (from.hasMember(((List) o).get(i), cloned)) {
					synchronize(context, cloned);
					return true;
				}
			return false;
		}
		
		synchronize(context, cloned);
		return true;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression, Hashtable) */
	public boolean isDisjointWith(ClassExpression other, Hashtable context) {
		if (!(other instanceof Restriction))
			return other.isDisjointWith(this, context);
		
		Restriction r = (Restriction) other;
		Object o = getOnProperty();
		if (o == null  ||  !o.equals(r.getOnProperty()))
			return false;

		o = r.props.get(PROP_OWL_HAS_VALUE);
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		switch (checkValue(o, cloned)) {
		case -1: // incompatible
			return true;
		case 0: // equal
			if (cloned == null  ||  cloned.size() == context.size())
				// unconditional equality
				return false;
			else
				// TODO: because the equality was conditional, there is still a chance to
				// return true by adopting complement conditions into context
				return false;
		}

		int max = getMaxCardinality();
		if (o != null) {
			if (checkValueRestrictions(o, cloned, max))
				if (cloned == null  ||  cloned.size() == context.size())
					// unconditional compatibility
					return false;
				else
					// TODO: because the compatibility was conditional, there is still a chance to
					// return true by adopting complement conditions into context
					return false;
			else
				return true;
		}
		
		if (max > -1  &&  r.getMinCardinality() > max)
			return true;
		
		max = r.getMaxCardinality();
		if (max > -1  &&  getMinCardinality() > max)
			return true;

		ClassExpression myValues = (ClassExpression) props.get(PROP_OWL_SOME_VALUES_FROM);
		if (myValues != null
				&& !myValues.isDisjointWith((ClassExpression) r.props.get(PROP_OWL_SOME_VALUES_FROM), cloned))
			return  false;
		
		myValues = (ClassExpression) props.get(PROP_OWL_ALL_VALUES_FROM);
		if (myValues != null
			 && myValues.isDisjointWith((ClassExpression) r.props.get(PROP_OWL_ALL_VALUES_FROM), cloned)) {
			synchronize(context, cloned);
			return true;
		}
		
		return false;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
	public boolean isWellFormed() {
		return getOnProperty() != null
			&& (props.containsKey(PROP_OWL_ALL_VALUES_FROM)
					|| props.containsKey(PROP_OWL_CARDINALITY)
					|| props.containsKey(PROP_OWL_HAS_VALUE)
					|| props.containsKey(PROP_OWL_MAX_CARDINALITY)
					|| props.containsKey(PROP_OWL_MIN_CARDINALITY)
					|| props.containsKey(PROP_OWL_SOME_VALUES_FROM));
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression, Hashtable) */
	public boolean matches(ClassExpression subtype, Hashtable context) {
		if (subtype == null)
			return false;
		
		if (subtype instanceof Enumeration)
			return ((Enumeration) subtype).hasSupertype(this, context);

		if (subtype instanceof TypeURI) {
			Restriction r = ManagedIndividual.getClassRestrictionsOnProperty(
					subtype.getURI(), getOnProperty());
			if (r == null)
				return false;
			subtype = r;
		}
		
		if (subtype instanceof Intersection)
			for (Iterator i=((Intersection) subtype).types(); i.hasNext();)
				if (matches((ClassExpression) i.next(), context))
					return true;
		
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		if (!(subtype instanceof Restriction)) {
			Object[] members = subtype.getUpperEnumeration();
			if (members != null  &&  members.length > 0) {
				for (int i=0; i<members.length; i++)
					if (!hasMember(members[i], cloned))
						return false;
				synchronize(context, cloned);
				return true;
			}
			String[] sups = subtype.getNamedSuperclasses();
			if (sups != null  &&  sups.length > 0) {
				for (int i=0; i<sups.length; i++) {
					Restriction r = ManagedIndividual.getClassRestrictionsOnProperty(sups[i],
							getOnProperty());
					if (r == null  ||  matches(r, context))
						return true;
				}
				return false;
			}
			if (subtype instanceof Union) {
				for (Iterator i=((Union) subtype).types(); i.hasNext();)
					if (!matches((ClassExpression) i.next(), context))
						return false;
				synchronize(context, cloned);
				return true;
			}
			return false;
		}
		
		Restriction other = (Restriction) subtype;
		if (!isWellFormed()
				||  !other.isWellFormed()
				||  !getOnProperty().equals(other.getOnProperty()))
			return false;
		
		Object o = other.props.get(PROP_OWL_HAS_VALUE);
		switch (checkValue(o, cloned)) {
		case -1: return false;
		case 0:
			synchronize(context, cloned);
			return true;
		}

		int myCard = getMaxCardinality();
		if (o != null
				&& !props.containsKey(PROP_OWL_HAS_VALUE)
				&& !(props.containsKey(PROP_OWL_ALL_VALUES_FROM)
						&& other.props.containsKey(PROP_OWL_ALL_VALUES_FROM)))
			return checkValueRestrictions(o, context, myCard);
		
		if (myCard > -1  &&  other.getMaxCardinality() > myCard)
			return false;
		
		myCard = getMinCardinality();
		if (myCard > 0  &&  other.getMinCardinality() < myCard)
			return false;
		
		ClassExpression myValues = (ClassExpression) props.get(PROP_OWL_SOME_VALUES_FROM);
		if (myValues != null
				&& !myValues.matches((ClassExpression) other.props.get(PROP_OWL_SOME_VALUES_FROM), cloned))
			return  false;
		
		myValues = (ClassExpression) props.get(PROP_OWL_ALL_VALUES_FROM);
		if (myValues == null
			 || myValues.matches((ClassExpression) other.props.get(PROP_OWL_ALL_VALUES_FROM), cloned)) {
			synchronize(context, cloned);
			return true;
		}
		
		return false;
	}
	
	public Restriction merge(Restriction other) {
		Object o = props.get(PROP_OWL_ON_PROPERTY);
		if (!(o instanceof Resource))
			return null;

		if (other == null  ||  !o.equals(other.props.get(PROP_OWL_ON_PROPERTY)))
			return this;
		
		Restriction res = (Restriction) copy();
		for (Iterator i = other.props.keySet().iterator(); i.hasNext();) {
			String key = i.next().toString();
			res.setProperty(key, other.getProperty(key));
		}
		
		return res;
	}
	
	public Restriction mergeWithNewCardinality(Restriction other, int max, int min) {
		Object o = props.get(PROP_OWL_ON_PROPERTY);
		if (!(o instanceof String))
			return null;

		if (other == null  ||  !o.equals(other.props.get(PROP_OWL_ON_PROPERTY)))
			return this;
		
		Restriction res = (Restriction) copyWithNewCardinality(max, min);
		for (Iterator i = other.props.keySet().iterator(); i.hasNext();) {
			String key = i.next().toString();
			res.setProperty(key, other.getProperty(key));
		}
		
		return res;
	}
	
	private List resolveVariables(List l, Hashtable context) {
		List result = new ArrayList(l.size());
		for (int i=0; i<l.size(); i++)
			result.add(Variable.resolveVarRef(l.get(i), context));
		return result;
	}

	/** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
	public void setProperty(String propURI, Object o) {
		if (o == null  ||  propURI == null  ||  props.containsKey(propURI))
			return;
		
		if (PROP_OWL_ON_PROPERTY.equals(propURI)) {
			if (o instanceof String)
				props.put(PROP_OWL_ON_PROPERTY, new Resource((String) o));
			else if (o instanceof Resource)
				props.put(PROP_OWL_ON_PROPERTY, new Resource(((Resource) o).getURI()));
			return;
		}
		
		int max = getMaxCardinality();
		if (max == 0  ||  props.containsKey(PROP_OWL_HAS_VALUE))
			return;

		ClassExpression all = (ClassExpression) props.get(PROP_OWL_ALL_VALUES_FROM);
		ClassExpression some = (ClassExpression) props.get(PROP_OWL_SOME_VALUES_FROM);
		
		if (PROP_OWL_ALL_VALUES_FROM.equals(propURI)) {
			Object tmp = TypeURI.asTypeURI(o);
			if (tmp != null)
				o = tmp;
			if (o instanceof ClassExpression
					&& all == null
					&& (some == null
							|| (max != 1 && ((ClassExpression) o).matches(some, null))))
				props.put(PROP_OWL_ALL_VALUES_FROM, o);
		} else if (PROP_OWL_CARDINALITY.equals(propURI)) {
			if (o instanceof Integer
					&& max == -1
					&& !props.containsKey(PROP_OWL_MIN_CARDINALITY)
					&& (((Integer) o).intValue() > 1
							|| (((Integer) o).intValue() == 1
									&& (some == null  ||  all == null))
							|| (((Integer) o).intValue() == 0
									&& all == null  &&  some == null)))
				props.put(PROP_OWL_CARDINALITY, o);
		} else if (PROP_OWL_HAS_VALUE.equals(propURI)) {
			if (max == -1  &&  all == null  &&  some == null
					&&  !props.containsKey(PROP_OWL_MIN_CARDINALITY)
					&&  Variable.checkDeserialization(o)) {
				props.put(PROP_OWL_HAS_VALUE, o);
				hasVarRefAsValue = Variable.isVarRef(o);
			}
		} else if (PROP_OWL_MAX_CARDINALITY.equals(propURI)) {
			int min = getMinCardinality();
			if (o instanceof Integer
					&& max == -1
					&& (((Integer) o).intValue() > 1
							|| (((Integer) o).intValue() == 1
									&& (some == null  ||  all == null))
							|| (((Integer) o).intValue() == 0
									&& all == null  &&  some == null)))
				if (min < ((Integer) o).intValue())
					props.put(PROP_OWL_MAX_CARDINALITY, o);
				else if (min == ((Integer) o).intValue()) {
					props.remove(PROP_OWL_MIN_CARDINALITY);
					props.put(PROP_OWL_CARDINALITY, o);
				}
		} else if (PROP_OWL_MIN_CARDINALITY.equals(propURI)) {
			if (o instanceof Integer
					&& ((Integer) o).intValue() > 0
					&& getMinCardinality() == 0)
				if (max < 0  ||  max > ((Integer) o).intValue())
					props.put(PROP_OWL_MIN_CARDINALITY, o);
				else if (max == ((Integer) o).intValue()) {
					props.remove(PROP_OWL_MAX_CARDINALITY);
					props.put(PROP_OWL_CARDINALITY, o);
				}
		} else if (PROP_OWL_SOME_VALUES_FROM.equals(propURI)) {
			Object tmp = TypeURI.asTypeURI(o);
			if (tmp != null)
				o = tmp;
			if (o instanceof ClassExpression
					&& some == null
					&& (all == null
							|| ((ClassExpression) all).matches((ClassExpression) o, null))) {
				if (all != null  &&  max == 1)
					props.remove(PROP_OWL_ALL_VALUES_FROM);
				props.put(PROP_OWL_SOME_VALUES_FROM, o);
			}
		}
	}
}
