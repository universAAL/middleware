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

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.MatchLogEntry;

/**
 * Implementation of OWL AllValuesFrom Restriction: it contains all individuals
 * that are connected by the specified property to individuals that are
 * instances of the specified class expression.
 * 
 * @author Carsten Stockloew
 */
public final class AllValuesFromRestriction extends PropertyRestriction {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "AllValuesFromRestriction";

    public static final String PROP_OWL_ALL_VALUES_FROM = OWL_NAMESPACE
	    + "allValuesFrom";;

    /** Standard constructor for exclusive use by serializers. */
    AllValuesFromRestriction() {
    }

    public AllValuesFromRestriction(String propURI, TypeExpression expr) {
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

    public TypeExpression copy() {
	return copyTo(new AllValuesFromRestriction());
    }

    public boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
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

	HashMap cloned = (context == null) ? null : (HashMap) context.clone();
	TypeExpression from = (TypeExpression) props
		.get(PROP_OWL_ALL_VALUES_FROM);
	if (from != null)
	    for (int i = 0; i < size; i++)
		if (!from.hasMember(((List) o).get(i), cloned, ttl, log))
		    return false;

	synchronize(context, cloned);
	return true;
    }

    public boolean isDisjointWith(TypeExpression other, HashMap context,
	    int ttl, List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
	if (!(other instanceof PropertyRestriction))
	    return other.isDisjointWith(this, context, ttl, log);

	PropertyRestriction r = (PropertyRestriction) other;
	Object o = getOnProperty();
	if (o == null || !o.equals(r.getOnProperty()))
	    return false;

	HashMap cloned = (context == null) ? null : (HashMap) context.clone();

	TypeExpression myValues = (TypeExpression) getProperty(PROP_OWL_ALL_VALUES_FROM);
	if (myValues != null
		&& myValues.isDisjointWith(
			(TypeExpression) getProperty(PROP_OWL_ALL_VALUES_FROM),
			cloned, ttl, log)) {
	    synchronize(context, cloned);
	    return true;
	}

	return false;
    }

    public boolean isWellFormed() {
	return getOnProperty() != null
		&& (hasProperty(PROP_OWL_ALL_VALUES_FROM));
    }

    public boolean matches(TypeExpression subset, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	Object noRes = matchesNonRestriction(subset, context, ttl, log);
	if (noRes instanceof Boolean)
	    return ((Boolean) noRes).booleanValue();

	PropertyRestriction otherRes = (PropertyRestriction) noRes;

	if (otherRes instanceof AllValuesFromRestriction) {
	    HashMap cloned = (context == null) ? null : (HashMap) context
		    .clone();
	    TypeExpression my = (TypeExpression) getProperty(PROP_OWL_ALL_VALUES_FROM);
	    TypeExpression other = (TypeExpression) ((AllValuesFromRestriction) otherRes)
		    .getProperty(PROP_OWL_ALL_VALUES_FROM);
	    if (my != null && other != null) {
		if (my.matches(other, cloned, ttl, log)) {
		    synchronize(context, cloned);
		    return true;
		}
	    }
	}

	return false;
    }

    public boolean setProperty(String propURI, Object o) {
	if (o == null || propURI == null || props.containsKey(propURI))
	    return false;

	// handle this restriction
	if (PROP_OWL_ALL_VALUES_FROM.equals(propURI)) {
	    TypeExpression all = (TypeExpression) getProperty(PROP_OWL_ALL_VALUES_FROM);
	    if (all != null)
		return false;
	    Object tmp = TypeURI.asTypeURI(o);
	    if (tmp != null)
		o = tmp;

	    if (!(o instanceof TypeExpression))
		return false;
	    return super.setProperty(PROP_OWL_ALL_VALUES_FROM, o);
	}

	// do not handle other restrictions
	if (propMap.containsKey(propURI))
	    return false;

	// for everything else: call super
	return super.setProperty(propURI, o);
    }
}
