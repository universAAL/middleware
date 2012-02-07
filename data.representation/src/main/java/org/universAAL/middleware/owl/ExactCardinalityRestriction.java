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
import java.util.List;

import org.universAAL.middleware.rdf.Resource;

/**
 * Implementation of OWL ExactCardinality Restriction: it contains all
 * individuals that are connected by the specified property to exact
 * <code>exact</code> individuals that are instances of the specified class
 * expression.
 * 
 * @author Carsten Stockloew
 */
public class ExactCardinalityRestriction extends AbstractRestriction {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "ExactCardinalityRestriction";

    public static final String PROP_OWL_CARDINALITY = OWL_NAMESPACE
	    + "cardinality";
    public static final String PROP_OWL_QUALIFIED_CARDINALITY = OWL_NAMESPACE
	    + "QualifiedCardinality";

    static {
	register(ExactCardinalityRestriction.class, null, PROP_OWL_CARDINALITY,
		null);
	// register(ExactCardinalityRestriction.class, null,
	// PROP_OWL_QUALIFIED_CARDINALITY, null);
    }

    /** Standard constructor for exclusive use by serializers. */
    ExactCardinalityRestriction() {
    }

    public ExactCardinalityRestriction(String propURI, int value) {
	if (propURI == null)
	    throw new NullPointerException();
	if (value < 0)
	    throw new IllegalArgumentException(
		    "Value of an Exact Cardinality Restriction must be non-negative: "
			    + value);
	setOnProperty(propURI);
	super.setProperty(PROP_OWL_CARDINALITY, new Integer(value));
    }

    public ExactCardinalityRestriction(String propURI, int value,
	    ClassExpression ce) {
	throw new UnsupportedOperationException("Not yet implemented");
	// setOnProperty(propURI);
	// super.setProperty(PROP_OWL_QUALIFIED_CARDINALITY, new
	// Integer(value));
    }

    public String getClassURI() {
	return MY_URI;
    }

    /** Get the value of this cardinality restriction */
    public int getValue() {
	Integer i = (Integer) props.get(PROP_OWL_CARDINALITY);
	if (i == null)
	    return 0;
	return i.intValue();
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#copy() */
    public ClassExpression copy() {
	return copyTo(new ExactCardinalityRestriction());
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object,
     *      Hashtable)
     */
    public boolean hasMember(Object member, Hashtable context) {
	if (member == null)
	    return false;

	Object value = ((Resource) member).getProperty(getOnProperty());

	if (value == null)
	    return getValue() == 0;

	if (!(value instanceof List))
	    return getValue() == 1;
	else
	    return getValue() == ((List) value).size();
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression,
     *      Hashtable)
     */
    public boolean isDisjointWith(ClassExpression other, Hashtable context) {
	if (!(other instanceof AbstractRestriction))
	    return other.isDisjointWith(this, context);

	AbstractRestriction r = (AbstractRestriction) other;
	Object o = getOnProperty();
	if (o == null || !o.equals(r.getOnProperty()))
	    return false;

	if (r instanceof MinCardinalityRestriction) {
	    if (getValue() < ((MinCardinalityRestriction) r).getValue())
		return true;
	} else if (r instanceof MaxCardinalityRestriction) {
	    if (getValue() > ((MaxCardinalityRestriction) r).getValue())
		return true;
	} else if (r instanceof ExactCardinalityRestriction) {
	    if (getValue() != ((ExactCardinalityRestriction) r).getValue())
		return true;
	}

	return false;
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
    public boolean isWellFormed() {
	return getOnProperty() != null && (hasProperty(PROP_OWL_CARDINALITY));
    }

    /**
     * @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression,
     *      Hashtable)
     */
    public boolean matches(ClassExpression subset, Hashtable context) {
	Object noRes = matchesNonRestriction(subset, context);
	if (noRes instanceof Boolean)
	    return ((Boolean) noRes).booleanValue();

	AbstractRestriction other = (AbstractRestriction) noRes;

	if (other instanceof MinCardinalityRestriction) {
	    return false;
	} else if (other instanceof MaxCardinalityRestriction) {
	    if (getValue() == 0
		    && ((MaxCardinalityRestriction) other).getValue() == 0)
		return true;
	} else if (other instanceof ExactCardinalityRestriction) {
	    if (getValue() == ((ExactCardinalityRestriction) other).getValue())
		return true;
	    else
		return false;
	}

	return false;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object o) {
	if (o == null || propURI == null || props.containsKey(propURI))
	    return;

	// handle this restriction
	if (PROP_OWL_CARDINALITY.equals(propURI)) {
	    if (o instanceof Integer) {
		Integer val = (Integer) o;
		if (val.intValue() < 0)
		    throw new IllegalArgumentException(
			    "Value of an Exact Cardinality Restriction must be non-negative: "
				    + val);
		super.setProperty(propURI, val);
	    }
	    return;
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
			.equals(ExactCardinalityRestriction.PROP_OWL_QUALIFIED_CARDINALITY)
		|| propURI
			.equals(AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM)
		|| propURI
			.equals(SomeValuesFromRestriction.PROP_OWL_SOME_VALUES_FROM)
		|| propURI
			.equals(BoundingValueRestriction.PROP_VALUE_HAS_MAX_EXCLUSIVE)
		|| propURI
			.equals(BoundingValueRestriction.PROP_VALUE_HAS_MAX_INCLUSIVE)
		|| propURI
			.equals(BoundingValueRestriction.PROP_VALUE_HAS_MIN_EXCLUSIVE)
		|| propURI
			.equals(BoundingValueRestriction.PROP_VALUE_HAS_MIN_INCLUSIVE))
	    return;

	// for everything else: call super
	super.setProperty(propURI, o);
    }
}
