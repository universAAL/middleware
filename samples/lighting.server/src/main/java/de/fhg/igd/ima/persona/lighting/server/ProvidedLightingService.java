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
package de.fhg.igd.ima.persona.lighting.server;

import java.util.Hashtable;

import org.persona.middleware.TypeMapper;
import org.persona.middleware.service.PropertyPath;
import org.persona.middleware.service.profile.ServiceProfile;
import org.persona.ontology.Location;
import org.persona.ontology.expr.Enumeration;
import org.persona.ontology.expr.Restriction;
import org.persona.platform.casf.ontology.device.lighting.LightSource;
import org.persona.platform.casf.ontology.device.lighting.ElectricLight;
import org.persona.platform.casf.ontology.device.lighting.Lighting;

/**
 * @author mtazari
 *
 */
public class ProvidedLightingService extends Lighting {
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
		register(ProvidedLightingService.class);
		addRestriction((Restriction)
				Lighting.getClassRestrictionsOnProperty(Lighting.PROP_CONTROLS).copy(),
				new String[] {Lighting.PROP_CONTROLS},
				serverLightingRestrictions);
		addRestriction(
				Restriction.getFixedValueRestriction(
						LightSource.PROP_HAS_TYPE, ElectricLight.lightBulb),
				new String[] {Lighting.PROP_CONTROLS, LightSource.PROP_HAS_TYPE},
				serverLightingRestrictions);
		addRestriction(
				Restriction.getAllValuesRestrictionWithCardinality(
						LightSource.PROP_SOURCE_BRIGHTNESS,
						new Enumeration(new Integer[] {new Integer(0), new Integer(100)}),
						1, 1),
				new String[] {Lighting.PROP_CONTROLS, LightSource.PROP_SOURCE_BRIGHTNESS},
				serverLightingRestrictions);
		
		String[] ppControls = new String[] {Lighting.PROP_CONTROLS};
		String[] ppBrightness = new String[] {
				Lighting.PROP_CONTROLS, 
				LightSource.PROP_SOURCE_BRIGHTNESS
				};
		PropertyPath brightnessPath = new PropertyPath(null, true, ppBrightness);
		
		ProvidedLightingService getControlledLamps = new ProvidedLightingService(SERVICE_GET_CONTROLLED_LAMPS);
		getControlledLamps.addOutput(OUTPUT_CONTROLLED_LAMPS, LightSource.MY_URI, 0, 0, ppControls);
		profiles[0] = getControlledLamps.myProfile;
		
		ProvidedLightingService getLampInfo = new ProvidedLightingService(SERVICE_GET_LAMP_INFO);
		getLampInfo.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1, ppControls);
		getLampInfo.addOutput(OUTPUT_LAMP_BRIGHTNESS,
				TypeMapper.getDatatypeURI(Integer.class), 1, 1,
				ppBrightness);
		getLampInfo.addOutput(OUTPUT_LAMP_LOCATION,
				Location.MY_URI, 1, 1,
				new String[] {Lighting.PROP_CONTROLS, LightSource.PROP_SOURCE_LOCATION});
		profiles[1] = getLampInfo.myProfile;
		
		ProvidedLightingService turnOff = new ProvidedLightingService(SERVICE_TURN_OFF);
		turnOff.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1, ppControls);
		turnOff.myProfile.addChangeEffect(brightnessPath, new Integer(0));
		profiles[2] = turnOff.myProfile;
		
		ProvidedLightingService turnOn = new ProvidedLightingService(SERVICE_TURN_ON);
		turnOn.addFilteringInput(INPUT_LAMP_URI, LightSource.MY_URI, 1, 1, ppControls);
		turnOn.myProfile.addChangeEffect(brightnessPath, new Integer(100));
		profiles[3] = turnOn.myProfile;
	}
	
	private ProvidedLightingService(String uri) {
		super(uri);
	}
}
