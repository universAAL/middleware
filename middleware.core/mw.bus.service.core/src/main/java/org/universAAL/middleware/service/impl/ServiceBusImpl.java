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
package org.universAAL.middleware.service.impl;

import java.util.HashMap;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.AvailabilitySubscriber;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owl.ServiceBusOntology;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.util.ResourceComparator;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ServiceBusImpl extends AbstractBus implements ServiceBus {

    private static Object[] busFetchParams;
    private static ServiceBusImpl theServiceBus = null;
    private static ServiceBusOntology serviceOntology = new ServiceBusOntology();
    private static ModuleContext mc;

    public static Object[] getServiceBusFetchParams() {
	return busFetchParams.clone();
    }

    public synchronized void assessContentSerialization(Resource content) {
	if (Constants.debugMode()) {
	    LogUtils.logDebug(
		    context,
		    ServiceBusImpl.class,
		    "assessContentSerialization",
		    new Object[] { "Assessing message content serialization:" },
		    null);

	    String str = BusMessage.trySerializationAsContent(content);
	    LogUtils.logDebug(
		    context,
		    ServiceBusImpl.class,
		    "assessContentSerialization",
		    new Object[] { "\n      1. serialization dump\n", str,
			    "\n      2. deserialize & compare with the original resource\n" },
		    null);
	    new ResourceComparator().printDiffs(content,
		    (Resource) BusMessage.deserializeAsContent(str));
	}
    }

    public static synchronized void startModule(Container c, ModuleContext mc,
	    Object[] serviceBusShareParams, Object[] serviceBusFetchParams) {
	if (theServiceBus == null) {
	    ServiceBusImpl.mc = mc;
	    OntologyManagement.getInstance().register(mc, serviceOntology);
	    theServiceBus = new ServiceBusImpl(mc);
	    busFetchParams = serviceBusFetchParams;
	    c.shareObject(mc, theServiceBus, serviceBusShareParams);
	}
    }

    public static void stopModule() {
	if (theServiceBus != null) {
	    OntologyManagement.getInstance().unregister(mc, serviceOntology);
	    theServiceBus.dispose();
	    theServiceBus = null;
	}

    }

    private ServiceBusImpl(ModuleContext mc) {
	super(mc, "mw.bus.service.osgi");
	busStrategy.setBus(this);
    }

    public static ModuleContext getModuleContext() {
	return mc;
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#addAvailabilitySubscription(String,
     *      AvailabilitySubscriber, ServiceRequest)
     */
    public void addAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, ServiceRequest request) {
	if (callerID != null
		&& registry.getBusMemberByID(callerID) instanceof ServiceCaller) {
	    ((ServiceStrategy) busStrategy).addAvailabilitySubscription(
		    callerID, subscriber, request);
	}
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#addNewServiceProfiles(String,
     *      ServiceProfile[])
     */
    public void addNewServiceProfiles(String calleeID,
	    ServiceProfile[] realizedServices) {
	if (calleeID != null) {
	    ((ServiceStrategy) busStrategy).addRegParams(calleeID,
		    realizedServices);
	}
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#getAllServices(String)
     */
    public ServiceProfile[] getAllServices(String callerID) {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * @see ServiceBus#getMatchingServices(String, Service)
     */
    public ServiceProfile[] getMatchingServices(String callerID, Service s) {
	return ((ServiceStrategy) busStrategy).getAllServiceProfiles(s
		.getType());
    }

    /**
     * @see ServiceBus#getMatchingService(String, String)
     */
    public ServiceProfile[] getMatchingServices(String callerID, String s) {
	return ((ServiceStrategy) busStrategy).getAllServiceProfiles(s);
    }

    /**
     * @see ServiceBus#getMatchingService(String)
     */
    public HashMap getMatchingServices(String s) {
	return ((ServiceStrategy) busStrategy)
		.getAllServiceProfilesWithCalleeIDs(s);
    }

    /**
     * @see ServiceBus#getMatchingServices(String, String[])
     */
    public ServiceProfile[] getMatchingServices(String callerID,
	    String[] keywords) {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * @see ServiceBus#removeAvailabilitySubscription(String,
     *      AvailabilitySubscriber, String)
     */
    public void removeAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, String requestURI) {
	if (callerID != null
		&& registry.getBusMemberByID(callerID) instanceof ServiceCaller) {
	    ((ServiceStrategy) busStrategy).removeAvailabilitySubscription(
		    callerID, subscriber, requestURI);
	}
    }

    /**
     * @see ServiceBus#removeMatchingRegParams(String, ServiceProfile[])
     */
    public void removeMatchingProfiles(String calleeID,
	    ServiceProfile[] realizedServices) {
	if (calleeID != null) {
	    ((ServiceStrategy) busStrategy).removeMatchingRegParams(calleeID,
		    realizedServices);
	}
    }

    /**
     * @see AbstractBus#brokerMessage(String, BusMessage)
     */
    public void brokerReply(String calleeID, BusMessage response) {
	if (calleeID != null) {
	    super.brokerMessage(calleeID, response);
	}
    }

    /**
     * @see AbstractBus#brokerMessage(String, BusMessage)
     */
    public void brokerRequest(String callerID, BusMessage request) {
	if (callerID != null) {
	    Object content = request.getContent();
	    if (content instanceof ServiceRequest) {
		ServiceRequest sr = (ServiceRequest) content;
		assessContentSerialization(sr);
	    }

	    super.brokerMessage(callerID, request);
	}
    }

    /**
     * @see ServiceBus#unregister(String, ServiceCallee)
     */
    public void unregister(String calleeID, ServiceCallee callee) {
	if (calleeID != null) {
	    ((ServiceStrategy) busStrategy).removeRegParams(calleeID);
	    super.unregister(calleeID, callee);
	}
    }

    /**
     * @see ServiceBus#unregister(String, ServiceCaller)
     */
    public void unregister(String callerID, ServiceCaller caller) {
	if (callerID != null) {
	    super.unregister(callerID, caller);
	}
    }
    
    @Override
    public void unregister(String memberID, BusMember m) {
	if (m instanceof ServiceCallee)
	    unregister(memberID, (ServiceCallee) m);
	else if (m instanceof ServiceCaller)
	    unregister(memberID, (ServiceCaller) m);
    }

    @Override
    protected BusStrategy createBusStrategy(CommunicationModule commModule) {
	return new ServiceStrategy(commModule, context);
    }

    public void handleSendError(ChannelMessage message,
	    CommunicationConnectorException e) {
	// TODO Auto-generated method stub
    }

}
