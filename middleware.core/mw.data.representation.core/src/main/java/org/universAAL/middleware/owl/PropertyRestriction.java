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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.universAAL.middleware.rdf.Resource;

/**
 * The base class of all simple restrictions. Subclasses will implement the
 * functionality for specific OWL or XSD restrictions.
 * 
 * All restrictions are defined by a property and one ore more constraints and
 * describe those individuals that are connected by the defined property to
 * individuals under the condition that the defined constraint is fulfilled.
 * 
 * @author Carsten Stockloew
 */
public abstract class PropertyRestriction extends TypeExpression {

    public static final String MY_URI = OWL_NAMESPACE + "Restriction";
    public static final String PROP_OWL_ON_PROPERTY = OWL_NAMESPACE
	    + "onProperty";

    protected static HashMap propMap = new HashMap();

    static {
	// for not handling properties of other restrictions, to be used in
	// setProperty
	propMap.put(HasValueRestriction.PROP_OWL_HAS_VALUE, null);
	propMap.put(MinCardinalityRestriction.PROP_OWL_MIN_CARDINALITY, null);
	propMap.put(
		MinCardinalityRestriction.PROP_OWL_MIN_QUALIFIED_CARDINALITY,
		null);
	propMap.put(MaxCardinalityRestriction.PROP_OWL_MAX_CARDINALITY, null);
	propMap.put(
		MaxCardinalityRestriction.PROP_OWL_MAX_QUALIFIED_CARDINALITY,
		null);
	propMap.put(ExactCardinalityRestriction.PROP_OWL_CARDINALITY, null);
	propMap.put(ExactCardinalityRestriction.PROP_OWL_QUALIFIED_CARDINALITY,
		null);
	propMap.put(AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM, null);
	propMap.put(SomeValuesFromRestriction.PROP_OWL_SOME_VALUES_FROM, null);
    }

    PropertyRestriction() {
	addType(MY_URI, true);
	ArrayList l = new ArrayList(1);
	l.add(new Resource(MY_URI));
	props.put(PROP_RDF_TYPE, l);
    }

    /**
     * Get the class URI for this {@link PropertyRestriction}.
     * 
     * @return The class URI.
     */
    public String getClassURI() {
	return MY_URI;
    }

    public Object getConstraint() {
	return null;
    }

    protected void setOnProperty(String propURI) {
	super.setProperty(PROP_OWL_ON_PROPERTY, new Resource(propURI));
    }

    public String getOnProperty() {
	Object o = props.get(PROP_OWL_ON_PROPERTY);
	return (o == null) ? null : o.toString();
    }

