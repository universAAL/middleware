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
 * A {@link TypeExpression} ({@link PropertyRestriction}) that contains all
 * individuals that are connected by a given property to an instance of the
 * given type expression. SomeValuesFromRestriction corresponds to OWL
 * ObjectSomeValuesFrom or DataSomeValuesFrom.
 * <p>
 * <code>SomeValuesFromRestriction(property, te)</code> can be seen as a
 * syntactic shortcut for
 * <code>MinCardinalityRestriction( 1, property, te )</code>.
 * 
 * @see MinCardinalityRestriction
 * 
 * @author Carsten Stockloew
 */
public final class SomeValuesFromRestriction extends PropertyRestriction {

    /** URI for this class. */
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "SomeValuesFromRestriction";

    /** URI for owl:someValuesFrom. */
    public static final String PROP_OWL_SOME_VALUES_FROM = OWL_NAMESPACE
	    + "someValuesFrom";;

    /** Standard constructor for exclusive use by serializers. */
    SomeValuesFromRestriction() {
    }

    /**
     * Constructor to create a new instance.
     * 
     * @param propURI
     *            URI of the property for which this restriction is defined.
     * @param expr
     *            Expression for the type that at least one value of the
     *            property must have.
     */
    public SomeValuesFromRestriction(String propURI, TypeExpression expr) {
	if (propURI == null || expr == null)
	    throw new NullPointerException();
	setOnProperty(propURI);
	super.setProperty(PROP_OWL_SOME_VALUES_FROM, expr);
    }

    /** @see org.universAAL.middleware.owl.PropertyRestriction#getClassURI() */
    public String getClassURI() {
	return MY_URI;
    }

    /** @see org.universAAL.middleware.owl.PropertyRestriction#getConstraint() */
    public Object getConstraint() {
	return getProperty(PROP_OWL_SOME_VALUES_FROM);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	return copyTo(new SomeValuesFromRestriction());
    }

    /**
     * @see org.universAAL.middleware.owl.TypeExpression#hasMember(Object,
     *      HashMap, int, List)
     */
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

	TypeExpression from = (TypeExpression) props
		.get(PROP_OWL_SOME_VALUES_FROM);
	if (from != null) {
	    HashMap cloned = (context == null) ? null : (HashMap) context
		    .clone();
	    for (int i = 0; i < size; i++)
		if (from.hasMember(((List) o).get(i), cloned, ttl, log)) {
		    synchronize(context, cloned);
		    return true;
		}
	    return false;
	}
	return true;
    }

    /**
     * @see org.universAAL.middleware.owl.TypeExpression#isDisjointWith(TypeExpression,
     *      HashMap, int, List)
     */
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

	TypeExpression myValues = (TypeExpression) getProperty(PROP_OWL_SOME_VALUES_FROM);
	if (myValues != null
		&& !myValues.isDisjointWith((TypeExpression) r
			.getProperty(PROP_OWL_SOME_VALUES_FROM), cloned, ttl,
			log))
	    return false;

	return false;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#isWellFormed() */
    public boolean isWellFormed() {
	return getOnProperty() != null
		&& (hasProperty(PROP_OWL_SOME_VALUES_FROM));
    }

    /**
     * @see org.universAAL.middleware.owl.TypeExpression#matches(TypeExpression,
     *      HashMap, int, List)
     */
    public boolean matches(TypeExpression subset, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	Object noRes = matchesNonRestriction(subset, context, ttl, log);
	if (noRes instanceof Boolean)
	    return ((Boolean) noRes).booleanValue();

	// PropertyRestriction otherRes = (PropertyRestriction)noRes;

	// if (otherRes instanceof SomeValuesFromRestriction) {
	// Hashtable cloned = (context == null) ? null : (Hashtable) context
	// .clone();
	// ClassExpression my = (ClassExpression)
	// getProperty(PROP_OWL_SOME_VALUES_FROM);
	// ClassExpression other = (ClassExpression)
	// ((SomeValuesFromRestriction) otherRes)
	// .getProperty(PROP_OWL_SOME_VALUES_FROM);
	// if (my != null && other != null) {
	// if (!my.matches(other, cloned)) {
	// //synchronize(context, cloned);
	// //return true;
	// }
	// }
	// }

	return false;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public boolean setProperty(String propURI, Object o) {
	if (o == null || propURI == null || props.containsKey(propURI))
	    return false;

	// handle this restriction
	if (PROP_OWL_SOME_VALUES_FROM.equals(propURI)) {
	    TypeExpression some = (TypeExpression) getProperty(PROP_OWL_SOME_VALUES_FROM);
	    if (some != null)
		return false;

	    Object tmp = TypeURI.asTypeURI(o);
	    if (tmp != null)
		o = tmp;

	    return super.setProperty(PROP_OWL_SOME_VALUES_FROM, o);
	}

	// do not handle other restrictions
	if (propMap.contains(propURI))
	    return false;

	// for everything else: call super
	return super.setProperty(propURI, o);
    }
}
