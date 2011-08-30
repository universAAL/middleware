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
import org.universAAL.middleware.rdf.Resource;

/**
 * Helper class to handle multiple {@link AbstractRestriction}s of the same
 * property.
 * 
 * NOTE: If you add this class to another Resource with
 * {@link #setProperty(String, Object)}, it will behave like an
 * {@link Intersection} of the restrictions contained in this MergedRestriction.
 * 
 * @author Carsten Stockloew
 */
public class MergedRestriction extends Intersection {

    /** A safe iterator that does not allow to remove elements. */
    private class SafeIterator implements Iterator {
	Iterator it;

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

    public static final int allValuesFromID = 0;
    public static final int someValuesFromID = 1;
    public static final int hasValueID = 2;
    public static final int boundingValueID = 3;
    public static final int minCardinalityID = 4;
    public static final int maxCardinalityID = 5;
    public static final int exactCardinalityID = 6;

    // All restrictions are stored in an array ('types' from the superclass).
    // 'index' points to the correct Restriction in this array, e.g.
    // index[allValuesFromID] is the index of 'types' where the
    // AllValuesFromRestriction is stored.
    private int index[] = new int[7];

    private String onProperty = null;

    /** Constructor for deserializers. */
    public MergedRestriction() {
	for (int i = 0; i < 7; i++)
	    index[i] = -1;
    }

    /**
     * Constructor for an empty set of restrictions. Restrictions can be add by
     * calling {@link #addRestriction(AbstractRestriction)} or
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
     *            instances of {@link AbstractRestriction}.
     */
    public MergedRestriction(String onProperty, ArrayList restrictions) {
	this();
	setRestrictions(onProperty, restrictions);
    }

    
    public static final MergedRestriction getPropertyBanningRestriction(String propURI) {
        MergedRestriction m = new MergedRestriction(propURI);
        m.addRestriction(new MaxCardinalityRestriction(propURI, 0));
        return m;
    }

    public static final MergedRestriction getFixedValueRestriction(String propURI,
            Object o) {
        if (propURI == null || o == null)
            return null;
    
        if (o instanceof String && isQualifiedName((String) o))
            o = new Resource((String) o);
    
        MergedRestriction m = new MergedRestriction(propURI);
        m.addRestriction(new HasValueRestriction(propURI, o));
        return m;
    }

    public static final MergedRestriction getCardinalityRestriction(String propURI,
            int min, int max) {
        if (propURI == null)
            return null;
        if ((max > -1 && max < min) // nothing
        	|| (max < 0 && min < 1)) // everything
            return null;
    
        MergedRestriction ret = new MergedRestriction(propURI);
        
        if (min > 0  &&  min == max) {
            ret.addRestriction(new ExactCardinalityRestriction(propURI, min));
        } else {
            if (min > 0)
        	ret.addRestriction(new MinCardinalityRestriction(propURI, min));
            if (max >= 0)
        	ret.addRestriction(new MaxCardinalityRestriction(propURI, max));
        }
        
        return ret;
    }

    public static final MergedRestriction getAllValuesRestrictionWithCardinality(
            String propURI, String typeURI, int min, int max) {
    
        MergedRestriction ret = new MergedRestriction(propURI);
        ret.addRestriction(getAllValuesRestrictionWithCardinality(propURI,
        	TypeURI.asTypeURI(typeURI), min, max));
        return ret;
    }

    public static final MergedRestriction getAllValuesRestriction(
            String propURI, String typeURI) {
        if (typeURI == null || propURI == null)
            return null;
    
        MergedRestriction ret = new MergedRestriction(propURI);
        ret.addRestriction(new AllValuesFromRestriction(propURI, TypeURI.asTypeURI(typeURI)));
        return ret;
    }

    public static final MergedRestriction getAllValuesRestrictionWithCardinality(
            String propURI, ClassExpression expr, int min, int max) {
        if (expr == null)
            return null;
    
        MergedRestriction ret = MergedRestriction.getCardinalityRestriction(propURI, min, max);
        if (ret != null)
            ret.addRestriction(new AllValuesFromRestriction(propURI, expr));
        return ret;
    }

    
    
    
    
