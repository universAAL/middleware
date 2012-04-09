/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
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
import org.universAAL.middleware.sodapop.AbstractBus;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;
import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.util.ResourceComparator;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ServiceBusImpl extends AbstractBus implements ServiceBus {

    public static ModuleContext moduleContext;
    public static Object[] busFetchParams;
    public static Object[] contentSerializerParams;
    private static MessageContentSerializer contentSerializer = null;
    private static ServiceBusOntology serviceOntology = new ServiceBusOntology();

    public static synchronized void assessContentSerialization(Resource content) {
	if (Constants.debugMode()) {
	    if (contentSerializer == null) {
		contentSerializer = (MessageContentSerializer) moduleContext
			.getContainer().fetchSharedObject(moduleContext,
				contentSerializerParams);
		if (contentSerializer == null)
		    return;
	    }

	    LogUtils
		    .logDebug(
			    moduleContext,
			    ServiceBusImpl.class,
			    "assessContentSerialization",
			    new Object[] { "Assessing message content serialization:" },
			    null);
	    // System.out.println(new RuntimeException().getStackTrace()[1]);

	    String str = contentSerializer.serialize(content);
	    LogUtils
		    .logDebug(
			    moduleContext,
			    ServiceBusImpl.class,
			    "assessContentSerialization",
			    new Object[] { "\n      1. serialization dump\n",
				    str,
				    "\n      2. deserialize & compare with the original resource\n" },
			    null);
	    new ResourceComparator().printDiffs(content,
		    (Resource) contentSerializer.deserialize(str));
	}
    }

    public static void startModule() {
	OntologyManagement.getInstance().register(serviceOntology);
    }

    public static void stopModule() {
	OntologyManagement.getInstance().unregister(serviceOntology);
    }

    public ServiceBusImpl(SodaPop g) {
	super(Constants.uAAL_BUS_NAME_SERVICE, new ServiceStrategy(g), g);
	busStrategy.setBus(this);
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#addAvailabilitySubscription(String,
     *      AvailabilitySubscriber, ServiceRequest)
     */
    public void addAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, ServiceRequest request) {
	if (callerID != null
		&& callerID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)
		&& registry.get(callerID
			.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				.length())) instanceof ServiceCaller)
	    ((ServiceStrategy) busStrategy).addAvailabilitySubscription(
		    callerID, subscriber, request);
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#addNewRegParams(String,
     *      ServiceProfile[])
     */
    public void addNewRegParams(String calleeID,
	    ServiceProfile[] realizedServices) {
	if (calleeID != null
		&& calleeID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    ((ServiceStrategy) busStrategy).addRegParams(calleeID,
		    realizedServices);
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#getAllServices(String)
     */
    public ServiceProfile[] getAllServices(String callerID) {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#getMatchingService(String,
     *      Service)
     */
    public ServiceProfile[] getMatchingService(String callerID, Service s) {
	return ((ServiceStrategy) busStrategy).getAllServiceProfiles(s
		.getType());
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#getMatchingService(String,
     *      Service[])
     */
    public ServiceProfile[] getMatchingService(String callerID,
	    String[] keywords) {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * not used method
     */
    public String register(BusMember member) {
	return null;
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#register(ServiceCallee,
     *      ServiceProfile[])
     */
    public String register(ServiceCallee callee,
	    ServiceProfile[] realizedServices) {
	String id = Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		+ super.register(callee);
	if (realizedServices != null)
	    ((ServiceStrategy) busStrategy).addRegParams(id, realizedServices);
	return id;
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#register(ServiceCaller)
     */
    public String register(ServiceCaller caller) {
	return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		+ super.register(caller);
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#removeAvailabilitySubscription(String,
     *      AvailabilitySubscriber, String)
     */
    public void removeAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, String requestURI) {
	if (callerID != null
		&& callerID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)
		&& registry.get(callerID
			.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				.length())) instanceof ServiceCaller)
	    ((ServiceStrategy) busStrategy).removeAvailabilitySubscription(
		    callerID, subscriber, requestURI);
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#removeMatchingRegParams(String,
     *      ServiceProfile[])
     */
    public void removeMatchingRegParams(String calleeID,
	    ServiceProfile[] realizedServices) {
	if (calleeID != null
		&& calleeID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    ((ServiceStrategy) busStrategy).removeMatchingRegParams(calleeID,
		    realizedServices);
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#sendReply(String,
     *      Message)
     */
    public void sendReply(String calleeID, Message response) {
	if (calleeID != null
		&& calleeID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    super.sendMessage(calleeID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()), response);
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#sendMessage(String,
     *      Message)
     */
    public void sendMessage(String callerID, Message request) {
	if (callerID != null
		&& callerID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    super.sendMessage(callerID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()), request);
    }

    /**
     * the method is not used
     */
    public void unregister(String id, BusMember member) {
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#unregister(String,
     *      ServiceCallee)
     */
    public void unregister(String calleeID, ServiceCallee callee) {
	if (calleeID != null
		&& calleeID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
	    ((ServiceStrategy) busStrategy).removeRegParams(calleeID);
	    super.unregister(calleeID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()), callee);
	}
    }

    /**
     * @see org.universAAL.middleware.service.ServiceBus#unregister(String,
     *      ServiceCaller)
     */
    public void unregister(String callerID, ServiceCaller caller) {
	if (callerID != null
		&& callerID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    super.unregister(callerID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()), caller);
    }
}
