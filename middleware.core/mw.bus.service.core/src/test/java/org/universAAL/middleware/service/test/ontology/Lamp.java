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

package org.universAAL.middleware.service.test.ontology;

/**
 * Ontological representation of a light source. Methods included in this class
 * are the mandatory ones for representing an ontological concept in Java
 * classes for uAAL. Usually it includes getters and setters for most of its
 * properties.
 * 
 * @author mtazari
 * 
 */
public class Lamp extends Device {
    public static final String MY_URI = TestOntology.NAMESPACE + "Lamp";
    public static final String PROP_SOURCE_BRIGHTNESS = TestOntology.NAMESPACE
	    + "hasBrightness";

    public Lamp() {
	super();
    }

    public Lamp(String uri) {
	super(uri);
    }

    /** @see org.universAAL.middleware.owl.ManagedIndividual#getClassURI() */
    public String getClassURI() {
	return MY_URI;
    }

    public int getBrightness() {
	Integer i = (Integer) props.get(PROP_SOURCE_BRIGHTNESS);
	return (i == null) ? -1 : i.intValue();
    }

    public void setBrightness(int percentage) {
	if (percentage > -1 && percentage < 101)
	    props.put(PROP_SOURCE_BRIGHTNESS, new Integer(percentage));
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#getPropSerializationType
     *      (java.lang.String)
     */
    public int getPropSerializationType(String propURI) {
	return (PROP_PHYSICAL_LOCATION.equals(propURI)) ? PROP_SERIALIZATION_REDUCED
		: PROP_SERIALIZATION_FULL;
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#isWellFormed()
     */
    public boolean isWellFormed() {
	return props.containsKey(PROP_SOURCE_BRIGHTNESS)
		&& props.containsKey(PROP_PHYSICAL_LOCATION);
    }
}
