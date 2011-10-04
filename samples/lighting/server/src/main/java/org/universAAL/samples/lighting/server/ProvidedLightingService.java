/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.universAAL.samples.lighting.server;

import java.util.Hashtable;

import org.universAAL.middleware.owl.Enumeration;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.impl.ResourceFactoryImpl;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.lighting.ElectricLight;
import org.universAAL.ontology.lighting.LightSource;
import org.universAAL.ontology.lighting.Lighting;
import org.universAAL.ontology.location.Location;
import org.universAAL.ontology.phThing.PhysicalThing;

/**
 * @author mtazari
 * 
 */
public class ProvidedLightingService extends Lighting {

    // All the static Strings are used to unique identify special functions and
    // objects
    public static final String LIGHTING_SERVER_NAMESPACE = "http://ontology.igd.fhg.de/LightingServer.owl#";
    public static final String MY_URI = LIGHTING_SERVER_NAMESPACE
	    + "LightingService";

    static final String SERVICE_GET_CONTROLLED_LAMPS = LIGHTING_SERVER_NAMESPACE
	    + "getControlledLamps";
    static final String SERVICE_GET_LAMP_INFO = LIGHTING_SERVER_NAMESPACE
	    + "getLampInfo";
    static final String SERVICE_TURN_OFF = LIGHTING_SERVER_NAMESPACE
	    + "turnOff";
    static final String SERVICE_TURN_ON = LIGHTING_SERVER_NAMESPACE + "turnOn";

    static final String INPUT_LAMP_URI = LIGHTING_SERVER_NAMESPACE + "lampURI";

    static final String OUTPUT_CONTROLLED_LAMPS = LIGHTING_SERVER_NAMESPACE
	    + "controlledLamps";
    static final String OUTPUT_LAMP_BRIGHTNESS = LIGHTING_SERVER_NAMESPACE
	    + "brightness";
    static final String OUTPUT_LAMP_LOCATION = LIGHTING_SERVER_NAMESPACE
	    + "location";

