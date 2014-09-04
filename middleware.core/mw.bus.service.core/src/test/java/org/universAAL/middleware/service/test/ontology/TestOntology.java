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

import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.IntRestriction;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owl.ServiceBusOntology;


/**
 * 
 * @author Carsten Stockloew
 * 
 */
public final class TestOntology extends Ontology {

    private static TestFactory factory = new TestFactory();;

    public static final String NAMESPACE = "http://ontology.universaal.org/Tutorial.owl#";

    public TestOntology() {
	super(NAMESPACE);
    }

    public void create() {
	Resource r = getInfo();
	r.setResourceComment("Tutorial ontology with device and lamp.");
	r.setResourceLabel("Tutorial ontology");
	addImport(DataRepOntology.NAMESPACE);
	addImport(ServiceBusOntology.NAMESPACE);

	OntClassInfoSetup oci;

	// load Device
	oci = createNewOntClassInfo(Device.MY_URI, factory, 0);
	oci.addSuperClass(PhysicalThing.MY_URI);

	// load Lamp
	oci = createNewOntClassInfo(Lamp.MY_URI, factory, 1);
	oci.addSuperClass(Device.MY_URI);
	oci.addDatatypeProperty(Lamp.PROP_SOURCE_BRIGHTNESS).setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			Lamp.PROP_SOURCE_BRIGHTNESS, new IntRestriction(
				new Integer(0), true, new Integer(100), true),
			1, 1));

	// load DeviceService
	oci = createNewOntClassInfo(DeviceService.MY_URI, factory, 2);
	oci.addSuperClass(Service.MY_URI);
	oci.addObjectProperty(DeviceService.PROP_CONTROLS);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		DeviceService.PROP_CONTROLS, Device.MY_URI));

	// load LampService
	oci = createNewOntClassInfo(LampService.MY_URI, factory, 3);
	oci.addSuperClass(DeviceService.MY_URI);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		DeviceService.PROP_CONTROLS, Lamp.MY_URI));

	// load Location
	oci = createNewOntClassInfo(Location.MY_URI, factory, 5);
	oci.addSuperClass(ManagedIndividual.MY_URI);
	
	// load IndoorPlace
	oci = createNewOntClassInfo(IndoorPlace.MY_URI, factory, 6);
	oci.addSuperClass(Location.MY_URI);
	
	// load OutdoorPlace
	oci = createNewOntClassInfo(OutdoorPlace.MY_URI, factory, 7);
	oci.addSuperClass(Location.MY_URI);
	
	// load Room
	oci = createNewOntClassInfo(Room.MY_URI, factory, 8);
	oci.addSuperClass(IndoorPlace.MY_URI);
	
	// load PhysicalThing
	oci = createNewOntClassInfo(PhysicalThing.MY_URI, factory, 4);
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci.addObjectProperty(PhysicalThing.PROP_PHYSICAL_LOCATION)
		.setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			PhysicalThing.PROP_PHYSICAL_LOCATION, Location.MY_URI,
			0, 1));
    }
}
