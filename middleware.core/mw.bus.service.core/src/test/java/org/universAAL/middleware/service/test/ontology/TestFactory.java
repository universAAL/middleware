/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;

/**
 * @author mtazari
 * @author Carsten Stockloew
 */
public class TestFactory implements ResourceFactory {

    public TestFactory() {
    }

    public Resource createInstance(String classURI, String instanceURI,
	    int factoryIndex) {

	switch (factoryIndex) {
	case 0:
	    return new Device(instanceURI);
	case 1:
	    return new Lamp(instanceURI);
	case 2:
	    return new DeviceService(instanceURI);
	case 3:
	    return new LampService(instanceURI);
	case 4:
	    return new PhysicalThing(instanceURI);
	case 5:
	    return new Location(instanceURI);
	case 6:
	    return new IndoorPlace(instanceURI);
	case 7:
	    return new OutdoorPlace(instanceURI);
	case 8:
	    return new Room(instanceURI);
	}

	return null;
    }
}
