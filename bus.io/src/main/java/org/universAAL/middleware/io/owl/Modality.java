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
package org.universAAL.middleware.io.owl;

import org.universAAL.middleware.owl.ManagedIndividual;

/**
 * Defines modalities which can be: voice, gui, gesture or sms
 * 
 * @author mtazari
 * @see org.universAAL.middleware.owl.ManagedIndividual
 */
public class Modality extends ManagedIndividual {
    public static final String MY_URI;
    static {
	MY_URI = uAAL_VOCABULARY_NAMESPACE + "Modality";
	register(Modality.class);
    }

    public static final int VOICE = 0;
    public static final int GUI = 1;
    public static final int GESTURE = 2;
    public static final int SMS = 3;

    private static final String[] names = { "voice", "gui", "gesture", "sms" };

    public static final Modality voice = new Modality(VOICE);
    public static final Modality gui = new Modality(GUI);
    public static final Modality gesture = new Modality(GESTURE);
    public static final Modality sms = new Modality(SMS);

    /**
     * Returns the list of all class members guaranteeing that no other members
     * will be created after a call to this method.
     */
    public static ManagedIndividual[] getEnumerationMembers() {
	return new ManagedIndividual[] { voice, gui, gesture, sms };
    }

    /**
     * Returns the modality with the given URI.
     * 
     * @see org.universAAL.middleware.owl.ManagedIndividual#getIndividualByURI(String)
     */
    public static ManagedIndividual getIndividualByURI(String instanceURI) {
	return (instanceURI != null && instanceURI
		.startsWith(uAAL_VOCABULARY_NAMESPACE)) ? valueOf(instanceURI
		.substring(uAAL_VOCABULARY_NAMESPACE.length())) : null;
    }

    /**
     * 
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
	default:
	    return null;
	}
    }

    /**
     * Returns the value of the property <code>rdfs:comment</code> on this
     * <code>owl:Class</code> from the underlying ontology.
     * 
     * @see org.universAAL.middleware.owl.ManagedIndividual#getRDFSComment()
     */
    public static String getRDFSComment() {
	return "An enumeration for specifying the modality of information.";
    }

    /**
     * Returns the value of the property <code>rdfs:label</code> on this
     * <code>owl:Class</code> from the underlying ontology.
     * 
     * @see org.universAAL.middleware.owl.ManagedIndividual#getRDFSLabel()
     */
    public static String getRDFSLabel() {
	return "Modality";
    }

    /**
     * 
     * @param name
     *            name of modality
     * @return Modality based on name
     */
    public static final Modality valueOf(String name) {
	for (int i = VOICE; i <= SMS; i++)
	    if (names[i].equals(name))
		return getLevelByOrder(i);
	return null;
    }

    private int order;

    /**
     * Usage of default Constructor is prevented
     */
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
     * 
     * @return name based on order defined at the time of construction
     */
    public String name() {
	return names[order];
    }

    /**
     * 
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
