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
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.PropertyPath;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.lighting.ElectricLight;
import org.universAAL.ontology.lighting.LightSource;
import org.universAAL.ontology.lighting.Lighting;
import org.universAAL.ontology.location.Location;

/**
 * @author mtazari
 *
 */
public class ProvidedLightingService extends Lighting {
	
	// All the static Strings are used to unique identify special functions and objects
	public static final String LIGHTING_SERVER_NAMESPACE = "http://ontology.igd.fhg.de/LightingServer.owl#";
	public static final String MY_URI = LIGHTING_SERVER_NAMESPACE + "LightingService";
	
	static final String SERVICE_GET_CONTROLLED_LAMPS = LIGHTING_SERVER_NAMESPACE + "getControlledLamps";
	static final String SERVICE_GET_LAMP_INFO = LIGHTING_SERVER_NAMESPACE + "getLampInfo";
	static final String SERVICE_TURN_OFF = LIGHTING_SERVER_NAMESPACE + "turnOff";
	static final String SERVICE_TURN_ON = LIGHTING_SERVER_NAMESPACE + "turnOn";
	
	static final String INPUT_LAMP_URI = LIGHTING_SERVER_NAMESPACE + "lampURI";
	
	static final String OUTPUT_CONTROLLED_LAMPS = LIGHTING_SERVER_NAMESPACE + "controlledLamps";
	static final String OUTPUT_LAMP_BRIGHTNESS = LIGHTING_SERVER_NAMESPACE + "brightness";
	static final String OUTPUT_LAMP_LOCATION = LIGHTING_SERVER_NAMESPACE + "location";
	
	static final ServiceProfile[] profiles = new ServiceProfile[4];
	private static Hashtable serverLightingRestrictions = new Hashtable();
	static {
		// we need to register all classes in the ontology for the serialization of the object
		register(ProvidedLightingService.class);
		
		// At next we define some restrictions an the properties of the service
		// All restrictions are saved in the local ontology
		
		// At first we add the restrictions given by the base class
		addRestriction((Restriction)
				Lighting.getClassRestrictionsOnProperty(Lighting.PROP_CONTROLS).copy(),
				new String[] {Lighting.PROP_CONTROLS},
				serverLightingRestrictions);
		
		// At next we set a restriction that allows only lights of type ElectricLight.lightBulb
		addRestriction(
				Restriction.getFixedValueRestriction(
						LightSource.PROP_HAS_TYPE, ElectricLight.lightBulb),
				new String[] {Lighting.PROP_CONTROLS, LightSource.PROP_HAS_TYPE},
				serverLightingRestrictions);
		
		// At last we restrict the values for the brightness of the lights to values between 0 and 100
		addRestriction(
				Restriction.getAllValuesRestrictionWithCardinality(
						LightSource.PROP_SOURCE_BRIGHTNESS,
						new Enumeration(new Integer[] {new Integer(0), new Integer(100)}),
						1, 1),
				new String[] {Lighting.PROP_CONTROLS, LightSource.PROP_SOURCE_BRIGHTNESS},
				serverLightingRestrictions);
		
		// Help structures to define the property-path
		String[] ppControls = new String[] {Lighting.PROP_CONTROLS};
		String[] ppBrightness = new String[] {
				Lighting.PROP_CONTROLS, 
				LightSource.PROP_SOURCE_BRIGHTNESS
				};
		PropertyPath brightnessPath = new PropertyPath(null, true, ppBrightness);
		
		// Creates the service-object that offers the available lights
		ProvidedLightingService getControlledLamps = new ProvidedLightingService(SERVICE_GET_CONTROLLED_LAMPS);
		// Add to the service-profile that it offers an output under the URI in OUTPUT_CONTROLLED_LAMPS
		getControlledLamps.addOutput(OUTPUT_CONTROLLED_LAMPS, LightSource.MY_URI, 0, 0, ppControls);
		profiles[0] = getControlledLamps.myProfile;
		
		// Create the service-object that allows to collect information about the lights
		ProvidedLightingService getLampInfo = new ProvidedLightingService(SERVICE_GET_LAMP_INFO);
		// We need in input URI to define the light-source
		getLampInfo.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1, ppControls);
		// Define the output for the brightness
		getLampInfo.addOutput(OUTPUT_LAMP_BRIGHTNESS,
				TypeMapper.getDatatypeURI(Integer.class), 1, 1,
				ppBrightness);
		// Define the output for the location
		getLampInfo.addOutput(OUTPUT_LAMP_LOCATION,
				Location.MY_URI, 1, 1,
				new String[] {Lighting.PROP_CONTROLS, LightSource.PROP_SOURCE_LOCATION});
		profiles[1] = getLampInfo.myProfile;
		
		// Create the service-object that allows to turn off the lights
		ProvidedLightingService turnOff = new ProvidedLightingService(SERVICE_TURN_OFF);
		// We need in input URI to define the light-source
		turnOff.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1, ppControls);
		// Here we define that the service will take effect on a special property
		turnOff.myProfile.addChangeEffect(brightnessPath, new Integer(0));
		profiles[2] = turnOff.myProfile;
		
		// Create the service-object that allows to turn on the lights
		ProvidedLightingService turnOn = new ProvidedLightingService(SERVICE_TURN_ON);
		// We need in input URI to define the light-source
		turnOn.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1, ppControls);
		// Here we define that the service will take effect on a special property
		turnOn.myProfile.addChangeEffect(brightnessPath, new Integer(100));
		profiles[3] = turnOn.myProfile;
	}
	
	private ProvidedLightingService(String uri) {
		super(uri);
	}
}
