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

import org.universAAL.middleware.service.owl.Service;

/**
 * Ontological service that handles light sources. Methods included in this
 * class are the mandatory ones for representing an ontological service in Java
 * classes for uAAL.
 * 
 * @author mtazari
 * 
 */
public class DeviceService extends Service {
    public static final String MY_URI = TestOntology.NAMESPACE
	    + "DeviceService";
    public static final String PROP_CONTROLS = TestOntology.NAMESPACE
	    + "controls";

    public DeviceService() {
	super();
    }

    public DeviceService(String uri) {
	super(uri);
    }

    /** @see org.universAAL.middleware.owl.ManagedIndividual#getClassURI() */
    public String getClassURI() {
	return MY_URI;
    }

    public int getPropSerializationType(String propURI) {
	return PROP_CONTROLS.equals(propURI) ? PROP_SERIALIZATION_FULL : super
		.getPropSerializationType(propURI);
    }

    public boolean isWellFormed() {
	return true;
    }
}