    /**
     * Helper method to copy Restrictions.
     * 
     * @see org.universAAL.middleware.owl.TypeExpression#copy()
     */
    protected TypeExpression copyTo(PropertyRestriction copy) {
	for (Iterator i = props.keySet().iterator(); i.hasNext();) {
	    String key = i.next().toString();
	    Object o = props.get(key);
	    if (o instanceof TypeExpression)
		o = ((TypeExpression) o).copy();
	    copy.props.put(key, o);
	}
	return copy;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getNamedSuperclasses() */
    public String[] getNamedSuperclasses() {
	return new String[0];
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getUpperEnumeration() */
    public Object[] getUpperEnumeration() {
	return new Object[0];
    }

    /**
     * Check all cases where the subtype is not a subclass of
     * {@link #PropertyRestriction()}. Helper method for
     * {@link #matches(TypeExpression, Hashtable)} in subclasses.
     * 
     * @see org.universAAL.middleware.owl.TypeExpression#matches(TypeExpression,
     *      Hashtable)
     */
    protected Object matchesNonRestriction(TypeExpression subtype,
	    Hashtable context) {
	if (subtype == null)
	    return Boolean.FALSE;

	if (subtype instanceof Enumeration)
	    return new Boolean(((Enumeration) subtype).hasSupertype(this,
		    context));

	if (subtype instanceof TypeURI) {
	    // TODO: change
	    MergedRestriction r = ManagedIndividual
		    .getClassRestrictionsOnProperty(subtype.getURI(),
			    getOnProperty());
	    if (r == null)
		return Boolean.FALSE;
	    subtype = r;
	}

	if (subtype instanceof Intersection)
	    for (Iterator i = ((Intersection) subtype).types(); i.hasNext();)
		if (matches((TypeExpression) i.next(), context))
		    return Boolean.TRUE;

	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();
	if (!(subtype instanceof PropertyRestriction)) {
	    Object[] members = subtype.getUpperEnumeration();
	    if (members != null && members.length > 0) {
		for (int i = 0; i < members.length; i++)
		    if (!hasMember(members[i], cloned))
			return Boolean.FALSE;
		synchronize(context, cloned);
		return Boolean.TRUE;
	    }
	    String[] sups = subtype.getNamedSuperclasses();
	    if (sups != null && sups.length > 0) {
		for (int i = 0; i < sups.length; i++) {
		    // TODO: change
		    MergedRestriction r = ManagedIndividual
			    .getClassRestrictionsOnProperty(sups[i],
				    getOnProperty());
		    if (r == null || matches(r, context))
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	    }
	    if (subtype instanceof Union) {
		for (Iterator i = ((Union) subtype).types(); i.hasNext();)
		    if (!matches((TypeExpression) i.next(), context))
			return Boolean.FALSE;
		synchronize(context, cloned);
		return Boolean.TRUE;
	    }
	    return Boolean.FALSE;
	}

	PropertyRestriction other = (PropertyRestriction) subtype;
	if (!isWellFormed() || !other.isWellFormed()
		|| !getOnProperty().equals(other.getOnProperty()))
	    return Boolean.FALSE;

	return other;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object value) {
	// the subclasses already checked the input parameters, so they are not
	// null

	// handle owl:onProperty
	if (PROP_OWL_ON_PROPERTY.equals(propURI)) {
	    if (value instanceof String)
		props.put(PROP_OWL_ON_PROPERTY, new Resource((String) value));
	    else if (value instanceof Resource)
		props.put(PROP_OWL_ON_PROPERTY, new Resource(((Resource) value)
			.getURI()));
	    return;
	}

	// for everything else: call super
	// TODO: should we really do this?
	super.setProperty(propURI, value);
    }

    /* *******************************************
     * Methods for handling Array of Restrictions
     * *******************************************
     */

    /**
     * Add this restriction to the given set of restrictions.
     * 
     * @param a
     *            The set of Restrictions.
     * @return True, if the restriction was added.
     */
    public boolean addToList(ArrayList a) {
	if (a == null)
	    return false;

	PropertyRestriction r = getRestriction(a, getClassURI());
	if (r == null) {
	    // the restriction is not yet in the array
	    a.add(this);
	    return true;
	}

	// the restriction is already in the array
	// TODO: should we merge?
	return false;
    }

    /**
     * Get a restriction with the given class URI from the given set of
     * restrictions. The class URI of a specific restriction can be retrieved
     * using either Restriction.MY_URI or {@link #PropertyRestriction
     * #getClassURI()}.
     * 
     * @param a
     *            The set of Restrictions.
     * @param restrictionURI
     *            The URI of the Restriction class to search for.
     * @return The restriction, or null, if no restriction of the given type
     *         could be found in the given set.
     */
    public static PropertyRestriction getRestriction(ArrayList a,
	    String restrictionURI) {
	if (restrictionURI == null || a == null)
	    return null;
	PropertyRestriction r = null;
	for (int i = 0; i < a.size(); i++) {
	    r = (PropertyRestriction) (a.get(i));
	    if (r.getClassURI().equals(restrictionURI))
		return r;
	}
	return null;
    }

    /**
     * This method is specifically defined for working with property paths,
     * hence whenever it is called it means that the path is already processed
     * until the 'onProperty' of this Restriction and now we are interested in
     * the restrictions defined for the next property in the path (the property
     * given as input parameter). Hence, the class set for 'allValuesFrom' must
     * be checked.
     */
    public AllValuesFromRestriction getRestrictionOnProperty(String propURI) {
	TypeExpression all = (TypeExpression) getProperty(AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM);
	if (all instanceof Intersection) {
	    // If the intersection already has an AllValuesFromRestriction for
	    // the given property, return it.
	    for (Iterator i = ((Intersection) all).types(); i.hasNext();) {
		TypeExpression tmp = (TypeExpression) i.next();
		if (tmp instanceof AllValuesFromRestriction
			&& ((AllValuesFromRestriction) tmp).getOnProperty()
				.equals(propURI))
		    return (AllValuesFromRestriction) tmp;
	    }
	} else if (all instanceof AllValuesFromRestriction
		&& ((AllValuesFromRestriction) all).getOnProperty().equals(
			propURI)) {
	    return (AllValuesFromRestriction) all;
	}

	if (!(all instanceof Intersection)) {
	    Intersection i = new Intersection();
	    if (all != null)
		i.addType(all);
	    props.put(AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM, i);
	    all = i;
	}

	// 'all' is now an intersection
	// create an AllValuesFrom Restriction to return
	AllValuesFromRestriction r = new AllValuesFromRestriction();
	r.setProperty(PROP_OWL_ON_PROPERTY, new Resource(propURI));
	((Intersection) all).addType(r);
	return r;
    }
}
