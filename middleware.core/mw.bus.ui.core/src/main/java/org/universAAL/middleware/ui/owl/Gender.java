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

import org.universAAL.middleware.owl.ManagedIndividual;

/**
 * Defines the Gender
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ManagedIndividual
 */
public class Gender extends ManagedIndividual {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE + "Gender";

    public static final int FEMALE = 0;
    public static final int MALE = 1;

    private static final String[] names = { "female", "male" };

    public static final Gender female = new Gender(FEMALE);
    public static final Gender male = new Gender(MALE);

    private int order;

    /**
     * @param order
     *            order
     * @return level based on the given order
     */
    public static Gender getLevelByOrder(int order) {
	switch (order) {
	case FEMALE:
	    return female;
	case MALE:
	    return male;
	default:
	    return null;
	}
    }

    /**
     * @param name
     */
    public static final Gender valueOf(String name) {
	for (int i = FEMALE; i <= MALE; i++)
	    if (names[i].equals(name))
		return getLevelByOrder(i);
	return null;
    }

    /** Usage of default constructor is prevented */
    private Gender() {
    }

    /**
     * Constructor receiving order
     * 
     * @param order
     *            order
     */
    private Gender(int order) {
	super(uAAL_VOCABULARY_NAMESPACE + names[order]);
	this.order = order;
    }

    /** @see org.universAAL.middleware.owl.ManagedIndividual#getClassURI() */
    public String getClassURI() {
	return MY_URI;
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
     * @return gender based on order defined at the time of construction
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
