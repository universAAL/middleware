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
import java.util.List;

import org.universAAL.middleware.rdf.Resource;

/**
 * Implementation of OWL AllValuesFrom Restriction: it contains all individuals
 * that are connected by the specified property to individuals that are
 * instances of the specified class expression.
 * 
 * @author Carsten Stockloew
 */
public class AllValuesFromRestriction extends PropertyRestriction {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "AllValuesFromRestriction";

    public static final String PROP_OWL_ALL_VALUES_FROM = OWL_NAMESPACE
	    + "allValuesFrom";;

    static {
	register(AllValuesFromRestriction.class, null,
		PROP_OWL_ALL_VALUES_FROM, null);
    }

    /** Standard constructor for exclusive use by serializers. */
    AllValuesFromRestriction() {
    }

    public AllValuesFromRestriction(String propURI, ClassExpression expr) {
	if (propURI == null || expr == null)
	    throw new NullPointerException();
	setOnProperty(propURI);
	super.setProperty(PROP_OWL_ALL_VALUES_FROM, expr);
    }

    public AllValuesFromRestriction(String propURI, String typeURI) {
	this(propURI, TypeURI.asTypeURI(typeURI));
    }

    public String getClassURI() {
	return MY_URI;
    }

    public Object getConstraint() {
	return getProperty(PROP_OWL_ALL_VALUES_FROM);
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#copy() */
    public ClassExpression copy() {
	return copyTo(new AllValuesFromRestriction());
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object,
     *      Hashtable)
     */
    public boolean hasMember(Object member, Hashtable context) {
	if (!(member instanceof Resource))
	    return member == null;

	Object o = ((Resource) member).getProperty(getOnProperty());
	if (o == null)
	    return true;
	if (!(o instanceof List)) {
	    List aux = new ArrayList(1);
	    aux.add(o);
	    o = aux;
	}
	int size = ((List) o).size();

	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();
	ClassExpression from = (ClassExpression) props
		.get(PROP_OWL_ALL_VALUES_FROM);
	if (from != null)
	    for (int i = 0; i < size; i++)
		if (!from.hasMember(((List) o).get(i), cloned))
		    return false;

	synchronize(context, cloned);
	return true;
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression,
     *      Hashtable)
     */
    public boolean isDisjointWith(ClassExpression other, Hashtable context) {
	if (!(other instanceof PropertyRestriction))
	    return other.isDisjointWith(this, context);

	PropertyRestriction r = (PropertyRestriction) other;
	Object o = getOnProperty();
	if (o == null || !o.equals(r.getOnProperty()))
	    return false;

	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();

	ClassExpression myValues = (ClassExpression) getProperty(PROP_OWL_ALL_VALUES_FROM);
	if (myValues != null
		&& myValues
			.isDisjointWith(
				(ClassExpression) getProperty(PROP_OWL_ALL_VALUES_FROM),
				cloned)) {
	    synchronize(context, cloned);
	    return true;
	}

	return false;
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
    public boolean isWellFormed() {
	return getOnProperty() != null
		&& (hasProperty(PROP_OWL_ALL_VALUES_FROM));
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression,
     *      Hashtable)
     */
    public boolean matches(ClassExpression subset, Hashtable context) {
	Object noRes = matchesNonRestriction(subset, context);
	if (noRes instanceof Boolean)
	    return ((Boolean) noRes).booleanValue();

	PropertyRestriction otherRes = (PropertyRestriction) noRes;

	if (otherRes instanceof AllValuesFromRestriction) {
	    Hashtable cloned = (context == null) ? null : (Hashtable) context
		    .clone();
	    ClassExpression my = (ClassExpression) getProperty(PROP_OWL_ALL_VALUES_FROM);
	    ClassExpression other = (ClassExpression) ((AllValuesFromRestriction) otherRes)
		    .getProperty(PROP_OWL_ALL_VALUES_FROM);
	    if (my != null && other != null) {
		if (my.matches(other, cloned)) {
		    synchronize(context, cloned);
		    return true;
		}
	    }
	}

	return false;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object o) {
	if (o == null || propURI == null || props.containsKey(propURI))
	    return;

	// handle this restriction
	if (PROP_OWL_ALL_VALUES_FROM.equals(propURI)) {
	    ClassExpression all = (ClassExpression) getProperty(PROP_OWL_ALL_VALUES_FROM);
	    if (all != null)
		return;
	    Object tmp = TypeURI.asTypeURI(o);
	    if (tmp != null)
		o = tmp;

	    super.setProperty(PROP_OWL_ALL_VALUES_FROM, o);
	    return;
	}

	// do not handle other restrictions
	if (propMap.containsKey(propURI))
	    return;

	// for everything else: call super
	super.setProperty(propURI, o);
    }
}
