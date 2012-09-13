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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * <p>
 * Helper class to handle multiple {@link PropertyRestriction}s of the same
 * property.
 * </p>
 * <p>
 * There is often more than one restriction for a specific property, e.g. a
 * minimum cardinality restriction and a maximum cardinality restriction. All
 * these restrictions must be specified separately by an instance of
 * {@link PropertyRestriction}. This class helps to manage multiple restrictions
 * for the same property.
 * </p>
 * <p>
 * NOTE: If you add this class to another Resource with
 * {@link #setProperty(String, Object)}, it will behave like an
 * {@link Intersection} of the restrictions contained in this MergedRestriction.
 * </p>
 * 
 * @author Carsten Stockloew
 */
public class MergedRestriction extends Intersection {

    /** A safe iterator that does not allow to remove elements. */
    private class SafeIterator implements Iterator {
	protected Iterator it;

	SafeIterator(Iterator it) {
	    this.it = it;
	}

	public boolean hasNext() {
	    return it.hasNext();
	}

	public Object next() {
	    return it.next();
	}

	public void remove() {
	}
    }

    /** The value of minimum cardinality if not defined. */
    public static final int MIN_UNDEFINED = 0;

    /** The value of maximum cardinality if not defined. */
    public static final int MAX_UNDEFINED = -1;

    /**
     * ID of the {@link AllValuesFromRestriction}. Only used in the context of
     * this class.
     */
    public static final int allValuesFromID = 0;

    /**
     * ID of the {@link SomeValuesFromRestriction}. Only used in the context of
     * this class.
     */
    public static final int someValuesFromID = 1;

    /**
     * ID of the {@link HasValueRestriction}. Only used in the context of this
     * class.
     */
    public static final int hasValueID = 2;

    /**
     * ID of the {@link MinCardinalityRestriction}. Only used in the context of
     * this class.
     */
    public static final int minCardinalityID = 3;

    /**
     * ID of the {@link MaxCardinalityRestriction}. Only used in the context of
     * this class.
     */
    public static final int maxCardinalityID = 4;

    /**
     * ID of the {@link ExactCardinalityRestriction}. Only used in the context
     * of this class.
     */
    public static final int exactCardinalityID = 5;

    /**
     * Index of all {@link PropertyRestriction}s that are part of this
     * {@link MergedRestriction}. All restrictions are stored in an array (
     * <code>types</code> from the superclass). <code>index</code> points to the
     * correct Restriction in this array, e.g.
     * <code>index[allValuesFromID]</code> is the index in <code>types</code>
     * where the AllValuesFromRestriction is stored.
     */
    private int index[] = new int[6];

    /** The property for which this restriction is defined. */
    private String onProperty = null;

    /** Constructor for deserializers. */
    public MergedRestriction() {
	for (int i = 0; i < 6; i++)
	    index[i] = -1;
    }

    /**
     * Constructor for an empty set of restrictions. Restrictions can be added
     * by calling {@link #addRestriction(PropertyRestriction)} or
     * {@link #addRestriction(MergedRestriction)}.
     * 
     * @param onProperty
     *            The property for which this restriction is defined.
     */
    public MergedRestriction(String onProperty) {
	this();
	this.onProperty = onProperty;
    }

    /**
     * Constructor with a given initial set of restrictions.
     * 
     * @param onProperty
     *            The property for which this restriction is defined.
     * @param restrictions
     *            The initial set of restrictions. The array must contain only
     *            instances of {@link PropertyRestriction}.
     */
    public MergedRestriction(String onProperty, ArrayList restrictions) {
	this();
	setRestrictions(onProperty, restrictions);
    }

    /**
     * <p>
     * Create a new restriction to state that no individual of a certain class
     * has the given property, i.e. the maximum cardinality of the given
     * property is zero.
     * </p>
     * <p>
     * The new {@link MergedRestriction} contains
     * <ul>
     * <li>a {@link MaxCardinalityRestriction} with value <code>0</code>.
     * </ul>
     * </p>
     * 
     * @param propURI
     *            The property for which this restriction is defined.
     * @return the restriction, or <code>null</code> if the parameters are
     *         invalid.
     */
    public static final MergedRestriction getPropertyBanningRestriction(
	    String propURI) {
	if (propURI == null)
	    return null;
	MergedRestriction m = new MergedRestriction(propURI);
	m.addRestriction(new MaxCardinalityRestriction(propURI, 0));
	return m;
    }

    /**
     * <p>
     * Create a new restriction to state that all individuals of a certain class
     * must have the given value for the given property.
     * </p>
     * <p>
     * The new {@link MergedRestriction} contains
     * <ul>
     * <li>a {@link HasValueRestriction} with value <code>value</code>.
     * </ul>
     * </p>
     * 
     * @param propURI
     *            The property for which this restriction is defined.
     * @param value
     *            The value that the given property must have. If this value is
     *            a {@link java.lang.String} that contains a valid URI, then the
     *            value must be a {@link Resource} with this URI.
     * @return the restriction, or <code>null</code> if the parameters are
     *         invalid.
     */
    public static final MergedRestriction getFixedValueRestriction(
	    String propURI, Object value) {
	if (propURI == null || value == null)
	    return null;

	if (value instanceof String && isQualifiedName((String) value))
	    value = new Resource((String) value);

	MergedRestriction m = new MergedRestriction(propURI);
	m.addRestriction(new HasValueRestriction(propURI, value));
	return m;
    }

    /**
     * <p>
     * Create a new restriction to state that for all individuals of a certain
     * class the cardinality of the given property is at least <code>min</code>
     * and at most <code>max</code>.
     * </p>
     * <p>
     * The new {@link MergedRestriction} contains
     * <ul>
     * <li>a {@link MinCardinalityRestriction} if <code>min</code> is valid and
     * <code>min != max</code>.
     * <li>a {@link MaxCardinalityRestriction} if <code>max</code> is valid and
     * <code>min != max</code>.
     * <li>an {@link ExactCardinalityRestriction} if <code>min</code> and
     * <code>max</code> are valid and <code>min == max</code>.
     * </ul>
     * </p>
     * 
     * @param propURI
     *            The property for which this restriction is defined.
     * @param min
     *            The minimum cardinality, or <code>0</code> if undefined.
     * @param max
     *            The maximum cardinality, or <code>-1</code> if undefined.
     * @return the restriction, or <code>null</code> if the parameters are
     *         invalid.
     */
    public static final MergedRestriction getCardinalityRestriction(
	    String propURI, int min, int max) {
	if (propURI == null)
	    return null;
	if (max > -1 && max < min) { // invalid
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "getCardinalityRestriction",
			    new String[] { "Can not create the restriction because of invalid input parameters: "
				    + "the maximum cardinality is smaller than the minimum cardinality; this does not make sense." },
			    null);
	    return null;
	}
	if (max < 0 && min < 1) { // everything
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "getCardinalityRestriction",
			    new String[] { "Can not create the restriction because of invalid input parameters: "
				    + "both maximum cardinality and minimum cardinality are undefined. This is not a restriction because everything is allowed." },
			    null);
	    return null;
	}

	MergedRestriction ret = new MergedRestriction(propURI);

	if (min > 0 && min == max) {
	    ret.addRestriction(new ExactCardinalityRestriction(propURI, min));
	} else {
	    if (min > 0)
		ret.addRestriction(new MinCardinalityRestriction(propURI, min));
	    if (max >= 0)
		ret.addRestriction(new MaxCardinalityRestriction(propURI, max));
	}

	return ret;
    }

    /**
     * <p>
     * Create a new restriction to state that for all individuals of a certain
     * class the cardinality of the given property is at least <code>min</code>
     * and at most <code>max</code> and the value of the given property is of
     * type <code>typeURI</code>.
     * </p>
     * <p>
     * The new {@link MergedRestriction} contains
     * <ul>
     * <li>an {@link AllValuesFromRestriction}
     * <li>a {@link MinCardinalityRestriction} if <code>min</code> is valid and
     * <code>min != max</code>.
     * <li>a {@link MaxCardinalityRestriction} if <code>max</code> is valid and
     * <code>min != max</code>.
     * <li>an {@link ExactCardinalityRestriction} if <code>min</code> and
     * <code>max</code> are valid and <code>min == max</code>.
     * </ul>
     * </p>
     * 
     * @param propURI
     *            The property for which this restriction is defined.
     * @param min
     *            The minimum cardinality, or <code>0</code> if undefined.
     * @param max
     *            The maximum cardinality, or <code>-1</code> if undefined.
     * @param typeURI
     *            URI of the type of values for this property.
     * @return the restriction, or <code>null</code> if the parameters are
     *         invalid.
     */
    public static final MergedRestriction getAllValuesRestrictionWithCardinality(
	    String propURI, String typeURI, int min, int max) {
	if (typeURI == null)
	    return null;

	TypeURI type = null;

	if (TypeMapper.isRegisteredDatatypeURI(typeURI))
	    type = new TypeURI(typeURI, true);
	else if (OntologyManagement.getInstance().isRegisteredClass(typeURI,
		true))
	    type = new TypeURI(typeURI, false);

	if (type == null) {
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "getAllValuesRestrictionWithCardinality",
			    new String[] { "Can not create the restriction because of invalid input parameters: "
				    + "the specified type URI ("
				    + typeURI
				    + ") is not registered. It is neither a data type nor a registered ontology class." },
			    null);
	    return null;
	}

	return getAllValuesRestrictionWithCardinality(propURI, type, min, max);
    }

    /**
     * <p>
     * Create a new restriction to state that for all individuals of a certain
     * class the cardinality of the given property is at least <code>min</code>
     * and at most <code>max</code> and the value of the given property is of
     * type <code>expr</code>.
     * </p>
     * <p>
     * The new {@link MergedRestriction} contains
     * <ul>
     * <li>an {@link AllValuesFromRestriction}
     * <li>a {@link MinCardinalityRestriction} if <code>min</code> is valid and
     * <code>min != max</code>.
     * <li>a {@link MaxCardinalityRestriction} if <code>max</code> is valid and
     * <code>min != max</code>.
     * <li>an {@link ExactCardinalityRestriction} if <code>min</code> and
     * <code>max</code> are valid and <code>min == max</code>.
     * </ul>
     * </p>
     * 
     * @param propURI
     *            The property for which this restriction is defined.
     * @param min
     *            The minimum cardinality, or <code>0</code> if undefined.
     * @param max
     *            The maximum cardinality, or <code>-1</code> if undefined.
     * @param expr
     *            The type of values for this property.
     * @return the restriction, or <code>null</code> if the parameters are
     *         invalid.
     */
    public static final MergedRestriction getAllValuesRestrictionWithCardinality(
	    String propURI, TypeExpression expr, int min, int max) {
	if (expr == null)
	    return null;

	MergedRestriction ret = MergedRestriction.getCardinalityRestriction(
		propURI, min, max);
	if (ret != null)
	    ret.addRestriction(new AllValuesFromRestriction(propURI, expr));
	return ret;
    }

    /**
     * <p>
     * Create a new restriction to state that for all individuals of a certain
     * class the value of the given property is of type <code>typeURI</code>.
     * </p>
     * <p>
     * The new {@link MergedRestriction} contains
     * <ul>
     * <li>an {@link AllValuesFromRestriction}
     * </ul>
     * </p>
     * 
     * @param propURI
     *            The property for which this restriction is defined.
     * @param typeURI
     *            The type of values of the property.
     * @return the restriction, or <code>null</code> if the parameters are
     *         invalid.
     */
    public static final MergedRestriction getAllValuesRestriction(
	    String propURI, String typeURI) {
	if (typeURI == null || propURI == null)
	    return null;

	TypeURI type = null;

	if (TypeMapper.isRegisteredDatatypeURI(typeURI))
	    type = new TypeURI(typeURI, true);
	else if (OntologyManagement.getInstance().isRegisteredClass(typeURI,
		true))
	    type = new TypeURI(typeURI, false);

	if (type == null) {
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "getAllValuesRestriction",
			    new String[] { "Can not create the restriction because of invalid input parameters: "
				    + "the specified type URI ("
				    + typeURI
				    + ") is not registered. It is neither a data type nor a registered ontology class." },
			    null);
	    return null;
	}

	MergedRestriction ret = new MergedRestriction(propURI);
	ret.addRestriction(new AllValuesFromRestriction(propURI, type));
	return ret;
    }

    /**
     * <p>
     * Create a new restriction to state that for all individuals of a certain
     * class the value of the given property is of type <code>expr</code>.
     * </p>
     * <p>
     * The new {@link MergedRestriction} contains
     * <ul>
     * <li>an {@link AllValuesFromRestriction}
     * </ul>
     * </p>
     * 
     * @param propURI
     *            The property for which this restriction is defined.
     * @param expr
     *            The type of values of the property.
     * @return the restriction, or <code>null</code> if the parameters are
     *         invalid.
     */
    public static final MergedRestriction getAllValuesRestriction(
	    String propURI, TypeExpression expr) {
	if (expr == null || propURI == null)
	    return null;

	MergedRestriction ret = new MergedRestriction(propURI);
	ret.addRestriction(new AllValuesFromRestriction(propURI, expr));
	return ret;
    }

    /**
     * Get a list of {@link MergedRestriction}s from the specified list which
     * may contain restrictions for <i>different</i> properties. For each
     * property, there exists exactly one {@link MergedRestriction} in the
     * returned list.
     * 
     * @param o
     *            The list of {@link PropertyRestriction}s. Elements of this
     *            list that are not instances of {@link PropertyRestriction} are
     *            ignored. Restrictions can be defined for different properties.
     * @return The list of {@link MergedRestriction}.
     */
    public static ArrayList getFromList(List o) {
	// multiple restrictions for (possibly multiple) properties
	// -> build array of MergedRestrictions
	// temporary map:
	// ___key: property URI (String)
	// ___val: ArrayList of PropertyRestrictions
	// -> each ArrayList will be a MergedRestriction
	HashMap map = new HashMap();
	Object tmp;

	// build temporary map
	for (int i = 0; i < o.size(); i++) {
	    tmp = o.get(i);
	    if (tmp instanceof PropertyRestriction) {
		PropertyRestriction res = (PropertyRestriction) tmp;
		ArrayList a = (ArrayList) map.get(res.getOnProperty());
		if (a == null)
		    map.put(res.getOnProperty(), a = new ArrayList());
		a.add(res);
	    }
	}

	// create MergedRestrictions
	ArrayList ret = new ArrayList();
	Iterator it = map.keySet().iterator();
	while (it.hasNext()) {
	    String prop = (String) it.next();
	    ArrayList a = (ArrayList) map.get(prop);
	    MergedRestriction m = new MergedRestriction(prop, a);
	    ret.add(m);
	}
	return ret;
    }

    /**
     * Reset this object. The object is then in the same state like a newly
     * created object with {@link #MergedRestriction()}, i.e. the
     * {@link #onProperty} is not specified and it contains no
     * {@link PropertyRestriction}.
     */
    private void reset() {
	types.clear();
	onProperty = null;
	for (int i = 0; i < index.length; i++)
	    index[i] = -1;
    }

    /**
     * Get the minimum cardinality, if this object specifies one. The minimum
     * cardinality can be specified be either a
     * {@link MinCardinalityRestriction} or an
     * {@link ExactCardinalityRestriction}.
     * 
     * @return the minimum cardinality, or <code>0</code> if no minimum
     *         cardinality is specified.
     */
    public int getMinCardinality() {
	if (index[minCardinalityID] != -1)
	    return ((MinCardinalityRestriction) (types
		    .get(index[minCardinalityID]))).getValue();
	if (index[exactCardinalityID] != -1)
	    return ((ExactCardinalityRestriction) (types
		    .get(index[exactCardinalityID]))).getValue();
	return MIN_UNDEFINED;
    }

    /**
     * Get the maximum cardinality, if this object specifies one. The maximum
     * cardinality can be specified be either a
     * {@link MaxCardinalityRestriction} or an
     * {@link ExactCardinalityRestriction}.
     * 
     * @return the maximum cardinality, or <code>-1</code> if no maximum
     *         cardinality is specified.
     */
    public int getMaxCardinality() {
	if (index[maxCardinalityID] != -1)
	    return ((MaxCardinalityRestriction) (types
		    .get(index[maxCardinalityID]))).getValue();
	if (index[exactCardinalityID] != -1)
	    return ((ExactCardinalityRestriction) (types
		    .get(index[exactCardinalityID]))).getValue();
	return MAX_UNDEFINED;
    }

    /**
     * Get the constraint of a specific restriction, e.g. the minimum
     * cardinality for {@link MinCardinalityRestriction}. This method calls
     * {@link PropertyRestriction#getConstraint()}; the return value depends on
     * the individual restriction.
     * 
     * @param id
     *            ID of the restriction.
     * @return The constraint of the restriction.
     */
    public Object getConstraint(int id) {
	if (index[id] != -1)
	    return ((PropertyRestriction) types.get(index[id])).getConstraint();
	return null;
    }

    /**
     * Set a list of restrictions. The object is first reset before new
     * restrictions are set. This method is similar to calling the constructor
     * {@link #MergedRestriction(String, ArrayList)} with the only difference
     * that no new object is created.
     * 
     * @param onProperty
     *            The property for which this restriction is defined.
     * @param restrictions
     *            The list of restrictions. The array must contain only
     *            instances of {@link PropertyRestriction}.
     */
    private void setRestrictions(String onProperty, ArrayList restrictions) {
	if (restrictions == null || onProperty == null)
	    throw new NullPointerException();
	reset();
	types.addAll(restrictions);
	this.onProperty = onProperty;
	try {
	    analyze();
	} catch (IllegalArgumentException e) {
	    reset();
	    throw e;
	}
    }

    /**
     * Get all {@link PropertyRestriction}s. The list is backed by the
     * {@link MergedRestriction}, so changes to the {@link MergedRestriction}
     * are reflected in the list.
     * 
     * @return an unmodifiable list of restrictions.
     */
    public List getRestrictions() {
	return Collections.unmodifiableList(types);
    }

    /**
     * Remove a restriction.
     * 
     * @param id
     *            ID of the restriction to remove.
     */
    private void removeRestriction(int id) {
	int index = this.index[id];
	if (index < 0 || index >= types.size())
	    return;

	// remove element
	types.remove(index);

	// adapt indices
	for (int i = 0; i < this.index.length; i++)
	    if (this.index[i] > index)
		this.index[i]--;
    }

    /**
     * Get a specific {@link PropertyRestriction}.
     * 
     * @param id
     *            ID of the property restriction.
     * @return the property restriction.
     */
    public PropertyRestriction getRestriction(int id) {
	if (index[id] == -1)
	    return null;
	return (PropertyRestriction) types.get(index[id]);
    }

    /**
     * Add a new Restriction, performing a sanity check. It is not guaranteed
     * that the given restriction will really be added, e.g. when this
     * MergedRestriction already has a {@link MinCardinalityRestriction} and a
     * {@link MaxCardinalityRestriction} with the same value is added, then the
     * {@link MinCardinalityRestriction} is removed and an
     * {@link ExactCardinalityRestriction} is added instead.
     * 
     * @param res
     *            The Restriction to add.
     * @return this restriction. This object is returned to allow for multiple
     *         calls of this method.
     * @throws IllegalArgumentException
     *             If the given restriction is defined for a different property
     *             than this merged restriction.
     */
    public MergedRestriction addRestriction(PropertyRestriction res) {
	// if (types.size()==1) {
	// addType(res);
	// onProperty = res.getOnProperty();
	// return this;
	// }
	if (res == null)
	    return this;

	if (onProperty != null)
	    if (!onProperty.equals(res.getOnProperty()))
		throw new IllegalArgumentException(
			"Trying to add a restriction for a different property. All restrictions of a MergedRestriction must be defined for the same property.");

	int max = getMaxCardinality();
	if (max == 0) {
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "addRestriction",
			    new String[] { "Can not add the PropertyRestriction ("
				    + res.getType()
				    + ") because the maximum cardinality is 0 (no additional restriction is allowed)." },
			    null);
	    return this;
	}

	// id points to the appropriate element in 'index' of the given
	// Restriction
	int id = getID(res);

	PropertyRestriction all = getRestriction(allValuesFromID);
	PropertyRestriction some = getRestriction(someValuesFromID);

	switch (id) {
	case allValuesFromID:
	    if (all == null
		    && (some == null || (max != 1 && res.matches(some, null)))) {
		index[allValuesFromID] = types.size();
		types.add(res);
	    } else {
		if (all != null)
		    LogUtils
			    .logDebug(
				    SharedResources.moduleContext,
				    MergedRestriction.class,
				    "addRestriction",
				    new String[] { "Can not add the AllValuesFromRestriction because such a restriction is already set." },
				    null);
		else
		    LogUtils
			    .logDebug(
				    SharedResources.moduleContext,
				    MergedRestriction.class,
				    "addRestriction",
				    new String[] { "Can not add the AllValuesFromRestriction, please check with your SomeValuesFromRestriction." },
				    null);
	    }
	    break;
	case someValuesFromID:
	    if (some == null
		    && (all == null || ((TypeExpression) all)
			    .matches(res, null))) {
		if (all != null && max == 1)
		    removeRestriction(allValuesFromID);
		index[someValuesFromID] = types.size();
		types.add(res);
	    }
	    break;
	case hasValueID:
	    PropertyRestriction has = getRestriction(hasValueID);
	    if (has == null) {
		if (max != 0) {
		    index[hasValueID] = types.size();
		    types.add(res);
		} else {
		    LogUtils
			    .logDebug(
				    SharedResources.moduleContext,
				    MergedRestriction.class,
				    "addRestriction",
				    new String[] { "Can not add the HasValueRestriction because the maximum cardinality is set to zero, so no instances are allowed." },
				    null);
		}
	    } else {
		LogUtils
			.logDebug(
				SharedResources.moduleContext,
				MergedRestriction.class,
				"addRestriction",
				new String[] { "Can not add the HasValueRestriction because such a restriction is already set." },
				null);
	    }
	    break;
	case minCardinalityID:
	    int newMin = ((MinCardinalityRestriction) res).getValue();
	    if (getMinCardinality() == MIN_UNDEFINED)
		if (max == MAX_UNDEFINED || max > newMin) {
		    if (index[minCardinalityID] == -1) {
			index[minCardinalityID] = types.size();
			types.add(res);
		    }
		} else if (max == newMin) {
		    removeRestriction(maxCardinalityID);
		    res = new ExactCardinalityRestriction(onProperty, newMin);
		    if (index[exactCardinalityID] == -1) {
			index[exactCardinalityID] = types.size();
			types.add(res);
		    } else {
			types.set(index[exactCardinalityID], res);
		    }
		}
	    break;
	case maxCardinalityID:
	    int newMax = ((MaxCardinalityRestriction) res).getValue();
	    int min = getMinCardinality();
	    if (max == MAX_UNDEFINED
		    && (newMax > 1
			    || (newMax == 1 && (some == null || all == null)) || (newMax == 0
			    && all == null && some == null)))
		if (min < newMax) {
		    index[maxCardinalityID] = types.size();
		    types.add(res);
		} else if (min == newMax) {
		    removeRestriction(minCardinalityID);
		    res = new ExactCardinalityRestriction(onProperty, newMax);
		    index[exactCardinalityID] = types.size();
		    types.add(res);
		}
	    break;
	case exactCardinalityID:
	    int newExact = ((ExactCardinalityRestriction) res).getValue();
	    if (max == MAX_UNDEFINED
		    && getRestriction(minCardinalityID) == null
		    && (newExact > 1
			    || (newExact == 1 && (some == null || all == null)) || (newExact == 0
			    && all == null && some == null)))
		if (index[exactCardinalityID] == -1) {
		    index[exactCardinalityID] = types.size();
		    types.add(res);
		} else {
		    types.set(index[exactCardinalityID], res);
		}
	    break;
	}
	return this;
    }

    /**
     * Add all restrictions of the given {@link MergedRestriction}, performing a
     * sanity check. It is not guaranteed that the given restriction will really
     * be added, e.g. when this {@link MergedRestriction} already has a
     * {@link MinCardinalityRestriction} and a {@link MaxCardinalityRestriction}
     * with the same value is added, then the {@link MinCardinalityRestriction}
     * is removed and an {@link ExactCardinalityRestriction} is added instead.
     * 
     * @param r
     *            The restriction to add.
     * @return this restriction. This object is returned to allow for multiple
     *         calls of this method.
     */
    public MergedRestriction addRestriction(MergedRestriction r) {
	ArrayList resList = (ArrayList) r.types;
	for (int i = 0; i < resList.size(); i++)
	    addRestriction((PropertyRestriction) ((TypeExpression) resList
		    .get(i)).copy());
	return this;
    }

    /**
     * Get the ID of the specified restriction.
     * 
     * @param res
     *            Restriction for which to return the ID.
     * @return ID of the specified restriction.
     */
    private int getID(PropertyRestriction res) {
	int idx = -1;
	if (res instanceof AllValuesFromRestriction)
	    idx = allValuesFromID;
	else if (res instanceof SomeValuesFromRestriction)
	    idx = someValuesFromID;
	else if (res instanceof HasValueRestriction)
	    idx = hasValueID;
	else if (res instanceof MinCardinalityRestriction)
	    idx = minCardinalityID;
	else if (res instanceof MaxCardinalityRestriction)
	    idx = maxCardinalityID;
	else if (res instanceof ExactCardinalityRestriction)
	    idx = exactCardinalityID;
	else
	    throw new IllegalArgumentException("Unknown Restriction type: "
		    + res + " (Class URI: " + res.getClassURI() + ")");
	return idx;
    }

    /**
     * Analyze the list of restrictions to check for invalid data. The
     * restrictions are stored in {@link Intersection#types} and are checked for
     * duplicate restrictions and restriction that are defined for a different
     * property. Additionally, the {@link #index} is checked. This analysis is
     * necessary after setting an unknown list of restriction, e.g. after
     * calling {@link #setRestrictions(String, ArrayList)}.
     */
    private void analyze() {
	for (int i = 0; i < types.size(); i++) {
	    if (!(types.get(i) instanceof PropertyRestriction))
		throw new IllegalArgumentException(
			"Non-restriction found at index: " + i);

	    PropertyRestriction res = (PropertyRestriction) types.get(i);
	    if (!onProperty.equals(res.getOnProperty()))
		throw new IllegalArgumentException(
			"Restriction defined for wrong property: "
				+ res.getClassURI() + " " + res.getOnProperty());

	    int idx = getID(res);
	    if (idx != -1 && index[idx] != -1)
		throw new IllegalArgumentException("Duplicate Restriction: "
			+ res.getClassURI());
	    index[idx] = i;
	}
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	ArrayList newList = new ArrayList();
	for (int i = 0; i < types.size(); i++)
	    newList.add(((PropertyRestriction) types.get(i)).copy());
	return new MergedRestriction(onProperty, newList);
    }

    /**
     * Create a new {@link MergedRestriction} with modified cardinality
     * restrictions.
     * 
     * @param min
     *            The new value for {@link MinCardinalityRestriction}.
     * @param max
     *            The new value for {@link MaxCardinalityRestriction}.
     * @return The new {@link MergedRestriction}.
     */
    public MergedRestriction copyWithNewCardinality(int min, int max) {
	if (max > -1 && max < min)
	    return null;
	MergedRestriction r = (MergedRestriction) copy();
	r.removeRestriction(minCardinalityID);
	r.removeRestriction(maxCardinalityID);
	r.removeRestriction(exactCardinalityID);
	r.addRestriction(new MinCardinalityRestriction(getOnProperty(), min));
	r.addRestriction(new MaxCardinalityRestriction(getOnProperty(), max));
	return r;
    }

    /**
     * Create a new {@link MergedRestriction} with modified onProperty value.
     * 
     * @param onProp
     *            The new URI of the property this restriction is defined for.
     * @return the new {@link MergedRestriction} which contains all simple
     *         restrictions of this class, but is defined for a different
     *         property.
     */
    public MergedRestriction copyOnNewProperty(String onProp) {
	MergedRestriction r = (MergedRestriction) copy();
	for (Iterator i = types.iterator(); i.hasNext();)
	    ((Resource) i.next()).setProperty(
		    PropertyRestriction.PROP_OWL_ON_PROPERTY, onProp);
	onProperty = onProp;
	return r;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#isWellFormed() */
    public boolean isWellFormed() {
	for (int i = 0; i < types.size(); i++)
	    if (!((TypeExpression) types.get(i)).isWellFormed())
		return false;
	return true;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object o) {
	// TODO: handle the restriction-related stuff here?
	// for now, it is not necessary, the restrictions themselves implement
	// the setProperty() method and perform a sanity-check for the
	// restriction itself, and the addRestriction() method performs the
	// sanity check for the set of restrictions.
    }

    /** Get an iterator for the added child class expressions. */
    public Iterator types() {
	// safe iterator, so that elements cannot be removed (this would destroy
	// our index)
	return new SafeIterator(types.iterator());
    }

    /**
     * If this MergedRestriction restricts the values of a property to be
     * individuals of a specific class (i.e. an {@link AllValuesFromRestriction}
     * of a {@link TypeURI}), then return the URI of this class.
     * 
     * @return The URI of the class the property must be an individual of.
     */
    public String getPropTypeURI() {
	TypeExpression all = null;

	if (index[allValuesFromID] == -1)
	    return null;

	all = (TypeExpression) ((AllValuesFromRestriction) types
		.get(index[allValuesFromID])).getConstraint();

	return (all instanceof TypeURI) ? ((TypeURI) all).getURI() : null;
    }

    public String getOnProperty() {
	return onProperty;
    }

    /**
     * Appends this restriction to the given root restriction on the given
     * property path.
     * 
     * @param root
     *            The root restriction for the first element of the property
     *            path. Can be null.
     * @param path
     *            The property path.
     * @return <b>null</b> if the parameters are invalid because of either<br>
     *         <ul>
     *         <li><code>path</code> is <i>null</i></li>
     *         <li><code>path</code> is empty</li>
     *         <li>the <code>onProperty</code> of this object is not set</li>
     *         <li>the <code>onProperty</code> does not match the last element
     *         of the <code>path</code></li>
     *         <li>the <code>onProperty</code> of <code>root</code> does not
     *         match the first element of the <code>path</code></li>
     *         </ul>
     *         a <b>{@link #MergedRestriction()}</b>that is either<br>
     *         <ul>
     *         <li><b><code>this</code></b> if <code>root</code> is <i>null</i>
     *         and the length of <code>path</code> is one</li>
     *         <li>a modified <b><code>root</code></b> (or a new
     *         {@link #MergedRestriction()} if <code>root</code> is <i>null</i>)
     *         otherwise</li>
     *         </ul>
     */
    public MergedRestriction appendTo(MergedRestriction root, String[] path) {
	// System.out.println("appending \n" + this.toStringRecursive());
	// System.out.println("\n\nto\n"
	// + (root == null ? "null" : root.toStringRecursive()));
	if (path == null || path.length == 0) {
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "appendTo",
			    new String[] { "Not possible to append a restriction because the specified path is invalid: "
				    + path == null ? "path is null."
				    : "path is empty." }, null);
	    return null;
	}
	if (getOnProperty() == null) {
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "appendTo",
			    new String[] { "Not possible to append a restriction because it does not define a property for which this restriction applies: the onProperty must be set." },
			    null);
	    return null;
	}
	if (!getOnProperty().equals(path[path.length - 1])) {
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "appendTo",
			    new String[] { "Not possible to append a restriction because "
				    + "the restriction is not defined for the property at the end of the property path: the onProperty value of the restriction must correspond to the last element of the property path." },
			    null);
	    return null;
	}
	if (path.length == 1)
	    if (root == null) {
		return this;
	    } else {
		// add this restriction to the given list of restrictions, if it
		// is not yet available
		root.addRestriction(this);
		return root;
	    }
	if (root == null) {
	    root = new MergedRestriction(path[0]);
	    PropertyRestriction r = new AllValuesFromRestriction();
	    r.setProperty(PropertyRestriction.PROP_OWL_ON_PROPERTY, path[0]);
	    root.addRestriction(r);
	} else {
	    // just a test: are all restrictions in root defined for the correct
	    // property?
	    if (!root.getOnProperty().equals(path[0])) {
		LogUtils
			.logDebug(
				SharedResources.moduleContext,
				MergedRestriction.class,
				"appendTo",
				new String[] { "Not possible to append a new restriction to an existing restriction because "
					+ "the existing restriction is defined for a different property than what is specified in the property path:\n"
					+ "the existing restriction restriction is defined for property "
					+ root.getOnProperty()
					+ "\nand the property path starts with "
					+ path[0] }, null);
		return null;
	    }
	}

	// get the AllValuesFromRestriction in root
	PropertyRestriction tmp = root.getRestriction(allValuesFromID);
	if (tmp == null) {
	    // we couldn't find the AllValuesFromRestriction in the root array
	    // -> create an empty one here
	    tmp = new AllValuesFromRestriction();
	    tmp.setOnProperty(path[0]);
	    root.addRestriction(tmp);
	}
	tmp = root.getRestriction(allValuesFromID);
	if (tmp == null) {
	    LogUtils
		    .logDebug(
			    SharedResources.moduleContext,
			    MergedRestriction.class,
			    "appendTo",
			    new String[] { "The root object does not contain an AllValuesFromRestriction and it is not possible to add a new AllValuesFromRestriction. Maybe root already contains a different restriction that prevents the AllValuesFromRestriction from being added (e.g. a SomeValuesFromRestriction or a HasValueRestriction)." },
			    null);
	    return null;
	}

	// tmp is now the root AllValuesFromRestriction
	// -> follow the AllValuesFromRestriction along the path, create
	// appropriate Restrictions and Intersections, if necessary
	for (int i = 1; i < path.length - 1; i++)
	    tmp = tmp.getRestrictionOnProperty(path[i]);
	// System.out.println("TEMP ROOT\n" + root.toStringRecursive());
	// System.out.println("TMP (last AllValues)\n" +
	// tmp.toStringRecursive());

	// tmp now points to the AllValuesFromRestriction of the path element
	// before the last path element
	TypeExpression all = (TypeExpression) tmp.getConstraint();
	if (all == null && types.size() == 1) {
	    // there is only one property restriction at the end of the path, so
	    // we don't need an intersection
	    // just add the new restriction from ~this~
	    tmp.changeProperty(
		    AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM, types
			    .get(0));
	} else {
	    // there are multiple property restrictions at the end of the path,
	    // they are all in an intersection

	    if (!(all instanceof Intersection)) {
		Intersection i = new Intersection();
		if (all != null)
		    i.addType(all);
		tmp.changeProperty(
			AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM, i);
		all = i;
	    }

	    // add all restrictions of ~this~ MergedRestriction (which is an
	    // Intersection and can't be set directly)
	    Intersection isec = (Intersection) all;
	    for (int i = 0; i < types.size(); i++)
		isec.addType((TypeExpression) types.get(i));
	}
	return root;
    }

    /**
     * Get a {@link MergedRestriction} of a property path previously set by
     * {@link #appendTo(MergedRestriction, String[])}
     * 
     * @param path
     *            The property path.
     * @return The {@link MergedRestriction} that is defined for the last
     *         element of the property path
     */
    public MergedRestriction getRestrictionOnPath(String[] path) {
	if (path == null || path.length == 0
		|| !getOnProperty().equals(path[0]))
	    return null;

	MergedRestriction tmp = this;
	for (int i = 1; i < path.length && tmp != null; i++)
	    tmp = tmp.getRestrictionOnPathElement(path[i]);
	return tmp;
    }

    private MergedRestriction getRestrictionOnPathElement(String pathElement) {
	TypeExpression all = (TypeExpression) getConstraint(allValuesFromID);
	if (all instanceof Intersection) {
	    // create a new MergedRestriction and collect all simple
	    // Restrictions
	    MergedRestriction m = new MergedRestriction(pathElement);
	    PropertyRestriction tmpRes;
	    for (Iterator i = ((Intersection) all).types(); i.hasNext();) {
		TypeExpression tmp = (TypeExpression) i.next();
		if (tmp instanceof PropertyRestriction) {
		    tmpRes = (PropertyRestriction) tmp;
		    if (tmpRes.getOnProperty().equals(pathElement))
			m.addRestriction(tmpRes);
		}
	    }
	    return m;
	} else if (all instanceof TypeURI)
	    return ManagedIndividual.getClassRestrictionsOnProperty(all
		    .getURI(), pathElement);

	if (all instanceof PropertyRestriction) {
	    PropertyRestriction res = (PropertyRestriction) all;
	    if (res.getOnProperty().equals(pathElement)) {
		MergedRestriction m = new MergedRestriction(pathElement);
		m.addRestriction(res);
		return m;
	    }
	}

	return null;
    }

    /**
     * Get the set of instances.
     * 
     * @return an array of all known instances.
     */
    public Object[] getEnumeratedValues() {
	int idx;

	idx = index[allValuesFromID];
	if (idx < 0)
	    return null;
	AllValuesFromRestriction allres = (AllValuesFromRestriction) types
		.get(idx);
	if (allres != null) {
	    TypeExpression all = (TypeExpression) allres.getConstraint();
	    if (all instanceof Enumeration)
		return ((Enumeration) all).getUpperEnumeration();
	    else if (all instanceof TypeURI) {
		OntClassInfo info = OntologyManagement.getInstance()
			.getOntClassInfo(all.getURI());
		return info == null ? null : info.getInstances();
	    }
	}

	idx = index[hasValueID];
	if (idx < 0)
	    return null;
	HasValueRestriction hasres = (HasValueRestriction) types.get(idx);
	if (hasres != null) {
	    TypeExpression has = (TypeExpression) hasres.getConstraint();
	    return (has == null) ? null : new Object[] { has };
	}

	return null;
    }

    /**
     * Create a new {@link MergedRestriction} that is a combination of this
     * restriction and the given restriction. If some parts are defined in both
     * restrictions, then the parts from <code>other</code> will be preferred.
     * 
     * @param other
     *            The restriction to merge with
     * @return <p>
     *         null if this restriction is not fully defined (has no
     *         onProperty).
     *         </p>
     *         <p>
     *         this if the onProperty of this object does not match the
     *         onProperty of the given object.
     *         </p>
     *         <p>
     *         A new MergedRestriction that combines all
     *         {@link PropertyRestriction}s of this object and the given object.
     *         </p>
     */
    public MergedRestriction merge(MergedRestriction other) {
	String onThis = getOnProperty();
	if (onThis == null)
	    return null;

	if (other == null)
	    return this;

	String onOther = other.getOnProperty();
	if (onOther == null || !onThis.equals(onOther))
	    return this;

	MergedRestriction res = (MergedRestriction) other.copy();
	res.addRestriction(this);
	return res;
    }

    /**
     * Returns true if the given object is a member of the class represented by
     * this class expression, otherwise false; cardinality restrictions are
     * ignored.
     * 
     * @param o
     *            The object to test for membership.
     * @return true, if the given object is a member of this class expression.
     */
    public boolean hasMemberIgnoreCardinality(Object o) {
	int j = 0;
	for (Iterator i = types.iterator(); i.hasNext();) {
	    if (j != minCardinalityID && j != maxCardinalityID
		    && j != exactCardinalityID)
		if (!((TypeExpression) i.next()).hasMember(o, null))
		    return false;
	    j++;
	}
	return true;
    }
}
