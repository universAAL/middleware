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

import java.util.HashMap;
import java.util.List;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.MatchLogEntry;
import org.universAAL.middleware.xsd.NonNegativeInteger;

/**
 * Implementation of OWL MaxCardinality Restriction: it contains all individuals
 * that are connected by the specified property to at most <code>max</code>
 * individuals that are instances of the specified class expression.
 * 
 * @author Carsten Stockloew
 */
public final class MaxCardinalityRestriction extends PropertyRestriction {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "MaxCardinalityRestriction";

    public static final String PROP_OWL_MAX_CARDINALITY = OWL_NAMESPACE
	    + "maxCardinality";
    public static final String PROP_OWL_MAX_QUALIFIED_CARDINALITY = OWL_NAMESPACE
	    + "maxQualifiedCardinality";

    /** Standard constructor for exclusive use by serializers. */
    MaxCardinalityRestriction() {
    }

    public MaxCardinalityRestriction(String propURI, int value) {
	if (propURI == null)
	    throw new NullPointerException();
	if (value < 0)
	    throw new IllegalArgumentException(
		    "Value of a Max Cardinality Restriction must be non-negative: "
			    + value);
	setOnProperty(propURI);
	super.setProperty(PROP_OWL_MAX_CARDINALITY, new NonNegativeInteger(
		value));
    }

    public MaxCardinalityRestriction(String propURI, int value,
	    TypeExpression ce) {
	throw new UnsupportedOperationException("Not yet implemented");
	// setOnProperty(propURI);
	// super.setProperty(PROP_OWL_MAX_QUALIFIED_CARDINALITY, new
	// Integer(value));
    }

    public String getClassURI() {
	return MY_URI;
    }

    /** Get the value of this cardinality restriction */
    public final int getValue() {
	NonNegativeInteger i = (NonNegativeInteger) props
		.get(PROP_OWL_MAX_CARDINALITY);
	if (i == null)
	    return -1;
	return i.intValue();
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	return copyTo(new MaxCardinalityRestriction());
    }

    public boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	// ttl =
	checkTTL(ttl);
	if (member == null)
	    return false;

	Object value = ((Resource) member).getProperty(getOnProperty());

	if (value == null)
	    return true;

	if (!(value instanceof List))
	    return getValue() > 0;
	else
	    return getValue() >= ((List) value).size();
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

	if (r instanceof MinCardinalityRestriction) {
	    if (getValue() < ((MinCardinalityRestriction) r).getValue())
		return true;
	} else if (r instanceof ExactCardinalityRestriction) {
	    if (getValue() < ((ExactCardinalityRestriction) r).getValue())
		return true;
	}

	return false;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#isWellFormed() */
    public boolean isWellFormed() {
	return getOnProperty() != null
		&& (hasProperty(PROP_OWL_MAX_CARDINALITY));
    }

    public boolean matches(TypeExpression subset, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	Object noRes = matchesNonRestriction(subset, context, ttl, log);
	if (noRes instanceof Boolean)
	    return ((Boolean) noRes).booleanValue();

	PropertyRestriction other = (PropertyRestriction) noRes;

	if (other instanceof MaxCardinalityRestriction) {
	    if (getValue() >= ((MaxCardinalityRestriction) other).getValue())
		return true;
	} else if (other instanceof ExactCardinalityRestriction) {
	    if (getValue() >= ((ExactCardinalityRestriction) other).getValue())
		return true;
	    else
		return false;
	}

	return false;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public boolean setProperty(String propURI, Object o) {
	if (o == null || propURI == null || props.containsKey(propURI))
	    return false;

	// handle this restriction
	if (PROP_OWL_MAX_CARDINALITY.equals(propURI)) {
	    if (o instanceof NonNegativeInteger) {
		return super.setProperty(propURI, o);
	    }
	    LogUtils.logError(
		    SharedResources.moduleContext,
		    MaxCardinalityRestriction.class,
		    "setProperty",
		    new Object[] {
			    "Trying to set the max cardinality with an invalid value: ",
			    o, " of type ", o.getClass().getName(),
			    ". It must be a NonNegativeInteger!" }, null);
	    return false;
	}

	// do not handle other restrictions
	if (propMap.containsKey(propURI))
	    return false;

	// for everything else: call super
	return super.setProperty(propURI, o);
    }
}
