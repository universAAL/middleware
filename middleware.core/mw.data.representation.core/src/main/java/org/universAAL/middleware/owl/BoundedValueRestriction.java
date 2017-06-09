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
import java.util.ListIterator;

import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;
import org.universAAL.middleware.util.MatchLogEntry;

/**
 * Base class for all {@link TypeRestriction}s that define a minimum or maximum
 * value on a given data type. Implementation of XSD Value Restrictions: it
 * contains all literals that meet the specified conditions. These conditions
 * are either:
 * <ol>
 * <li>min inclusive</li>
 * <li>min exclusive</li>
 * <li>max inclusive</li>
 * <li>max exclusive</li>
 * </ol>
 *
 * It is possible to define a condition on the minimum value and on the maximum
 * value in the same {@link BoundedValueRestriction}.
 * <p>
 * Sub classes provide helper methods for specific data types.
 *
 * @author Carsten Stockloew
 */
public abstract class BoundedValueRestriction extends TypeRestriction {

	/** URI for the facet xsd:minInclusive. */
	protected static final String XSD_FACET_MIN_INCLUSIVE;

	/** URI for the facet xsd:maxInclusive. */
	protected static final String XSD_FACET_MAX_INCLUSIVE;

	/** URI for the facet xsd:minExclusive. */
	protected static final String XSD_FACET_MIN_EXCLUSIVE;

	/** URI for the facet xsd:maxExclusive. */
	protected static final String XSD_FACET_MAX_EXCLUSIVE;

	/**
	 * The minimum value, or null if no minimum is defined. Can also be a
	 * {@link Variable} reference.
	 */
	private Object min = null;

	/**
	 * The maximum value, or null if no maximum is defined. Can also be a
	 * {@link Variable} reference.
	 */
	private Object max = null;

	/** True, if the minimum value is included. */
	private boolean minInclusive;

	/** True, if the maximum value is included. */
	private boolean maxInclusive;

	static {
		XSD_FACET_MIN_INCLUSIVE = TypeMapper.XSD_NAMESPACE + "minInclusive";
		XSD_FACET_MAX_INCLUSIVE = TypeMapper.XSD_NAMESPACE + "maxInclusive";
		XSD_FACET_MIN_EXCLUSIVE = TypeMapper.XSD_NAMESPACE + "minExclusive";
		XSD_FACET_MAX_EXCLUSIVE = TypeMapper.XSD_NAMESPACE + "maxExclusive";
	}

	/**
	 * Standard constructor.
	 *
	 * @param datatypeURI
	 *            URI of the data type for which this restriction is defined.
	 *            Must be one of the supported data types.
	 */
	protected BoundedValueRestriction(String datatypeURI) {
		super(datatypeURI);
	}

	/**
	 * Constructor.
	 *
	 * @param datatypeURI
	 *            URI of the data type for which this restriction is defined.
	 *            Must be one of the supported data types.
	 * @param min
	 *            The minimum value, or a {@link Variable}, or null if no
	 *            minimum is defined.
	 * @param minInclusive
	 *            True, if the minimum value is included. Ignored, if min is
	 *            null.
	 * @param max
	 *            The maximum value, or a {@link Variable}, or null if no
	 *            maximum is defined.
	 * @param maxInclusive
	 *            True, if the maximum value is included. Ignored, if max is
	 *            null.
	 */
	protected BoundedValueRestriction(String datatypeURI, Object min, boolean minInclusive, Object max,
			boolean maxInclusive) {
		super(datatypeURI);

		if (min == null && max == null)
			throw new NullPointerException("Either min or max must be not null.");

		if (min instanceof Comparable && max instanceof Comparable) {
			if (((Comparable) min).compareTo((Comparable) max) > 0)
				throw new IllegalArgumentException("min can not be greater than max.");
		}

		setFacets(min, minInclusive, max, maxInclusive);
	}

	/**
	 * Determines whether the type of the given object is valid. Sub classes
	 * should override this method to check for their type and call super to
	 * check whether it is a {@link Variable}.
	 *
	 * @param o
	 *            The object to check.
	 * @return true, if the type is valid.
	 */
	protected boolean checkType(Object o) {
		return Variable.isVarRef(o);
	}

	/**
	 * Set the constraining facets for min and max.
	 *
	 * @param min
	 *            The minimum value, or a {@link Variable}, or null if no
	 *            minimum is defined.
	 * @param minInclusive
	 *            True, if the minimum value is included. Ignored, if min is
	 *            null.
	 * @param max
	 *            The maximum value, or a {@link Variable}, or null if no
	 *            maximum is defined.
	 * @param maxInclusive
	 *            True, if the maximum value is included. Ignored, if max is
	 *            null.
	 */
	private void setFacets(Object min, boolean minInclusive, Object max, boolean maxInclusive) {
		setMinFacet(min, minInclusive);
		setMaxFacet(max, maxInclusive);
	}

	/**
	 * Set the constraining facets for min.
	 *
	 * @param min
	 *            The minimum value, or a {@link Variable}, or null if no
	 *            minimum is defined.
	 * @param minInclusive
	 *            True, if the minimum value is included. Ignored, if min is
	 *            null.
	 */
	private void setMinFacet(Object min, boolean minInclusive) {
		if (!checkType(min))
			return;
		if (min != null) {
			if (minInclusive)
				addConstrainingFacet(XSD_FACET_MIN_INCLUSIVE, min);
			else
				addConstrainingFacet(XSD_FACET_MIN_EXCLUSIVE, min);
		}
		this.min = min;
		this.minInclusive = minInclusive;
	}

