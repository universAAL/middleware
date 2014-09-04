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
package org.universAAL.middleware.service.test.util;

import org.universAAL.middleware.service.AggregatingFilterFactory;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.test.ontology.DeviceService;
import org.universAAL.middleware.service.test.ontology.IndoorPlace;
import org.universAAL.middleware.service.test.ontology.Lamp;
import org.universAAL.middleware.service.test.ontology.LampService;
import org.universAAL.middleware.service.test.ontology.Location;
import org.universAAL.middleware.service.test.ontology.PhysicalThing;
import org.universAAL.middleware.service.test.ontology.Room;

public class RequestUtil {

    public static final String CONSUMER_NAMESPACE = "http://ontology.igd.fhg.de/Consumer.owl#";
    public static final String lampURI = CONSUMER_NAMESPACE + "lampURI";
    public static final String OUTPUT_LIST_OF_LAMPS = CONSUMER_NAMESPACE
	    + "controlledLamps";

    public static String[] ppControls;
    public static String[] ppBrightness;
    public static String[] ppLocation;

    static {
	// Help structures to define property paths used more than once below
	ppControls = new String[] { DeviceService.PROP_CONTROLS };
	ppBrightness = new String[] { DeviceService.PROP_CONTROLS,
		Lamp.PROP_SOURCE_BRIGHTNESS };
	ppLocation = new String[] { DeviceService.PROP_CONTROLS,
		PhysicalThing.PROP_PHYSICAL_LOCATION };
    }


    // *****************************************************************
    // Services Requests
    // *****************************************************************

    private static ServiceRequest createRequest(boolean dev) {
	if (dev)
	    return new ServiceRequest(new DeviceService(), null);
	else
	    return new ServiceRequest(new LampService(), null);
    }

    public static ServiceRequest getAllLampsRequest(boolean dev) {
	ServiceRequest getAllLamps = createRequest(dev);
	getAllLamps.addRequiredOutput(OUTPUT_LIST_OF_LAMPS, ppControls);
	return getAllLamps;
    }

    public static ServiceRequest getAllLampsRequest2(boolean dev) {
	ServiceRequest getAllLamps = createRequest(dev);
	getAllLamps.addRequiredOutput(OUTPUT_LIST_OF_LAMPS, ppControls);
	getAllLamps.addTypeFilter(ppControls, Lamp.MY_URI);
	return getAllLamps;
    }

    public static ServiceRequest getAllLampsRequest3() {
	ServiceRequest getAllLamps = createRequest(true);
	getAllLamps.addRequiredOutput(OUTPUT_LIST_OF_LAMPS, ppControls);
	getAllLamps.addAggregatingFilter(AggregatingFilterFactory
		.createServiceSelectionFilter());
	return getAllLamps;
    }

    public static ServiceRequest turnOffRequest(boolean dev) {
	ServiceRequest turnOff = createRequest(dev);
	turnOff.addValueFilter(ppControls, new Lamp(lampURI));
	turnOff.addChangeEffect(ppBrightness, new Integer(0));
	return turnOff;
    }

    public static ServiceRequest turnOnRequest(boolean dev) {
	ServiceRequest turnOn = createRequest(dev);
	turnOn.addValueFilter(ppControls, new Lamp(lampURI));
	turnOn.addChangeEffect(ppBrightness, new Integer(100));
	return turnOn;
    }

    public static ServiceRequest dimRequest(boolean dev, Integer percent) {
	ServiceRequest dim = createRequest(dev);
	dim.addValueFilter(ppControls, new Lamp(lampURI));
	dim.addChangeEffect(ppBrightness, percent);
	return dim;
    }

    public static ServiceRequest getAllLampsFromLocation() {
	ServiceRequest getAllLamps = createRequest(false);
	getAllLamps.addTypeFilter(ppLocation, Location.MY_URI);
	getAllLamps.addRequiredOutput(OUTPUT_LIST_OF_LAMPS, ppControls);
	// System.out.println(getAllLamps.toStringRecursive());
	return getAllLamps;
    }

    public static ServiceRequest getAllLampsFromIndoors() {
	ServiceRequest getAllLamps = createRequest(false);
	getAllLamps.addTypeFilter(ppLocation, IndoorPlace.MY_URI);
	getAllLamps.addRequiredOutput(OUTPUT_LIST_OF_LAMPS, ppControls);
	// System.out.println(getAllLamps.toStringRecursive());
	return getAllLamps;
    }

    public static ServiceRequest getAllLampsFromRoom() {
	ServiceRequest getAllLamps = createRequest(false);
	getAllLamps.addTypeFilter(ppLocation, Room.MY_URI);
	getAllLamps.addRequiredOutput(OUTPUT_LIST_OF_LAMPS, ppControls);
	return getAllLamps;
    }
}
