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
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.util.MatchLogEntry;

/**
 * A {@link TypeExpression} ({@link PropertyRestriction}) that contains all
 * individuals that are connected by a given property only to
 * individuals/literals that are contained in the given type expression.
 * AllValuesFromRestriction corresponds to OWL ObjectAllValuesFrom or
 * DataAllValuesFrom.
 * <p>
 * <code>AllValuesFromRestriction(property, te)</code> can be seen as a
 * syntactic shortcut for
 * <code>MaxCardinalityRestriction( 0, property, Complement( te ) ))</code>.
 * <p>
 * Compared to Java, the class in the following code (defining an instance
 * variable called <code>property</code>):
 * 
 * <pre>
 * public class MyTE {
 *     String property;
 * }
 * </pre>
 * 
 * is similar to the following type expression:
 * 
 * <pre>
 * AllValuesFromRestriction(property,
 * 	TypeURI(TypeMapper.getDatatypeURI(String.class)))
 * </pre>
 * 
 * @see MaxCardinalityRestriction
 * @see Complement
 * @see TypeMapper
 * @see TypeURI
 * 
 * @author Carsten Stockloew
 */
public final class AllValuesFromRestriction extends PropertyRestriction {

    /** URI for this class. */
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "AllValuesFromRestriction";

    /** URI for owl:allValuesFrom. */
    public static final String PROP_OWL_ALL_VALUES_FROM = OWL_NAMESPACE
	    + "allValuesFrom";;

    /** Standard constructor for exclusive use by serializers. */
    AllValuesFromRestriction() {
    }

    /**
     * Constructor to create a new instance.
     * 
     * @param propURI
     *            URI of the property for which this restriction is defined.
     * @param expr
     *            Expression for the type that all values of the property must
     *            have.
     */
    public AllValuesFromRestriction(String propURI, TypeExpression expr) {
	if (propURI == null || expr == null)
	    throw new NullPointerException();
	setOnProperty(propURI);
	super.setProperty(PROP_OWL_ALL_VALUES_FROM, expr);
    }

    /**
     * Constructor to create a new instance.
     * 
     * @param propURI
     *            URI of the property for which this restriction is defined.
     * @param typeURI
     *            URI of the type that all values of the property must have.
     *            Creates a new {@link TypeURI} and calls
     *            {@link AllValuesFromRestriction#AllValuesFromRestriction(String, TypeExpression)}
     *            .
     */
    public AllValuesFromRestriction(String propURI, String typeURI) {
	this(propURI, TypeURI.asTypeURI(typeURI));
    }

    @Override
    public String getClassURI() {
	return MY_URI;
    }

    @Override
    public Object getConstraint() {
	return getProperty(PROP_OWL_ALL_VALUES_FROM);
    }

    @Override
    public TypeExpression copy() {
	return copyTo(new AllValuesFromRestriction());
    }

    @Override
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

    @Override
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

    @Override
    public boolean isWellFormed() {
	return getOnProperty() != null
		&& (hasProperty(PROP_OWL_ALL_VALUES_FROM));
    }

    @Override
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

    @Override
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
	if (propMap.contains(propURI))
	    return false;

	// for everything else: call super
	return super.setProperty(propURI, o);
    }
}
