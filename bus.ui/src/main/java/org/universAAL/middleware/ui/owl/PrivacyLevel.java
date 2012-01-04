/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.ui.owl;

import org.universAAL.middleware.owl.ComparableIndividual;

/**
 * Defines privacy levels that can be: insensible, known_people_only,
 * intimates_only, home_mates_only, personal
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ComparableIndividual
 */
public class PrivacyLevel extends ComparableIndividual {
    
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE + "PrivacyLevel";

    public static final int INSENSIBLE = 0;
    public static final int KNOWN_PEOPLE_ONLY = 1;
    public static final int INTIMATES_ONLY = 2;
    public static final int HOME_MATES_ONLY = 3;
    public static final int uAAL = 4;

    private static final String[] names = { "insensible", "known_people_only",
	    "intimates_only", "home_mates_only", "personal" };

    public static final PrivacyLevel personal = new PrivacyLevel(uAAL);
    public static final PrivacyLevel homeMatesOnly = new PrivacyLevel(
	    HOME_MATES_ONLY);
    public static final PrivacyLevel intimatesOnly = new PrivacyLevel(
	    INTIMATES_ONLY);
    public static final PrivacyLevel knownPeopleOnly = new PrivacyLevel(
	    KNOWN_PEOPLE_ONLY);
    public static final PrivacyLevel insensible = new PrivacyLevel(INSENSIBLE);

    private int order;


    /**
     * @return max value of privacy level
     */
    public static PrivacyLevel getMaxValue() {
	return personal;
    }

    /**
     * @return min value of privacy level
     */
    public static PrivacyLevel getMinValue() {
	return insensible;
    }

    /**
     * 
     * @param order
     *            order
     * @return privacy level based on received order
     */
    public static PrivacyLevel getLevelByOrder(int order) {
	switch (order) {
	case INSENSIBLE:
	    return insensible;
	case KNOWN_PEOPLE_ONLY:
	    return knownPeopleOnly;
	case INTIMATES_ONLY:
	    return intimatesOnly;
	case HOME_MATES_ONLY:
	    return homeMatesOnly;
	case uAAL:
	    return personal;
	default:
	    return null;
	}
    }

    /**
     * @param name
     *            privacy level name
     * @return privacy level
     */
    public static final PrivacyLevel valueOf(String name) {
	for (int i = INSENSIBLE; i <= uAAL; i++)
	    if (names[i].equals(name))
		return getLevelByOrder(i);
	return null;
    }

    /** Usage of default constructor is prevented */
    private PrivacyLevel() {
    }

    /**
     * Constructor receives privacy level order
     * 
     * @param order
     *            order
     */
    private PrivacyLevel(int order) {
	super(uAAL_VOCABULARY_NAMESPACE + names[order]);
	this.order = order;
    }

    /**
     * @see org.universAAL.middleware.owl.ComparableIndividual
     */
    public int compareTo(Object other) {
	return (this == other) ? 0
		: (order < ((PrivacyLevel) other).order) ? -1 : 1;
    }

    /**
     * Returns with next privacy level
     */
    public ComparableIndividual getNext() {
	return getLevelByOrder(order + 1);
    }

    /**
     * Returns with previuos privacy level
     */
    public ComparableIndividual getPrevious() {
	return getLevelByOrder(order - 1);
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#getPropSerializationType(String)
     */
    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#isWellFormed()
     */
    public boolean isWellFormed() {
	return true;
    }

    /**
     * @return privacy level name based on order defined at the time of
     *         construction
     */
    public String name() {
	return names[order];
    }

    /**
     * @return order defined at the time of construction
     */
    public int ord() {
	return order;
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#setProperty(String,
     *      Object)
     */
    public void setProperty(String propURI, Object o) {
	// do nothing
    }
}
