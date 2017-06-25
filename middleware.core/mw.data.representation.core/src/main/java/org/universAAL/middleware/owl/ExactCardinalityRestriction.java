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
 * A {@link TypeExpression} ({@link PropertyRestriction}) that contains all
 * individuals that are connected by a given property to exactly a given number
 * of different instances of the given type expression.
 * ExactCardinalityRestriction corresponds to OWL ObjectExactCardinality or
 * DataExactCardinality.
 * <p>
 * <code>ExactCardinalityRestriction(n, property, te)</code> is equivalent to
 * <code>Intersection( MinCardinalityRestriction(n, property, te), MaxCardinalityRestriction(n, property, te) )</code>.
 *
 * @see MinCardinalityRestriction
 * @see MaxCardinalityRestriction
 *
 * @author Carsten Stockloew
 */
public final class ExactCardinalityRestriction extends PropertyRestriction {

	/** URI for this class. */
	public static final String MY_URI = VOCABULARY_NAMESPACE + "ExactCardinalityRestriction";

	/** URI for owl:cardinality. */
	public static final String PROP_OWL_CARDINALITY = OWL_NAMESPACE + "cardinality";

	/** URI for owl:QualifiedCardinality. */
	public static final String PROP_OWL_QUALIFIED_CARDINALITY = OWL_NAMESPACE + "QualifiedCardinality";

	/** Standard constructor for exclusive use by serializers. */
	ExactCardinalityRestriction() {
	}

	/**
	 * Constructor to create a new instance.
	 *
	 * @param propURI
	 *            URI of the property for which this restriction is defined.
	 * @param value
	 *            The exact cardinality that this property must have.
	 */
	public ExactCardinalityRestriction(String propURI, int value) {
		if (propURI == null)
			throw new NullPointerException();
		if (value < 0)
			throw new IllegalArgumentException(
					"Value of an Exact Cardinality Restriction must be non-negative: " + value);
		setOnProperty(propURI);
		super.setProperty(PROP_OWL_CARDINALITY, new NonNegativeInteger(value));
	}

	// public ExactCardinalityRestriction(String propURI, int value,
	// TypeExpression ce) {
	// throw new UnsupportedOperationException("Not yet implemented");
	// // setOnProperty(propURI);
	// // super.setProperty(PROP_OWL_QUALIFIED_CARDINALITY, new
	// // Integer(value));
	// }

	@Override
	public String getClassURI() {
		return MY_URI;
	}

	/**
	 * Get the value of this cardinality restriction.
	 *
	 * @return the value of this cardinality restriction
	 */
	public int getValue() {
		NonNegativeInteger i = (NonNegativeInteger) props.get(PROP_OWL_CARDINALITY);
		if (i == null)
			return -1;
		return i.intValue();
	}

	@Override
	public TypeExpression copy() {
		return copyTo(new ExactCardinalityRestriction());
	}

	@Override
	public boolean hasMember(Object member, HashMap context, int ttl, List<MatchLogEntry> log) {
		// ttl =
		checkTTL(ttl);
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

	@Override
	public boolean isDisjointWith(TypeExpression other, HashMap context, int ttl, List<MatchLogEntry> log) {
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
		} else if (r instanceof MaxCardinalityRestriction) {
			if (getValue() > ((MaxCardinalityRestriction) r).getValue())
				return true;
		} else if (r instanceof ExactCardinalityRestriction) {
			if (getValue() != ((ExactCardinalityRestriction) r).getValue())
				return true;
		}

		return false;
	}

	@Override
	public boolean isWellFormed() {
		return getOnProperty() != null && (hasProperty(PROP_OWL_CARDINALITY));
	}

	@Override
	public boolean matches(TypeExpression subset, HashMap context, int ttl, List<MatchLogEntry> log) {
		Object noRes = matchesNonRestriction(subset, context, ttl, log);
		if (noRes instanceof Boolean)
			return ((Boolean) noRes).booleanValue();

		PropertyRestriction other = (PropertyRestriction) noRes;

		if (other instanceof MinCardinalityRestriction) {
			return false;
		} else if (other instanceof MaxCardinalityRestriction) {
			if (getValue() == 0 && ((MaxCardinalityRestriction) other).getValue() == 0)
				return true;
		} else if (other instanceof ExactCardinalityRestriction) {
			return getValue() == ((ExactCardinalityRestriction) other).getValue();
		}

		return false;
	}

	@Override
	public boolean setProperty(String propURI, Object o) {
		if (o == null || propURI == null || props.containsKey(propURI))
			return false;

		// handle this restriction
		if (PROP_OWL_CARDINALITY.equals(propURI)) {
			if (o instanceof NonNegativeInteger) {
				return super.setProperty(propURI, o);
			}
			LogUtils.logError(SharedResources.moduleContext, ExactCardinalityRestriction.class, "setProperty",
					new Object[] { "Trying to set the exact cardinality with an invalid value: ", o, " of type ",
							o.getClass().getName(), ". It must be a NonNegativeInteger!" },
					null);
			return false;
		}

		// do not handle other restrictions
		if (propMap.contains(propURI))
			return false;

		// for everything else: call super
		return super.setProperty(propURI, o);
	}
}
