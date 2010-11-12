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

import java.util.ArrayList;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.persona.middleware.context.ContextEvent;
import org.persona.middleware.context.ContextPublisher;
import org.persona.middleware.context.DefaultContextPublisher;
import org.persona.middleware.service.CallStatus;
import org.persona.middleware.service.ServiceCall;
import org.persona.middleware.service.ServiceCallee;
import org.persona.middleware.service.ServiceResponse;
import org.persona.middleware.service.process.ProcessOutput;
import org.persona.ontology.context.ContextProvider;
import org.persona.ontology.context.ContextProviderType;
import org.persona.platform.casf.ontology.device.lighting.LightSource;
import org.persona.platform.casf.ontology.location.PLocation;
import org.persona.platform.casf.ontology.location.RoomPlace;

import de.fhg.igd.ima.persona.lighting.server.unit_impl.LampStateListener;
import de.fhg.igd.ima.persona.lighting.server.unit_impl.MyLighting;

/**
 * @author mtazari
 *
 */
public class LightingProvider extends ServiceCallee implements LampStateListener {
	static final String LAMP_URI_PREFIX = ProvidedLightingService.LIGHTING_SERVER_NAMESPACE +
			"controlledLamp";
	static final String LOCATION_URI_PREFIX = "urn:aal_space:myHome#";
	
	private static final ServiceResponse invalidInput = new ServiceResponse(
			CallStatus.serviceSpecificFailure);
	static {
		invalidInput.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
				"Invalid input!"));
	}
	
	private MyLighting theServer;
	private ContextPublisher cp;

	LightingProvider(BundleContext context) {
		// The parent need to know the profiles of the available functions to register them
		super(context, ProvidedLightingService.profiles);
		
		// prepare for context publishing by create a ContextProvider object
		ContextProvider info =  new ContextProvider(
					ProvidedLightingService.LIGHTING_SERVER_NAMESPACE + "LightingContextProvider");
		info.setType(ContextProviderType.controller);
		cp = new DefaultContextPublisher(context, info);
		
		// initialize the helper class that will save the available lights (their number is defined in MyLighting)
		theServer = new MyLighting();
		theServer.addListener(this);
	}
	/* (non-Javadoc)
	 * @see org.persona.middleware.service.ServiceCallee#communicationChannelBroken()
	 */
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub
	}
	
	// create a service response that including all available light sources
	private ServiceResponse getControlledLamps() {
		ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
		int[] lamps = theServer.getLampIDs();
		ArrayList al = new ArrayList(lamps.length);
		for (int i=0; i<lamps.length; i++)
			al.add(new LightSource(LAMP_URI_PREFIX + lamps[i]));
		sr.addOutput(new ProcessOutput(ProvidedLightingService.OUTPUT_CONTROLLED_LAMPS, al));
		return sr;
	}
	
	private ServiceResponse getLampInfo(String lampURI) {
		try {
			int lampID = Integer.parseInt(lampURI.substring(LAMP_URI_PREFIX.length()));
			String loc = theServer.getLampLocation(lampID);
			int state = theServer.isOn(lampID)? 100 : 0;
			ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
			sr.addOutput(new ProcessOutput(ProvidedLightingService.OUTPUT_LAMP_BRIGHTNESS,
					new Integer(state)));
			sr.addOutput(new ProcessOutput(ProvidedLightingService.OUTPUT_LAMP_LOCATION,
					new PLocation(LOCATION_URI_PREFIX + loc, new RoomPlace())));
			return sr;
		} catch (Exception e) {
			return invalidInput;
		}
	}

	/* (non-Javadoc)
	 * @see org.persona.middleware.service.ServiceCallee#handleCall(org.persona.middleware.service.ServiceCall)
	 * 
	 * Since this class is a child of ServiceCallee it is registered to the service-bus
	 * Every service call that passes the restrictions will take affect here
	 * Given by the URI of the request we know what specific function we have to call
	 */
	public ServiceResponse handleCall(ServiceCall call) {
		if (call == null)
			return null;
		
		String operation = call.getProcessURI();
		if (operation == null)
			return null;
		
		if (operation.startsWith(ProvidedLightingService.SERVICE_GET_CONTROLLED_LAMPS))
			return getControlledLamps();
		
		Object input = call.getInputValue(ProvidedLightingService.INPUT_LAMP_URI);
		if (input == null)
			return null;
		
		if (operation.startsWith(ProvidedLightingService.SERVICE_GET_LAMP_INFO))
			return getLampInfo(input.toString());
		
		if (operation.startsWith(ProvidedLightingService.SERVICE_TURN_OFF))
			return turnOff(input.toString());
		
		if (operation.startsWith(ProvidedLightingService.SERVICE_TURN_ON))
			return turnOn(input.toString());
		
		return null;
	}

	/* (non-Javadoc)
	 * @see de.fhg.igd.ima.persona.lighting.server.unit_impl.LampStateListener#lampStateChanged(int, java.lang.String, boolean)
	 * 
	 *  To demonstrate the functionality of the context bus we publish an event for every time the value of a lamp is changed
	 */
	public void lampStateChanged(int lampID, String loc, boolean isOn) {
		// Create an object that defines a specific lamp
		LightSource ls = new LightSource(LightingProvider.LAMP_URI_PREFIX + lampID);
		// Set the properties of the light (location and brightness)
		ls.setSourceLocation(new PLocation(LightingProvider.LOCATION_URI_PREFIX + loc, new RoomPlace()));
		ls.setBrightness(isOn? 100 : 0);
		Activator.log.log(LogService.LOG_INFO,
				"LightingProvider: publishing a context event on the state of a lamp!");
		// finally create an context event and publish it
		cp.publish(new ContextEvent(ls, LightSource.PROP_SOURCE_BRIGHTNESS));
	}
	
	private ServiceResponse turnOff(String lampURI) {
		try {
			theServer.turnOff(Integer.parseInt(lampURI.substring(LAMP_URI_PREFIX.length())));
			return new ServiceResponse(CallStatus.succeeded);
		} catch (Exception e) {
			return invalidInput;
		}
	}
	
	private ServiceResponse turnOn(String lampURI) {
		try {
			theServer.turnOn(Integer.parseInt(lampURI.substring(LAMP_URI_PREFIX.length())));
			return new ServiceResponse(CallStatus.succeeded);
		} catch (Exception e) {
			return invalidInput;
		}
	}
}