	/**
	 * Set the constraining facets for max.
	 *
	 * @param max
	 *            The maximum value, or a {@link Variable}, or null if no
	 *            maximum is defined.
	 * @param maxInclusive
	 *            True, if the maximum value is included. Ignored, if max is
	 *            null.
	 */
	private void setMaxFacet(Object max, boolean maxInclusive) {
		if (!checkType(min))
			return;
		if (max != null) {
			if (maxInclusive)
				addConstrainingFacet(XSD_FACET_MAX_INCLUSIVE, max);
			else
				addConstrainingFacet(XSD_FACET_MAX_EXCLUSIVE, max);
		}
		this.max = max;
		this.maxInclusive = maxInclusive;
	}

	/**
	 * Copy the facets for min and max to a different
	 * {@link BoundedValueRestriction}.
	 *
	 * @param copy
	 *            The object to which to copy the facets.
	 * @return the value given as parameter, but with the restrictions copied.
	 * @see org.universAAL.middleware.owl.TypeExpression#copy()
	 */
	protected TypeExpression copyTo(BoundedValueRestriction copy) {
		copy.setFacets(min, minInclusive, max, maxInclusive);
		return super.copyTo(copy);
	}

	/**
	 * Returns the minimum value.
	 *
	 * @return the minimum value, or null if not defined or an instance of
	 *         {@link Variable}.
	 */
	public Comparable<?> getLowerbound() {
		if (min instanceof Comparable)
			return (Comparable<?>) min;
		return null;
	}

	/**
	 * Returns the maximum value.
	 *
	 * @return the maximum value, or null if not defined or an instance of
	 *         {@link Variable}.
	 */
	public Comparable<?> getUpperbound() {
		if (max instanceof Comparable)
			return (Comparable<?>) max;
		return null;
	}

	@Override
	public boolean setProperty(String propURI, Object o) {
		if (o == null || propURI == null)
			return false;

		if (PROP_OWL_WITH_RESTRICTIONS.equals(propURI)) {
			if (min == null && max == null) {
				// values are not set yet
				// retrieve the values from the given object and set them
				// o must be a list
				if (o instanceof List) {
					// each element of the list is a constraining facet and is
					// one (anonymous) Resource with only one property
					ListIterator it = ((List) o).listIterator();
					Facet facet;

					while ((facet = iterate(it)) != null) {
						// check for correct datatype
						if (getTypeURI().equals(TypeMapper.getDatatypeURI(facet.value.getClass()))) {

							// process the facet
							if (XSD_FACET_MIN_INCLUSIVE.equals(facet.facetURI)) {
								setMinFacet(facet.value, true);
							} else if (XSD_FACET_MIN_EXCLUSIVE.equals(facet.facetURI)) {
								setMinFacet(facet.value, false);
							} else if (XSD_FACET_MAX_INCLUSIVE.equals(facet.facetURI)) {
								setMaxFacet(facet.value, true);
							} else if (XSD_FACET_MAX_EXCLUSIVE.equals(facet.facetURI)) {
								setMaxFacet(facet.value, false);
							} else {
								super.setFacet(facet);
							}
						}
					}
					return true;
				}
			}
			return false;
		}

		// call super for other properties (or for more general facets)
		return super.setProperty(propURI, o);
	}

	@Override
	public boolean hasMember(Object member, HashMap context, int ttl, List<MatchLogEntry> log) {
		HashMap cloned = (context == null) ? null : (HashMap) context.clone();

		if (!hasMember(member, cloned, ttl, log, min, minInclusive, max, maxInclusive))
			return false;

		if (!super.hasMember(member, cloned, ttl, log))
			return false;

		synchronize(context, cloned);
		return true;
	}

	@Override
	public boolean isDisjointWith(TypeExpression other, HashMap context, int ttl, List<MatchLogEntry> log) {
		// ttl =
		checkTTL(ttl);
		if (other instanceof BoundedValueRestriction) {

			boolean min1Incl = minInclusive;
			boolean max1Incl = maxInclusive;
			boolean min2Incl = ((BoundedValueRestriction) other).minInclusive;
			boolean max2Incl = ((BoundedValueRestriction) other).maxInclusive;

			Object min1 = Variable.resolveVarRef(min, context);
			Object max1 = Variable.resolveVarRef(max, context);
			Object min2 = Variable.resolveVarRef(((BoundedValueRestriction) other).min, context);
			Object max2 = Variable.resolveVarRef(((BoundedValueRestriction) other).max, context);

			return (max1 instanceof Comparable && min2 instanceof Comparable
					&& (((Comparable) max1).compareTo(min2) < 0
							|| (((Comparable) max1).compareTo(min2) == 0 && (!max1Incl || !min2Incl))))
					|| (max2 instanceof Comparable && min1 instanceof Comparable
							&& (((Comparable) max2).compareTo(min1) < 0
									|| (((Comparable) max2).compareTo(min1) == 0 && (!max2Incl || !min1Incl))));
		}

		return false;
	}

	@Override
	public boolean isWellFormed() {
		return restrictions.size() > 0;
	}

	@Override
	public boolean matches(TypeExpression subset, HashMap context, int ttl, List<MatchLogEntry> log) {
		// ttl =
		checkTTL(ttl);
		// TODO: check other ClassExpressions (e.g. Union..)

		if (!(subset instanceof BoundedValueRestriction))
			return false;

		BoundedValueRestriction other = (BoundedValueRestriction) subset;
		if (!isWellFormed() || !other.isWellFormed())
			return false;

		return matches(other.min, other.minInclusive, other.max, other.maxInclusive, context, ttl, log, min,
				minInclusive, max, maxInclusive);
	}
}