    public static ArrayList getFromList(List o) {
	// multiple restrictions for (possibly multiple) properties
	// -> build array of MergedRestrictions
	// temporary map:
	// ___key: property URI (String)
	// ___val: ArrayList of AbstractRestrictions
	// -> each ArrayList will be a MergedRestriction
	HashMap map = new HashMap();
	Object tmp;

	// build temporary map
	for (int i = 0; i < o.size(); i++) {
	    tmp = o.get(i);
	    if (tmp instanceof AbstractRestriction) {
		AbstractRestriction res = (AbstractRestriction) tmp;
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

    public void reset() {
	types.clear();
	onProperty = null;
	for (int i = 0; i < index.length; i++)
	    index[i] = -1;
    }

    public int getMinCardinality() {
	if (index[minCardinalityID] != -1)
	    return ((MinCardinalityRestriction) (types
		    .get(index[minCardinalityID]))).getValue();
	if (index[exactCardinalityID] != -1)
	    return ((ExactCardinalityRestriction) (types
		    .get(index[exactCardinalityID]))).getValue();
	return 0;
    }

    public int getMaxCardinality() {
	if (index[maxCardinalityID] != -1)
	    return ((MaxCardinalityRestriction) (types
		    .get(index[maxCardinalityID]))).getValue();
	if (index[exactCardinalityID] != -1)
	    return ((ExactCardinalityRestriction) (types
		    .get(index[exactCardinalityID]))).getValue();
	return -1;
    }

    public Object getConstraint(int id) {
	if (index[id] != -1)
	    return ((AbstractRestriction) types.get(index[id])).getConstraint();
	return null;
    }

    public void setRestrictions(String onProperty, ArrayList restrictions) {
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

    public List getRestrictions() {
	return Collections.unmodifiableList(types);
    }

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

    private AbstractRestriction getRestriction(int id) {
	if (index[id] == -1)
	    return null;
	return (AbstractRestriction) types.get(index[id]);
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
     */
    public MergedRestriction addRestriction(AbstractRestriction res) {
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
	if (max == 0 || getRestriction(hasValueID) != null)
	    return this;

	// id points to the appropriate element in 'index' of the given
	// Restriction
	int id = getIndex(res);

	ClassExpression all = index[allValuesFromID] == -1 ? null
		: (ClassExpression) ((Resource) (types
			.get(index[allValuesFromID])))
			.getProperty(AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM);
	ClassExpression some = index[someValuesFromID] == -1 ? null
		: (ClassExpression) ((Resource) (types
			.get(index[someValuesFromID])))
			.getProperty(SomeValuesFromRestriction.PROP_OWL_SOME_VALUES_FROM);

	switch (id) {
	case allValuesFromID:
	    if (all == null
		    && (some == null || (max != 1 && res.matches(some, null)))) {
		index[allValuesFromID] = types.size();
		types.add(res);
	    }
	    break;
	case someValuesFromID:
	    if (some == null
		    && (all == null || ((ClassExpression) all).matches(res,
			    null))) {
		if (all != null && max == 1)
		    removeRestriction(allValuesFromID);
		index[someValuesFromID] = types.size();
		types.add(res);
	    }
	    break;
	case hasValueID:
	    if (max == -1 && all == null && some == null
		    && getRestriction(minCardinalityID) == null) {
		index[hasValueID] = types.size();
		types.add(res);
	    }
	    break;
	case boundingValueID:
	    if (index[boundingValueID] == -1) {
		index[boundingValueID] = types.size();
		types.add(res);
	    }
	    break;
	case minCardinalityID:
	    int newMin = ((MinCardinalityRestriction) res).getValue();
	    if (getMinCardinality() == 0)
		if (max < 0 || max > newMin) {
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
	    if (max == -1
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
	    if (max == -1
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

    public MergedRestriction addRestriction(MergedRestriction r) {
	ArrayList resList = (ArrayList) r.types;
	for (int i = 0; i < resList.size(); i++)
	    addRestriction((AbstractRestriction) resList.get(i));
	return this;
    }

    private int getIndex(AbstractRestriction res) {
	int idx = -1;
	if (res instanceof AllValuesFromRestriction)
	    idx = allValuesFromID;
	else if (res instanceof SomeValuesFromRestriction)
	    idx = someValuesFromID;
	else if (res instanceof HasValueRestriction)
	    idx = hasValueID;
	else if (res instanceof BoundingValueRestriction)
	    idx = boundingValueID;
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

    private void analyze() {
	for (int i = 0; i < types.size(); i++) {
	    if (!(types.get(i) instanceof AbstractRestriction))
		throw new IllegalArgumentException(
			"Non-restriction found at index: " + i);

	    AbstractRestriction res = (AbstractRestriction) types.get(i);
	    if (!onProperty.equals(res.getOnProperty()))
		throw new IllegalArgumentException(
			"Restriction defined for wrong property: "
				+ res.getClassURI() + " " + res.getOnProperty());

	    int idx = getIndex(res);
	    if (idx != -1 && index[idx] != -1)
		throw new IllegalArgumentException("Duplicate Restriction: "
			+ res.getClassURI());
	    index[idx] = i;
	}
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#copy() */
    public ClassExpression copy() {
	ArrayList newList = new ArrayList();
	for (int i = 0; i < types.size(); i++)
	    newList.add(((AbstractRestriction) types.get(i)).copy());
	return new MergedRestriction(onProperty, newList);
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
    public boolean isWellFormed() {
	for (int i = 0; i < types.size(); i++)
	    if (!((ClassExpression) types.get(i)).isWellFormed())
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

    public String getPropTypeURI() {
	ClassExpression all = null;

	if (index[allValuesFromID] == -1)
	    return null;

	all = (ClassExpression) ((AllValuesFromRestriction) types
		.get(index[allValuesFromID]))
		.getProperty(AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM);

	return (all instanceof TypeURI) ? ((TypeURI) all).getURI() : null;
    }

    public String getOnProperty() {
	return onProperty;
    }

    /**
     * Appends this restriction to the given root restriction on the given
     * property path.
     * 
     * 
     * @param root
     *            The root restriction for the first element of the property
     *            path. Can be null.
     * @param path
     *            The property path.
     * @return
     */
    public MergedRestriction appendTo(MergedRestriction root, String[] path) {
	if (path == null || path.length == 0)
	    return null;
	if (getOnProperty() == null)
	    return null;
	if (!getOnProperty().equals(path[path.length - 1]))
	    return null;
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
	    root = new MergedRestriction(this.getOnProperty());
	    AbstractRestriction r = new AllValuesFromRestriction();
	    r.setProperty(AbstractRestriction.PROP_OWL_ON_PROPERTY, path[0]);
	    root.addRestriction(r);
	} else {
	    // just a test: are all restrictions in root defined for the correct
	    // property?
	    if (!root.getOnProperty().equals(path[0]))
		return null;
	}

	// get the AllValuesFromRestriction in root
	AbstractRestriction tmp = root.getRestriction(allValuesFromID);
	if (tmp == null) {
	    // we couldn't find the AllValuesFromRestriction in the root array
	    // -> create an empty one here
	    tmp = new AllValuesFromRestriction();
	    tmp.setOnProperty(path[0]);
	    root.addRestriction(tmp);
	}

	// tmp is now the root AllValuesFromRestriction
	// -> follow the AllValuesFromRestriction along the path, create
	// appropriate Restrictions and Intersections, if necessary
	for (int i = 1; i < path.length - 1; i++)
	    tmp = tmp.getRestrictionOnProperty(path[i]);

	// tmp now points to the AllValuesFromRestriction of the path element
	// before the last path element
	ClassExpression all = (ClassExpression) getConstraint(allValuesFromID);
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
	    isec.addType((ClassExpression) types.get(i));
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
	ClassExpression all = (ClassExpression) getConstraint(allValuesFromID);
	if (all instanceof Intersection) {
	    // create a new MergedRestriction and collect all simple
	    // Restrictions
	    MergedRestriction m = new MergedRestriction(pathElement);
	    AbstractRestriction tmpRes;
	    for (Iterator i = ((Intersection) all).types(); i.hasNext();) {
		ClassExpression tmp = (ClassExpression) i.next();
		if (tmp instanceof AbstractRestriction) {
		    tmpRes = (AbstractRestriction) tmp;
		    if (tmpRes.getOnProperty().equals(pathElement))
			m.addRestriction(tmpRes);
		}
	    }
	    return m;
	} else if (all instanceof TypeURI)
	    return ManagedIndividual.getClassRestrictionsOnProperty(all
		    .getURI(), pathElement);

	if (all instanceof AbstractRestriction) {
	    AbstractRestriction res = (AbstractRestriction) all;
	    if (res.getOnProperty().equals(pathElement)) {
		MergedRestriction m = new MergedRestriction(pathElement);
		m.addRestriction(res);
		return m;
	    }
	}

	return null;
    }
}
