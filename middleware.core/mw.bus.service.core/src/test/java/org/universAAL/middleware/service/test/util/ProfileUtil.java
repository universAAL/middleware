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
package org.universAAL.middleware.service.test.util;

import java.util.ArrayList;

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.service.test.ontology.DeviceService;
import org.universAAL.middleware.service.test.ontology.IndoorPlace;
import org.universAAL.middleware.service.test.ontology.Lamp;
import org.universAAL.middleware.service.test.ontology.LampService;
import org.universAAL.middleware.service.test.ontology.Location;
import org.universAAL.middleware.service.test.ontology.PhysicalThing;

public class ProfileUtil {
    // All the static Strings are used to unique identify special functions and
    // objects
    public static String LAMP_SERVER_NAMESPACE = "http://ontology.igd.fhg.de/TestLampServer.owl#";

    public static String SERVICE_GET_LAMPS;
    public static String SERVICE_GET_LAMP_INFO;
    public static String SERVICE_GET_LAMPS_INDOORS;
    public static String SERVICE_TURN_OFF;
    public static String SERVICE_TURN_ON;
    public static String SERVICE_DIM;

    public static String INPUT_LAMP_URI;
    public static String INPUT_LAMP_BRIGHTNESS;
    public static String INPUT_LAMP_LOCATION;

    public static String OUTPUT_CONTROLLED_LAMPS;
    public static String OUTPUT_LAMP_BRIGHTNESS;
    public static String OUTPUT_LAMP_LOCATION;

    public static String[] OUTPUT_CONTROLLED_LAMPS_ARR;

    public static String[] ppControls;
    public static String[] ppBrightness;
    public static String[] ppLocation;
    public static String[] ppLocationType;

    public static ArrayList<ServiceProfile> profiles = new ArrayList<ServiceProfile>();

    private static String[] createArray(String s) {
	return new String[] { s + "0", s + "1", s + "2" };
    }

    static {
	SERVICE_GET_LAMPS = LAMP_SERVER_NAMESPACE + "srv_getControlledLamps";
	SERVICE_GET_LAMP_INFO = LAMP_SERVER_NAMESPACE + "srv_getLampInfo";
	SERVICE_GET_LAMPS_INDOORS = LAMP_SERVER_NAMESPACE
		+ "srv_getLampsIndoor";
	SERVICE_TURN_OFF = LAMP_SERVER_NAMESPACE + "srv_turnOff";
	SERVICE_TURN_ON = LAMP_SERVER_NAMESPACE + "srv_turnOn";
	SERVICE_DIM = LAMP_SERVER_NAMESPACE + "srv_dim";

	INPUT_LAMP_URI = LAMP_SERVER_NAMESPACE + "in_lampURI";
	INPUT_LAMP_BRIGHTNESS = LAMP_SERVER_NAMESPACE + "in_brightness";
	INPUT_LAMP_LOCATION = LAMP_SERVER_NAMESPACE + "in_location";

	OUTPUT_CONTROLLED_LAMPS = LAMP_SERVER_NAMESPACE + "out_controlledLamps";
	OUTPUT_LAMP_BRIGHTNESS = LAMP_SERVER_NAMESPACE + "out_brightness";
	OUTPUT_LAMP_LOCATION = LAMP_SERVER_NAMESPACE + "out_location";

	OUTPUT_CONTROLLED_LAMPS_ARR = createArray(OUTPUT_CONTROLLED_LAMPS);

	// Help structures to define property paths used more than once below
	ppControls = new String[] { DeviceService.PROP_CONTROLS };
	ppBrightness = new String[] { DeviceService.PROP_CONTROLS,
		Lamp.PROP_SOURCE_BRIGHTNESS };
	ppLocation = new String[] { DeviceService.PROP_CONTROLS,
		PhysicalThing.PROP_PHYSICAL_LOCATION };
	ppLocationType = new String[] { DeviceService.PROP_CONTROLS,
		PhysicalThing.PROP_PHYSICAL_LOCATION };

	// create_getControlledLamps(true);
	// create_getLampInfo(true);
	// create_turnOff(true);
	// create_turnOn(true);
	// create_dim(true);
	// create_getControlledLamps(false);
	// create_getLampInfo(false);
	// create_turnOff(false);
	// create_turnOn(false);
	// create_dim(false);
	// create_getIndoors1();
	// create_getIndoors2();
	// create_getIndoors3();
    }

    private static Service createService(String uri, boolean dev) {
	if (dev)
	    return new DeviceService(uri + "__Device");
	else
	    return new LampService(uri + "__Lamp");
    }

