/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	Copyright 2012-2014 Ericsson Nikola Tesla d.d., www.ericsson.com/hr/
	
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
package org.universAAL.middleware.ui.owl;

import org.universAAL.middleware.owl.ManagedIndividual;

/**
 * Defines modalities which can be: voice, gui, gesture, sms, web or mobile
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @author eandgrg
 * 
 */
public class Modality extends ManagedIndividual {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE + "Modality";

    public static final int VOICE = 0;
    public static final int GUI = 1;
    public static final int GESTURE = 2;
    public static final int SMS = 3;
    public static final int WEB = 4;
    public static final int MOBILE = 5;

    private static final String[] names = { "voice", "gui", "gesture", "sms",
	    "web", "mobile" };

    public static final Modality voice = new Modality(VOICE);
    public static final Modality gui = new Modality(GUI);
    public static final Modality gesture = new Modality(GESTURE);
    public static final Modality sms = new Modality(SMS);
    public static final Modality web = new Modality(WEB);
    public static final Modality mobile = new Modality(MOBILE);

    private int order;

    /**
     * @param order
     *            order
     * @return modality based on given order
     */
    public static Modality getLevelByOrder(int order) {
	switch (order) {
	case VOICE:
	    return voice;
	case GUI:
	    return gui;
	case GESTURE:
	    return gesture;
	case SMS:
	    return sms;
	case WEB:
	    return web;
	case MOBILE:
	    return mobile;
	default:
	    return null;
	}
    }

    /**
     * @param name
     *            name of modality
     * @return Modality based on name
     */
    public static final Modality valueOf(String name) {
	for (int i = VOICE; i <= MOBILE; i++)
	    if (names[i].equals(name))
		return getLevelByOrder(i);
	return null;
    }

    /** Usage of default Constructor is prevented */
    private Modality() {

    }

    /**
     * Constructor receives order
     * 
     * @param order
     */
    private Modality(int order) {
	super(uAAL_VOCABULARY_NAMESPACE + names[order]);
	this.order = order;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.universAAL.middleware.owl.ManagedIndividual#getClassURI()
     */
    public String getClassURI() {
	return MY_URI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.owl.ManagedIndividual#getPropSerializationType
     * (java.lang.String)
     */
    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.universAAL.middleware.owl.ManagedIndividual#isWellFormed()
     */
    public boolean isWellFormed() {
	return true;
    }

    /**
     * @return name based on order defined at the time of construction
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
     * @return number of defined modalities
     */
    public static int getSize() {
	return names.length;
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#setProperty(String,
     *      Object)
     */
    public boolean setProperty(String propURI, Object o) {
	// do nothing
	return false;
    }
}
