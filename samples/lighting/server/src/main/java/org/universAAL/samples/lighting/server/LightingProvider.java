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

import java.util.ArrayList;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.Enumeration;
import org.universAAL.middleware.owl.Intersection;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.lighting.ElectricLight;
import org.universAAL.ontology.lighting.LightSource;
import org.universAAL.ontology.location.indoor.Room;
import org.universAAL.samples.lighting.server.unit_impl.LampStateListener;
import org.universAAL.samples.lighting.server.unit_impl.MyLighting;

/**
 * @author mtazari
 * 
 */
public class LightingProvider extends ServiceCallee implements
	LampStateListener {

    // this is just to prepare a standard error message for later use
    private static final ServiceResponse invalidInput = new ServiceResponse(
	    CallStatus.serviceSpecificFailure);
    static {
	invalidInput.addOutput(new ProcessOutput(
		ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input!"));
    }

    /************** THE MAPPING NOTE ****************/
    // when binding components to universAAL that are originally using other
    // conventions for doing their job, you always have to do mappings between
    // non-universAAL stuff and universAAL-compliant stuff
    // example stuff to map are: IDs, the representation of the actual data, and
    // the exported operations
    /*********** END OF THE MAPPING NOTE *************/

    // the following two constants and the three methods following them perform
    // the ID mapping for this example (see THE MAPPING NOTE above)
    static final String LAMP_URI_PREFIX = ProvidedLightingService.LIGHTING_SERVER_NAMESPACE
	    + "controlledLamp";
    static final String LOCATION_URI_PREFIX = "urn:aal_space:myHome#";

    private static String constructLampURIfromLocalID(int localID) {
	return LAMP_URI_PREFIX + localID;
    }

    private static String constructLocationURIfromLocalID(String localID) {
	return LOCATION_URI_PREFIX + localID;
    }

    private static int extractLocalIDfromLampURI(String lampURI) {
	return Integer.parseInt(lampURI.substring(LAMP_URI_PREFIX.length()));
    }

    // end of preparations for ID mapping

    // this is a helper method called from the next method that also
    // demonstrates the case of mapping data representation, e.g. light sources
    // and locations as explicitly sharable objects
    private static LightSource[] getAllLightSources(MyLighting theServer) {
	int[] lamps = theServer.getLampIDs();
	LightSource[] result = new LightSource[lamps.length];
	for (int i = 0; i < lamps.length; i++)
	    result[i] = new LightSource(
	    // first param: instance URI
		    constructLampURIfromLocalID(lamps[i]),
		    // second param: light type
		    ElectricLight.lightBulb,
		    // thrid param: light location
		    new Room(constructLocationURIfromLocalID(theServer
			    .getLampLocation(lamps[i]))));
	return result;
    }

    /**
     * Helper method to construct the ontological declaration of context events
     * published by LightingProvider.
     */
    private static ContextEventPattern[] providedEvents(MyLighting theServer) {
	// the LightingProvioder publishes its context events only from within
	// "lampStateChanged()" below

	// here, we must try to ontologically describe the nature of those
	// context events; to put it casually, we know that these events have
	// always a light source as subject and that the event is always about
	// the change of their brightness; the used value for the brightness is
	// always either 0% or 100%, meaning that these light sources cannot be
	// dimmed

	// we do this by providing two alternative descriptions based on two
	// disjoint assumptions:

	// Assumption 1: "theServer" below controls only a pre-determined set of
	// light sources without any dynamic changes

	// Assumption 2: light sources controlled by "theServer" below might
	// change dynamically; new light sources can always be added and
	// existing ones might disappear and even might come back again

	// however, the following is for both alternatives equal, namely

	// 1) that the event is always about the change of brightness
	MergedRestriction predicateRestriction = MergedRestriction
		.getFixedValueRestriction(ContextEvent.PROP_RDF_PREDICATE,
			LightSource.PROP_SOURCE_BRIGHTNESS);

	// and 2) that the reported value will always be either 0 or 100
	MergedRestriction objectRestriction = MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			ContextEvent.PROP_RDF_OBJECT, new Enumeration(
				new Integer[] { new Integer(0),
					new Integer(100) }), 1, 1);

	// now we demonstrate how each of the two alternatives discussed above
	// would work for describing the subjects of our context events

	// let's start with the variant 1 under the assumption 1
	// in this case, we can say that the subject of the context events is
	// always a member of a given set
	// in order to build this set, we must first fetch the set members from
	// a helper method
	LightSource[] myLights = getAllLightSources(theServer);

	// the following is to say that the subject of my context events is
	// always one single member of the above array
	MergedRestriction subjectRestriction = MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			ContextEvent.PROP_RDF_SUBJECT,
			new Enumeration(myLights), 1, 1);

	// now we can close the first variant by creating a ContextEventPattern
	// and adding the above restrictions to it
	ContextEventPattern cep1 = new ContextEventPattern();
	cep1.addRestriction(subjectRestriction);
	cep1.addRestriction(predicateRestriction);
	cep1.addRestriction(objectRestriction);

	// now, let's switch to the variant 2 under the assumption 2 for an
	// alternative way of describing the subject part

	// the subject of these context events is always an instance of
	// LightSource of type "light bulb" whose property srcLocation is always
	// an instance of Room
	Intersection xsection = new Intersection();
	xsection.addType(new TypeURI(LightSource.MY_URI, false));
	xsection.addType(MergedRestriction.getFixedValueRestriction(
		LightSource.PROP_HAS_TYPE, ElectricLight.lightBulb));
	xsection.addType(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			LightSource.PROP_PHYSICAL_LOCATION, Room.MY_URI, 1, 1));
	subjectRestriction = MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			ContextEvent.PROP_RDF_SUBJECT, xsection, 1, 1);

	// now we can close the second variant as well, by creating a
	// ContextEventPattern and adding the above restrictions to it
	ContextEventPattern cep2 = new ContextEventPattern();
	cep2.addRestriction(subjectRestriction);
	cep2.addRestriction(predicateRestriction);
	cep2.addRestriction(objectRestriction);

	// we must actually make a decision and return only one of the above
	// alternatives, but here we return both in order to indicate that
	// context providers might provide different classes of context events
	// and hence might be forced to return several such descriptions of
	// their events
	return new ContextEventPattern[] { cep1, cep2 };
    }

    // the original server being here wrapped and bound to universAAL
    private MyLighting theServer;

    // needed for publishing context events (whenever you think that it might be
    // important to share a new info with other components in a universAAL-based
    // AAL SPace, you have to publish that info as a context event
    private ContextPublisher cp;

    LightingProvider(ModuleContext context) {
	// as a service providing component, we have to extend ServiceCallee
	// this in turn requires that we introduce which services we would like
	// to
	// provide to the universAAL-based AAL Space
	super(context, ProvidedLightingService.profiles);

	// this is just an example that wraps a faked "original server"
	theServer = new MyLighting();

	// prepare for context publishing
	ContextProvider info = new ContextProvider(
		ProvidedLightingService.LIGHTING_SERVER_NAMESPACE
			+ "LightingContextProvider");
	info.setType(ContextProviderType.controller);
	info.setProvidedEvents(providedEvents(theServer));
	cp = new DefaultContextPublisher(context, info);

	// now we are ready to listen to the changes on the server side
	theServer.addListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.service.ServiceCallee#communicationChannelBroken
     * ()
     */
    public void communicationChannelBroken() {
	// TODO Auto-generated method stub
    }

    // create a service response that including all available light sources
    private ServiceResponse getControlledLamps() {
	// We assume that the Service-Call always succeeds because we only
	// simulate the lights
	ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	// create a list including the available lights
	int[] lamps = theServer.getLampIDs();
	ArrayList al = new ArrayList(lamps.length);
	for (int i = 0; i < lamps.length; i++) {
	    LightSource ls = new LightSource(
		    constructLampURIfromLocalID(lamps[i]));
	    ls.setLightType(ElectricLight.lightBulb);
	    al.add(ls);
	}
	// create and add a ProcessOutput-Event that binds the output URI to the
	// created list of lamps
	sr.addOutput(new ProcessOutput(
		ProvidedLightingService.OUTPUT_CONTROLLED_LAMPS, al));
	return sr;
    }

    // create a service response with informations about the available lights
    private ServiceResponse getLampInfo(String lampURI) {
	try {
	    // collect the needed data
	    int lampID = extractLocalIDfromLampURI(lampURI);
	    String loc = theServer.getLampLocation(lampID);
	    int state = theServer.isOn(lampID) ? 100 : 0;
	    // We assume that the Service-Call always succeeds because we only
	    // simulate the lights
	    ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	    // create and add a ProcessOutput-Event that binds the output URI to
	    // the state of the lamp
	    sr.addOutput(new ProcessOutput(
		    ProvidedLightingService.OUTPUT_LAMP_BRIGHTNESS,
		    new Integer(state)));
	    // create and add a ProcessOutput-Event that binds the output URI to
	    // the location of the lamp
	    sr.addOutput(new ProcessOutput(
		    ProvidedLightingService.OUTPUT_LAMP_LOCATION, new Room(
			    constructLocationURIfromLocalID(loc))));
	    return sr;
	} catch (Exception e) {
	    return invalidInput;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.service.ServiceCallee#handleCall(org.universAAL
     * .middleware.service.ServiceCall)
     * 
     * Since this class is a child of ServiceCallee it is registered to the
     * service-bus Every service call that passes the restrictions will take
     * affect here Given by the URI of the request we know what specific
     * function we have to call
     */
    public ServiceResponse handleCall(ServiceCall call) {
	if (call == null)
	    return null;

	String operation = call.getProcessURI();
	if (operation == null)
	    return null;

	if (operation
		.startsWith(ProvidedLightingService.SERVICE_GET_CONTROLLED_LAMPS))
	    return getControlledLamps();

	Object input = call
		.getInputValue(ProvidedLightingService.INPUT_LAMP_URI);
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

    /*
     * (non-Javadoc)
     * 
     * @seeorg.universAAL.samples.lighting.server.unit_impl.LampStateListener#
     * lampStateChanged(int, java.lang.String, boolean)
     * 
     * To demonstrate the functionality of the context bus we publish an event
     * for every time the value of a lamp is changed
     */
    public void lampStateChanged(int lampID, String loc, boolean isOn) {
	// Create an object that defines a specific lamp
	LightSource ls = new LightSource(constructLampURIfromLocalID(lampID));
	// Set the properties of the light (location and brightness)
	ls.setLocation(new Room(constructLocationURIfromLocalID(loc)));
	ls.setBrightness(isOn ? 100 : 0);
	LogUtils
		.logInfo(
			Activator.mc,
			LightingProvider.class,
			"lampStateChanged",
			new Object[] { "publishing a context event on the state of a lamp!" },
			null);
	// finally create an context event and publish it with the light source
	// as subject and the property that changed as predicate
	cp.publish(new ContextEvent(ls, LightSource.PROP_SOURCE_BRIGHTNESS));
    }

    // Simple use the turnOff method from the ProvidedLightingService
    private ServiceResponse turnOff(String lampURI) {
	try {
	    theServer.turnOff(extractLocalIDfromLampURI(lampURI));
	    return new ServiceResponse(CallStatus.succeeded);
	} catch (Exception e) {
	    return invalidInput;
	}
    }

    // Simple use the turnOn method from the ProvidedLightingService
    private ServiceResponse turnOn(String lampURI) {
	try {
	    theServer.turnOn(extractLocalIDfromLampURI(lampURI));
	    return new ServiceResponse(CallStatus.succeeded);
	} catch (Exception e) {
	    return invalidInput;
	}
    }
}