    public static ServiceProfile create_getControlledLamps(boolean dev) {
	Service getControlledLamps = createService(SERVICE_GET_LAMPS, dev);
	getControlledLamps.addOutput(OUTPUT_CONTROLLED_LAMPS, Lamp.MY_URI, 0,
		-1, ppControls);
	return getControlledLamps.getProfile();
    }

    public static ServiceProfile create_getControlledLamps(boolean dev,
	    int outputIndex) {
	Service getControlledLamps = createService(SERVICE_GET_LAMPS, dev);
	getControlledLamps.addOutput(OUTPUT_CONTROLLED_LAMPS_ARR[outputIndex],
		Lamp.MY_URI, 0, -1, ppControls);
	return getControlledLamps.getProfile();
    }

    public static ServiceProfile create_getLampInfo(boolean dev) {
	Service getLampInfo = createService(SERVICE_GET_LAMP_INFO, dev);
	getLampInfo.addFilteringInput(INPUT_LAMP_URI, Lamp.MY_URI, 1, 1,
		ppControls);
	getLampInfo.addOutput(OUTPUT_LAMP_BRIGHTNESS,
		TypeMapper.getDatatypeURI(Integer.class), 1, 1, ppBrightness);
	getLampInfo.addOutput(OUTPUT_LAMP_LOCATION, Location.MY_URI, 1, 1,
		ppLocation);
	return getLampInfo.getProfile();
    }

    public static ServiceProfile create_turnOff(boolean dev) {
	Service turnOff = createService(SERVICE_TURN_OFF, dev);
	turnOff.addFilteringInput(INPUT_LAMP_URI, Lamp.MY_URI, 1, 1, ppControls);
	turnOff.getProfile().addChangeEffect(ppBrightness, new Integer(0));
	return turnOff.getProfile();
    }

    public static ServiceProfile create_turnOn(boolean dev) {
	Service turnOn = createService(SERVICE_TURN_ON, dev);
	turnOn.addFilteringInput(INPUT_LAMP_URI, Lamp.MY_URI, 1, 1, ppControls);
	turnOn.getProfile().addChangeEffect(ppBrightness, new Integer(100));
	return turnOn.getProfile();
    }

    public static ServiceProfile create_dim(boolean dev) {
	Service dim = createService(SERVICE_DIM, dev);
	dim.addFilteringInput(INPUT_LAMP_URI, Lamp.MY_URI, 1, 1, ppControls);
	dim.addInputWithChangeEffect(INPUT_LAMP_BRIGHTNESS,
		TypeMapper.getDatatypeURI(Integer.class), 1, 1, ppBrightness);
	return dim.getProfile();
    }

    public static ServiceProfile create_getIndoors1() {
	Service getIndoors = createService(SERVICE_GET_LAMPS_INDOORS + "_1_",
		false);
	getIndoors.addInstanceLevelRestriction(MergedRestriction
		.getFixedValueRestriction(PhysicalThing.PROP_PHYSICAL_LOCATION,
			IndoorPlace.MY_URI), ppLocation);
	getIndoors.addOutput(OUTPUT_CONTROLLED_LAMPS, Lamp.MY_URI, 0, 0,
		ppControls);
	return getIndoors.getProfile();
	// System.out.println(getIndoors.getProfile().toStringRecursive());
    }

    public static ServiceProfile create_getIndoors2() {
	Service getIndoors = createService(SERVICE_GET_LAMPS_INDOORS + "_2_",
		false);
	getIndoors.addFilteringType(INPUT_LAMP_LOCATION, ppLocation);
	getIndoors.addOutput(OUTPUT_CONTROLLED_LAMPS, Lamp.MY_URI, 0, 0,
		ppControls);
	return getIndoors.getProfile();
    }

    public static ServiceProfile create_getIndoors3() {
	Service getIndoors = createService(SERVICE_GET_LAMPS_INDOORS + "_3_",
		false);
	getIndoors.addInstanceLevelRestriction(MergedRestriction
		.getFixedValueRestriction(PhysicalThing.PROP_PHYSICAL_LOCATION,
			IndoorPlace.MY_URI), ppLocation);
	getIndoors.addFilteringType(INPUT_LAMP_LOCATION, ppLocation);
	getIndoors.addOutput(OUTPUT_CONTROLLED_LAMPS, Lamp.MY_URI, 0, 0,
		ppControls);
	return getIndoors.getProfile();
    }
}
