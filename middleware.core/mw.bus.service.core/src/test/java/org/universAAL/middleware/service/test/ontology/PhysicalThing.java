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

import org.universAAL.middleware.owl.ManagedIndividual;

/**
 * Represents the class of physical things that are supposed to have a location.
 * 
 * @author mtazari
 * 
 */
public class PhysicalThing extends ManagedIndividual {
    public static final String MY_URI = TestOntology.NAMESPACE
	    + "PhysicalThing";
    public static final String PROP_PHYSICAL_LOCATION = TestOntology.NAMESPACE
	    + "hasLocation";

    protected PhysicalThing() {
	super();
    }

    public PhysicalThing(String uri) {
	super(uri);
    }

    protected PhysicalThing(String uriPrefix, int numProps) {
	super(uriPrefix, numProps);
    }

    public String getClassURI() {
	return MY_URI;
    }

    public Location getLocation() {
	return (Location) props.get(PROP_PHYSICAL_LOCATION);
    }

    /**
     * From the point of view of this top most class of things with a location,
     * the location can be represented in its reduced form. As the class has no
     * other property, for all other input, we return
     * {@link ManagedIndividual#PROP_SERIALIZATION_OPTIONAL}.
     * 
     * @see ManagedIndividual#getPropSerializationType(String).
     */
    public int getPropSerializationType(String propURI) {
	if (PROP_PHYSICAL_LOCATION.equals(propURI))
	    return PROP_SERIALIZATION_REDUCED;
	return PROP_SERIALIZATION_OPTIONAL;
    }

    public boolean setLocation(Location loc) {
	if (loc == null)
	    throw new IllegalArgumentException();
	props.put(PROP_PHYSICAL_LOCATION, loc);
	return true;
    }

    public boolean setProperty(String propURI, Object o) {
	if (PROP_PHYSICAL_LOCATION.equals(propURI) && o instanceof Location)
	    return setLocation((Location) o);
	else
	    return super.setProperty(propURI, o);
    }
}
