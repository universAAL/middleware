/*
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.owl.supply;

import org.universAAL.middleware.owl.ComparableIndividual;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public final class LevelRating extends ComparableIndividual {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "LevelRating";

    public static final int NONE = 0;
    public static final int LOW = 1;
    public static final int MIDDLE = 2;
    public static final int HIGH = 3;
    public static final int FULL = 4;

    private static final String[] names = { "none", "low", "middle", "high",
	    "full" };

    public static final LevelRating none = new LevelRating(NONE);
    public static final LevelRating low = new LevelRating(LOW);
    public static final LevelRating middle = new LevelRating(MIDDLE);
    public static final LevelRating high = new LevelRating(HIGH);
    public static final LevelRating full = new LevelRating(FULL);

    /** The current value of this object. */
    private int order;

    // prevent the usage of the default constructor
    private LevelRating() {

    }

    private LevelRating(int order) {
	super(uAAL_VOCABULARY_NAMESPACE + names[order]);
	this.order = order;
    }

    /** @see org.universAAL.middleware.owl.ManagedIndividual#getClassURI() */
    public String getClassURI() {
	return MY_URI;
    }

    public static LevelRating getMaxValue() {
	return full;
    }

    public static LevelRating getMinValue() {
	return none;
    }

    public static LevelRating getLevelByOrder(int order) {
	switch (order) {
	case NONE:
	    return none;
	case LOW:
	    return low;
	case MIDDLE:
	    return middle;
	case HIGH:
	    return high;
	case FULL:
	    return full;
	default:
	    return null;
	}
    }

    public static final LevelRating valueOf(String name) {
	for (int i = NONE; i <= FULL; i++)
	    if (names[i].equals(name))
		return getLevelByOrder(i);
	return null;
    }

    public int compareTo(Object other) {
	return (this == other) ? 0 : (order < ((LevelRating) other).order) ? -1
		: 1;
    }

    public ComparableIndividual getNext() {
	return getLevelByOrder(order + 1);
    }

    public ComparableIndividual getPrevious() {
	return getLevelByOrder(order - 1);
    }

    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /** @see org.universAAL.middleware.rdf.Resource#isWellFormed() */
    public boolean isWellFormed() {
	return true;
    }

    /** Get a human-readable description for this Rating value. */
    public String name() {
	return names[order];
    }

    public int ord() {
	return order;
    }

    /**
     * Overrides the default method to prevent properties from being added.
     * 
     * @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object)
     */
    public boolean setProperty(String propURI, Object o) {
	// do nothing
	return false;
    }
}