    static final ServiceProfile[] profiles = new ServiceProfile[4];
    private static Hashtable serverLightingRestrictions = new Hashtable();
    static {
	// we need to register all classes in the ontology for the serialization
	// of the object
	// OntologyManagement.getInstance().register(new SimpleOntology(MY_URI,
	// Lighting.MY_URI));
	OntologyManagement.getInstance().register(
		new SimpleOntology(MY_URI, Lighting.MY_URI,
			new ResourceFactoryImpl() {
			    @Override
			    public Resource createInstance(String classURI,
				    String instanceURI, int factoryIndex) {
				return new ProvidedLightingService(instanceURI);
			    }
			}));

	// Help structures to define property paths used more than once below
	String[] ppControls = new String[] { Lighting.PROP_CONTROLS };
	String[] ppBrightness = new String[] { Lighting.PROP_CONTROLS,
		LightSource.PROP_SOURCE_BRIGHTNESS };

	// The purpose of the rest of this static segment is to describe
	// services that we want to make available. We start with some
	// "class-level restrictions" that are inherent to the underlying
	// service component realized in the subpackage 'unit_impl'. That is, we
	// know from unit_impl.MyLighting.java that
	// 1. it controls lamps
	// 2. that can only be switched on and off

	// Before adding our own restrictions, we first "inherit" the
	// restrictions defined by the superclass
	addRestriction((MergedRestriction) Lighting
		.getClassRestrictionsOnProperty(Lighting.MY_URI,
			Lighting.PROP_CONTROLS).copy(), ppControls,
		serverLightingRestrictions);

	// then, we add a restriction stating that the type of controlled light
	// sources is ElectricLight.lightBulb meaning that light sources
	// controlled by this class of services are all light bulbs
	addRestriction(MergedRestriction.getFixedValueRestriction(
		LightSource.PROP_HAS_TYPE, ElectricLight.lightBulb),
		new String[] { Lighting.PROP_CONTROLS,
			LightSource.PROP_HAS_TYPE }, serverLightingRestrictions);

	// finally, we restrict the values for the brightness of the lights to
	// only 0 and 100 meaning that the controlled light bulbs do not support
	// dimming
	addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			LightSource.PROP_SOURCE_BRIGHTNESS, new Enumeration(
				new Integer[] { new Integer(0),
					new Integer(100) }), 1, 1),
		ppBrightness, serverLightingRestrictions);

	/*
	 * create the service description #1 to be registered with the service
	 * bus
	 */

	// Create the service-object for retrieving the controlled light bulbs
	ProvidedLightingService getControlledLamps = new ProvidedLightingService(
		SERVICE_GET_CONTROLLED_LAMPS);
	// Add an output with the given URI (parameter #1) and the following
	// additional info to the service-profile:
	// - it delivers an indefinite number (parameters #3 & #4) of
	//   LightSource (parameter #2) objects
	// - that are those controlled by this class of services (parameter #5)
	// Note that because no filtering has been defined, the output will
	// contain all of the controlled light bulbs
	getControlledLamps.addOutput(OUTPUT_CONTROLLED_LAMPS,
		LightSource.MY_URI, 0, 0, ppControls);
	// we are finished and can add this profile to the list of service
	// profiles to be registered with the service bus
	profiles[0] = getControlledLamps.myProfile;

	/*
	 * create the service description #2 to be registered with the service
	 * bus
	 */

	// Create the service-object for retrieving info about the location and
	// state of each controlled light bulb
	ProvidedLightingService getLampInfo = new ProvidedLightingService(
		SERVICE_GET_LAMP_INFO);
	// Add an input with the given URI (parameter #1) and the following
	// additional info to the service-profile:
	// - it will be used to restrict the scope of the process results (cf.
	//   "Filtering" in the method name)
	// - it must be exactly one (parameters #3 & #4) LightSource (parameter
	//   #2) object
	// - that is used to select the controlled light bulb (parameter #5) to
	//   be considered in the scope of the process results
	// Note that 'addFilteringInput' works based on equality, i.e. from all
	// objects addressed by 'ppControls' only those are selected that have
	// the same identity as the value passed for this input parameter
	getLampInfo.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1,
		ppControls);
	// one of the results of using this service is the delivery of info
	// about the brightness (parameter #5) of the light bulb in the scope
	// (cf. the input parameter); this info will be a single (parameters #3
	// & #4) number of type integer (parameter #2) that is assigned to an
	// output parameter identifiable by the given URI (parameter 1)
	getLampInfo.addOutput(OUTPUT_LAMP_BRIGHTNESS, TypeMapper
		.getDatatypeURI(Integer.class), 1, 1, ppBrightness);
	// another result of using this service is the delivery of info about
	// the location (parameter #5) of the light bulb in the scope (cf. the
	// input parameter); this info will be a single (parameters #3 & #4)
	// object of type Location (parameter #2) that is assigned to an output
	// parameter identifiable by the given URI (parameter 1)
	getLampInfo.addOutput(OUTPUT_LAMP_LOCATION, Location.MY_URI, 1, 1,
		new String[] { Lighting.PROP_CONTROLS,
			PhysicalThing.PROP_PHYSICAL_LOCATION });
	// we are finished and can add this profile to the list of service
	// profiles to be registered with the service bus
	profiles[1] = getLampInfo.myProfile;

	/*
	 * create the service description #3 to be registered with the service
	 * bus
	 */

	// Create the service-object for turning off each controlled light bulb
	ProvidedLightingService turnOff = new ProvidedLightingService(
		SERVICE_TURN_OFF);
	// We need an input parameter identical with the previous one
	turnOff.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1,
		ppControls);
	// but the result of using this service is the change of the brightness
	// (parameter #1) of the selected light bulb (cf. the input parameter)
	// to 0 (parameter #2)
	turnOff.myProfile.addChangeEffect(ppBrightness, new Integer(0));
	// we are finished and can add this profile to the list of service
	// profiles to be registered with the service bus
	profiles[2] = turnOff.myProfile;

	/*
	 * create the service description #4 to be registered with the service
	 * bus
	 */

	// Create the service-object for turning on each controlled light bulb
	ProvidedLightingService turnOn = new ProvidedLightingService(
		SERVICE_TURN_ON);
	// We need an input parameter identical with the previous one
	turnOn.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1,
		ppControls);
	// but the result of using this service is the change of the brightness
	// (parameter #1) of the selected light bulb (cf. the input parameter)
	// to 100 (parameter #2)
	turnOn.myProfile.addChangeEffect(ppBrightness, new Integer(100));
	profiles[3] = turnOn.myProfile;
    }

    private ProvidedLightingService(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }
}
