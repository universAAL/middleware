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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.UnmodifiableResource;
import org.universAAL.middleware.service.AggregatingFilter;
import org.universAAL.middleware.service.AggregationFunction;
import org.universAAL.middleware.service.AvailabilitySubscriber;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.data.ILocalServiceSearchResultsData;
import org.universAAL.middleware.service.data.ILocalServicesIndexData;
import org.universAAL.middleware.service.data.ILocalWaitingCallersData;
import org.universAAL.middleware.service.data.factory.IServiceStrategyDataFactory;
import org.universAAL.middleware.service.data.factory.ServiceStrategyDataFactory;
import org.universAAL.middleware.service.owl.InitialServiceDialog;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.process.OutputBinding;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.sodapop.BusStrategy;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageType;
import org.universAAL.middleware.util.Constants;

/**
 * This class implements the BusStrategy for the ServiceBus
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ServiceStrategy extends BusStrategy {
    private static final String PROP_uAAL_REGISTERATION_STATUS = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "registrationStatus";
    private static final String PROP_uAAL_SERVICE_REALIZATION_ID = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theRealization";
    private static final String PROP_uAAL_SERVICE_REGISTERED_PROFILE = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "registeredProfile";
    private static final String PROP_uAAL_SERVICE_PROVIDED_BY = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "registeredBy";
    private static final String PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theRequest";
    private static final String PROP_uAAL_SERVICE_SUBSCRIBER = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theSubscriber";
    private static final String PROP_uAAL_SERVICE_TYPE = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "serviceType";
    private static final Resource RES_STATUS_DEREGISTERED = new Resource(
	    Resource.uAAL_VOCABULARY_NAMESPACE + "deregistered");
    private static final Resource RES_STATUS_REGISTERED = new Resource(
	    Resource.uAAL_VOCABULARY_NAMESPACE + "registered");
    private static final String TYPE_uAAL_SERVICE_BUS_COORDINATOR = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "Coordinator";
    private static final String TYPE_uAAL_SERVICE_BUS_NOTIFICATION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "SubscriberNotification";
    private static final String TYPE_uAAL_SERVICE_BUS_REGISTRATION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "ServiceRegistration";
    private static final String TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "ServiceSubscription";
    private static final String TYPE_uAAL_SERVICE_PROFILE_INFORMATION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "ProfileInformation";
    private static final String CONTEXT_REQUEST_MESSAGE = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "requestMessage";
    private static final String CONTEXT_RESPONSE_MESSAGE = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "responseMessage";
    static final String CONTEXT_SPECIALIZED_CLASS_MATCH = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "specializedClassMatch";
    static final String CONTEXT_SPECIALIZED_INSTANCE_MATCH = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "specializedInstanceMatch";

    private class AvailabilitySubscription {
	String id;
	Object reqOrSubs;
    }

    // serviceURI -> Vector(ServiceRealization)
    private Hashtable allServicesIndex;

    // serviceURI -> Vector(AvailabilitySubscription)
    private Hashtable allSubscriptionsIndex;

    // request.msgID -> Vector(call.context) + call.msgID -> call.context
    private Hashtable allWaitingCallers;

    // requestURI -> serviceURI + callerURI -> Vector(AvailabilitySubscription)
    private Hashtable localSubscriptionsIndex;

    // serviceURI -> Vector(ServiceRealization)
    private Hashtable startDialogs;

    // request.msgID -> callerID
    protected ILocalWaitingCallersData localWaitingCallers;
    // processURI -> ServiceRealization
    protected ILocalServicesIndexData localServicesIndex;
    // serviceURI -> List(ServiceRealization) (was replaced with the new
    // mechanism)
    protected ILocalServiceSearchResultsData localServiceSearchResults;
    private boolean isCoordinator;
    protected String theCoordinator = null;

    public ServiceStrategy(SodaPop sodapop) {
	super(sodapop);

	// Initiated the factory
	IServiceStrategyDataFactory factory = createServiceStrategyDataFactory();

	// dummy action to force the load of the class InitialServiceDialog
	theCoordinator = InitialServiceDialog.MY_URI;
	theCoordinator = null;
	// end of dummy action: we had to set the coordinator ID back to null
	// until the real ID is found out
	localSubscriptionsIndex = new Hashtable();
	localServicesIndex = factory.createLocalServicesIndexData();
	localWaitingCallers = factory.createLocalWaitingCallersData();
	localServiceSearchResults = factory
		.createLocalServiceSearchResultsData();
	isCoordinator = Constants.isCoordinatorInstance();
	LogUtils
		.logDebug(
			ServiceBusImpl.moduleContext,
			ServiceStrategy.class,
			"ServiceStrategy",
			new Object[] { "This instance is ",
				isCoordinator ? "" : "NOT ", "the coordinator." },
			null);
	if (isCoordinator) {
	    allServicesIndex = new Hashtable();
	    allSubscriptionsIndex = new Hashtable();
	    allWaitingCallers = new Hashtable();
	    startDialogs = new Hashtable();
	} else {

	}
    }

    protected IServiceStrategyDataFactory createServiceStrategyDataFactory() {
	return new ServiceStrategyDataFactory();
    }

    /**
     * Adds availability subscription (registration and un-registration of
     * services), according to the ServiceRequest
     * 
     * @param id
     *            - the ID of the caller who asked to make the subscription
     * @param subscriber
     *            - the object to be notified about registration event
     * @param request
     *            - the service request to match the the service profiles. The
     *            notifications will be fired only regarding the
     *            registration/unregistration of services with the matching
     *            service profiles.
     * 
     */
    void addAvailabilitySubscription(String id,
	    AvailabilitySubscriber subscriber, ServiceRequest request) {
	if (request == null || subscriber == null || request.isAnon())
	    return;

	AvailabilitySubscription as = new AvailabilitySubscription();
	as.id = request.getURI();
	as.reqOrSubs = subscriber;
	getVector(localSubscriptionsIndex, id).add(as);
	localSubscriptionsIndex.put(as.id, request.getRequestedService()
		.getType());

	if (isCoordinator)
	    addSubscriber(id, request);
	else if (isCoordinatorKnown()) {
	    Resource res = new Resource(id);
	    res.addType(TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION, true);
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST, request);
	    res.setProperty(PROP_uAAL_REGISTERATION_STATUS,
		    RES_STATUS_REGISTERED);
	    ServiceBusImpl.assessContentSerialization(res);
	    Message m = new Message(MessageType.p2p_event, res);
	    m.setReceivers(new String[] { theCoordinator });
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * Add service profiles to a previously registered ServiceCallee
     * 
     * @param calleeID
     *            - the id of the ServiceCallee
     * @param realizedServices
     *            - the profiles to add
     */
    void addRegParams(String calleeID, ServiceProfile[] realizedServices) {
	if (realizedServices == null
		|| calleeID == null
		|| !(getBusMember(calleeID
			.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				.length())) instanceof ServiceCallee))
	    return;

	for (int i = 0; i < realizedServices.length; i++) {
	    // check for qualifications of each realized service
	    if (realizedServices[i] == null
		    || realizedServices[i].getTheService() == null)
		// ignore not-qualified ones
		continue;

	    String processURI = realizedServices[i].getProcessURI();
	    if (processURI == null)
		// ignore not-qualified ones
		continue;

	    // qualifications fulfilled -> associate service with its provider
	    ServiceRealization registration = new ServiceRealization(calleeID,
		    realizedServices[i]);
	    // index it over the ID of the operation registered
	    localServicesIndex.addServiceRealization(processURI, registration);
	    
	    if (isCoordinator)
		// more complex indexing of services by the coordinator
		indexServices(realizedServices[i], registration, processURI);
	}

	if (!isCoordinator && isCoordinatorKnown()) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_REGISTRATION, true);
	    r
		    .setProperty(PROP_uAAL_REGISTERATION_STATUS,
			    RES_STATUS_REGISTERED);
	    r.setProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE, Arrays
		    .asList(realizedServices));
	    r
		    .setProperty(PROP_uAAL_SERVICE_PROVIDED_BY, new Resource(
			    calleeID));
	    ServiceBusImpl.assessContentSerialization(r);
	    Message m = new Message(MessageType.p2p_event, r);
	    m.setReceivers(new String[] { theCoordinator });
	    sodapop.propagateMessage(bus, m);
	} else if (theCoordinator == null) {
	    // using the dummy value "this" to indicate that the coordinator has
	    // at least one registration
	    theCoordinator = "this";
	    // publish an event informing all peers started prior to the
	    // coordinator
	    // about the availability of the coordinator
	    // do this in a thread after waiting 10 seconds to make sure that
	    // the join process within the sodapop engine is closed
	    new Thread() {
		public void run() {
		    try {
			sleep(10000);
			Resource res = new Resource(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
					+ sodapop.getID());
			res.addType(TYPE_uAAL_SERVICE_BUS_COORDINATOR, true);
			ServiceBusImpl.assessContentSerialization(res);
			sodapop.propagateMessage(bus, new Message(
				MessageType.p2p_event, res));
		    } catch (Exception e) {
			// set the flag to redo this step
			theCoordinator = null;
		    }
		}
	    }.start();
	}
    }

    /**
     * Add service availability subscriber that will be notified about
     * registration/unregistration of services, matching according the
     * ServiceRequest passed as a parameter
     * 
     * @param callerID
     *            - the id of the subscriber
     * @param request
     *            - the request to describe the desired services
     */
    private void addSubscriber(String callerID, ServiceRequest request) {
	String serviceURI = request.getRequestedService().getType();
	synchronized (allServicesIndex) {
	    AvailabilitySubscription as = new AvailabilitySubscription();
	    as.id = callerID;
	    as.reqOrSubs = request;
	    getVector(allSubscriptionsIndex, serviceURI).add(as);
	    Vector realizations = (Vector) allServicesIndex.get(serviceURI);
	    if (realizations != null)
		for (Iterator i = realizations.iterator(); i.hasNext();) {
		    ServiceRealization sr = (ServiceRealization) i.next();
		    if (null != matches(callerID, request, sr))
			notifySubscriber(
				as,
				((ServiceProfile) sr
					.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
					.getProcessURI(), true);
		}
	}
    }

    /**
     * Pass the call message to the matching service callees
     * 
     * @param m
     *            - the message
     * @param matches
     *            - a list of hashtables that describe the matched services
     */
    private void callServices(Message m, Vector matches) {
	int size = matches.size();
	matches.add(new Integer(size));
	allWaitingCallers.put(m.getID(), matches);
	int maxTimeout = 0;
	for (int i = 0; i < size; i++) {
	    Hashtable match = (Hashtable) matches.get(i);
	    match.put(CONTEXT_REQUEST_MESSAGE, m);
	    ServiceRealization sr = (ServiceRealization) match
		    .get(Constants.VAR_uAAL_SERVICE_TO_SELECT);
	    Object timeout = ((ServiceProfile) sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
		    .getProperty(ServiceProfile.PROP_uAAL_RESPONSE_TIMEOUT);
	    if (timeout instanceof Integer
		    && ((Integer) timeout).intValue() > maxTimeout)
		maxTimeout = ((Integer) timeout).intValue();
	    String receiver = Constants.extractPeerID(sr.getProperty(
		    ServiceRealization.uAAL_SERVICE_PROVIDER).toString());
	    ServiceCall sc = (ServiceCall) match
		    .remove(ServiceRealization.uAAL_ASSERTED_SERVICE_CALL);
	    if (m.getContent() instanceof ServiceRequest){
		sc.setRequest((ServiceRequest)m.getContent());
	    }
	    ServiceBusImpl.assessContentSerialization(sc);
	    Message call = new Message(MessageType.p2p_request, sc);
	    allWaitingCallers.put(call.getID(), match);
	    if (sodapop.getID().equals(receiver))
		handleMessage(call, null);
	    else {
		call.setReceivers(new String[] { receiver });
		sodapop.propagateMessage(bus, call);
	    }
	}
	if (maxTimeout > 0) {
	    try {
		Thread.sleep(maxTimeout);
	    } catch (Exception e) {
	    }
	    sendServiceResponse(m);
	}
    }

    /**
     * This method starts a general purpose user interaction related to a
     * certain service class. The decision about a concrete goal to reach (if at
     * all) will be taken later during exploring the user interface
     * 
     * @param matchingServices
     *            - the currently matching services for the general purpose user
     *            interaction request
     * @param vendor
     *            - the vendor who provides the currently matching services
     * @param m
     *            - the message request for general purpose user interaction
     */
    private void callStartDialog(Vector matchingServices, String vendor,
	    Message m) {
	if (matchingServices == null) {
	    sendNoMatchingFound(m);
	    return;
	}

	Object calleeID = null, processURI = null;
	for (Iterator i = matchingServices.iterator(); i.hasNext();) {
	    ServiceRealization sr = (ServiceRealization) i.next();
	    if (sr == null)
		continue;
	    ServiceProfile sp = (ServiceProfile) sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
	    if (sp == null)
		continue;
	    Service s = sp.getTheService();
	    if (s == null)
		continue;
	    if (vendor.equals(String.valueOf(s
		    .getProperty(InitialServiceDialog.PROP_HAS_VENDOR)))) {
		processURI = sp.getProcessURI();
		calleeID = sr
			.getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER);
		if (processURI instanceof String && calleeID instanceof String) {
		    Object user = (m.getContent() instanceof ServiceRequest) ? ((ServiceRequest) m
			    .getContent())
			    .getProperty(ServiceRequest.PROP_uAAL_INVOLVED_HUMAN_USER)
			    : null;
		    ServiceCall sc = new ServiceCall((String) processURI);
		    if (user instanceof Resource)
			sc.setInvolvedUser((Resource) user);
		    ServiceBusImpl.assessContentSerialization(sc);
		    Message call = new Message(MessageType.p2p_request, sc);
		    String receiver = Constants
			    .extractPeerID((String) calleeID);
		    if (sodapop.getID().equals(receiver))
			handleMessage(call, null);
		    else {
			call.setReceivers(new String[] { receiver });
			sodapop.propagateMessage(bus, call);
		    }
		    break;
		} else
		    processURI = null;
	    }
	}

	if (processURI == null)
	    sendNoMatchingFound(m);
	else {
	    ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
	    m = m.createReply(resp);
	    if (sodapop.getID().equals(m.getSource()))
		replyToLocalCaller(m);
	    else
		sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * Sends a response to the message passed as a parameter
     * 
     * @param m
     *            - the message, to which the response is sent
     */
    private void sendServiceResponse(Message m) {
	Vector matches = (Vector) allWaitingCallers.remove(m.getID());
	if (matches == null)
	    return;

	String callingPeer = m.getSource();
	synchronized (matches) {
	    int size = matches.size() - 1;
	    int numTimedOut = ((Integer) matches.remove(size)).intValue();
	    if (size == numTimedOut)
		// there has been no one response => this method is called
		// because of timeout!
		m = m.createReply(new ServiceResponse(
			CallStatus.responseTimedOut));
	    else {
		// boolean arrays to indicate which of the responses had which
		// kind of failure
		// nmsf for NO_MATCHING_SERVICE_FOUND, rto for
		// RESPONSE_TIMED_OUT, and
		// ssf for SERVICE_SPECIFIC_FAILURE
		boolean[] nmsf = new boolean[size], rto = new boolean[size], ssf = new boolean[size];
		Vector goods = new Vector(size); // responses with
		// CallStatus.SUCCEEDED
		int bads = 0; // the total number of responses with failure
		for (int i = 0; i < size; i++) {
		    Hashtable match = (Hashtable) matches.get(i);
		    ServiceResponse sr = (ServiceResponse) match
			    .get(CONTEXT_RESPONSE_MESSAGE);
		    if (sr != null) {
			switch (sr.getCallStatus().ord()) {
			case CallStatus.SUCCEEDED:
			    goods.add(match);
			    nmsf[i] = rto[i] = ssf[i] = false;
			    break;
			case CallStatus.NO_MATCHING_SERVICE_FOUND:
			    bads++;
			    nmsf[i] = true;
			    rto[i] = ssf[i] = false;
			    break;
			case CallStatus.RESPONSE_TIMED_OUT:
			    // usually timeout should be captured a dozen lines
			    // below here because if the call is timed out, we
			    // usually do not receive any response but
			    // nevertheless we handle this case because the
			    // callee might send a response with this status
			    // doing bad practice
			    bads++;
			    rto[i] = true;
			    nmsf[i] = ssf[i] = false;
			    break;
			case CallStatus.SERVICE_SPECIFIC_FAILURE:
			    bads++;
			    ssf[i] = true;
			    nmsf[i] = rto[i] = false;
			    break;
			}
		    } else {
			// sr == null => if later there is no good response and
			// the response of this call is used to send the final
			// response, then we must create a pseudo timeout
			// response for this case; but because it is not sure if
			// it is needed we create the pseudo response when we
			// are sure it is needed (see comments a dozen lines
			// below)
			if (numTimedOut-- > 0) {
			    // possibly this is one of those really timed out
			    bads++;
			    rto[i] = true;
			    nmsf[i] = ssf[i] = false;
			} else {
			    // actually not possible, because ServiceCallee does
			    // not allow this
			    nmsf[i] = rto[i] = ssf[i] = false;
			}
		    }
		}
		switch (goods.size()) {
		case 0:
		    if (bads == 0)
			// shouldn't be the case...
			m = m.createReply(new ServiceResponse(
				CallStatus.responseTimedOut));
		    else {
			Hashtable bad = null;
			// if there is one response with
			// SERVICE_SPECIFIC_FAILURE take that one
			for (int i = 0; i < size; i++) {
			    if (ssf[i]) {
				bad = (Hashtable) matches.get(i);
				break;
			    } else if (rto[i])
				bad = (Hashtable) matches.get(i);
			    else if (bad == null)
				bad = (Hashtable) matches.get(i);
			}
			ServiceResponse sr = (ServiceResponse) bad
				.get(CONTEXT_RESPONSE_MESSAGE);
			if (sr == null)
			    // see the 'sr == null' comment a dozen lines above
			    sr = new ServiceResponse(
				    CallStatus.responseTimedOut);
			m = m.createReply(sr);
		    }
		    break;
		case 1:
		    Hashtable match = (Hashtable) goods.get(0);
		    ServiceResponse sr = (ServiceResponse) match
			    .get(CONTEXT_RESPONSE_MESSAGE);
		    prepareRequestedOutput(sr.getOutputs(), match);
		    m = m.createReply(sr);
		    break;
		default:
		    size = goods.size();
		    List aggregations = ((ServiceRequest) m.getContent())
			    .getOutputAggregations();
		    if (!aggregations.isEmpty()) {
			int[] points = new int[size];
			for (int i = 0; i < points.length; i++)
			    points[i] = 0;
			for (Iterator i = aggregations.iterator(); i.hasNext();) {
			    AggregatingFilter af = (AggregatingFilter) i.next();
			    List params = af.getFunctionParams();
			    switch (af.getTheFunction().ord()) {
			    case AggregationFunction.ONE_OF:
				break;
			    case AggregationFunction.MIN_OF:
				for (int j = 0; j < size; j++) {
				    Object oj = getOutputValue(
					    (Hashtable) goods.get(j), af);
				    for (int k = j + 1; k < size; k++) {
					Object ok = getOutputValue(
						(Hashtable) goods.get(k), af);
					if (oj instanceof Comparable)
					    if (ok == null)
						points[k]++;
					    else {
						int l = ((Comparable) oj)
							.compareTo(ok);
						if (l < 0)
						    points[k]++;
						else if (l > 0)
						    points[j]++;
					    }
					else {
					    points[j]++;
					    if (!(ok instanceof Comparable))
						points[k]++;
					}
				    }
				}
				break;
			    case AggregationFunction.MAX_OF:
				for (int j = 0; j < size; j++) {
				    Object oj = getOutputValue(
					    (Hashtable) goods.get(j), af);
				    for (int k = j + 1; k < size; k++) {
					Object ok = getOutputValue(
						(Hashtable) goods.get(k), af);
					if (oj instanceof Comparable)
					    if (ok == null)
						points[k]++;
					    else {
						int l = ((Comparable) oj)
							.compareTo(ok);
						if (l > 0)
						    points[k]++;
						else if (l < 0)
						    points[j]++;
					    }
					else {
					    points[j]++;
					    if (!(ok instanceof Comparable))
						points[k]++;
					}
				    }
				}
				break;
			    case AggregationFunction.MIN_DISTANCE_TO_REF_LOC:
				for (int j = 0; j < size; j++) {
				    Object oj = getOutputValue(
					    (Hashtable) goods.get(j), af);
				    for (int k = j + 1; k < size; k++) {
					Object ok = getOutputValue(
						(Hashtable) goods.get(k), af);
					if (oj instanceof AbsLocation)
					    if (ok == null)
						points[k]++;
					    else {
						float dj = ((AbsLocation) oj)
							.getDistanceTo((AbsLocation) params
								.get(1));
						float dk = ((AbsLocation) ok)
							.getDistanceTo((AbsLocation) params
								.get(1));
						if (dj < dk)
						    points[k]++;
						else if (dk < dj)
						    points[j]++;
					    }
					else {
					    points[j]++;
					    if (!(ok instanceof AbsLocation))
						points[k]++;
					}
				    }
				}
				break;
			    case AggregationFunction.MAX_DISTANCE_TO_REF_LOC:
				for (int j = 0; j < size; j++) {
				    Object oj = getOutputValue(
					    (Hashtable) goods.get(j), af);
				    for (int k = j + 1; k < size; k++) {
					Object ok = getOutputValue(
						(Hashtable) goods.get(k), af);
					if (oj instanceof AbsLocation)
					    if (ok == null)
						points[k]++;
					    else {
						float dj = ((AbsLocation) oj)
							.getDistanceTo((AbsLocation) params
								.get(1));
						float dk = ((AbsLocation) ok)
							.getDistanceTo((AbsLocation) params
								.get(1));
						if (dj > dk)
						    points[k]++;
						else if (dk > dj)
						    points[j]++;
					    }
					else {
					    points[j]++;
					    if (!(ok instanceof AbsLocation))
						points[k]++;
					}
				    }
				}
				break;
			    }
			}
			int ind = 0, min = points[0];
			for (int i = 1; i < size; i++)
			    if (points[i] < min) {
				ind = i;
				min = points[i];
			    }
			for (int j = 0; j < ind; j++, size--)
			    goods.remove(0);
			while (size > 1)
			    goods.remove(--size);
		    }
		    if (size == 1) {
			// the above aggregations have reduced the number of
			// responses to one
			Hashtable ctxt = (Hashtable) goods.get(0);
			ServiceResponse sresp = (ServiceResponse) ctxt
				.get(CONTEXT_RESPONSE_MESSAGE);
			prepareRequestedOutput(sresp.getOutputs(), ctxt);
			m = m.createReply(sresp);
		    } else {
			// get one of the responses and change its output list
			// to the list of
			// all output lists while calling
			// 'prepareRequestedOutput'
			Hashtable ctxt = null;
			List resultSet = null;
			ServiceResponse resp = null;
			for (int i = 0; resultSet == null && i < size; i++) {
			    ctxt = (Hashtable) goods.get(0);
			    resp = (ServiceResponse) ctxt
				    .get(CONTEXT_RESPONSE_MESSAGE);
			    resultSet = resp.getOutputs();
			}
			if (resultSet != null) {
			    List cloned = new ArrayList(resultSet.size());
			    for (Iterator i = resultSet.iterator(); i.hasNext();) {
				cloned.add(i.next());
				i.remove();
			    }
			    if (!cloned.isEmpty()) {
				prepareRequestedOutput(cloned, ctxt);
				resultSet.add(cloned);
			    }
			    for (int i = 0; i < size; i++) {
				ctxt = (Hashtable) goods.get(i);
				ServiceResponse tmp = (ServiceResponse) ctxt
					.get(CONTEXT_RESPONSE_MESSAGE);
				if (tmp == resp)
				    continue;
				List aux = tmp.getOutputs();
				if (aux != null && !aux.isEmpty()) {
				    prepareRequestedOutput(aux, ctxt);
				    resultSet.add(aux);
				}
			    }
			}
			m = m.createReply(resp);
		    }
		    break;
		}
	    }
	}

	ServiceBusImpl.assessContentSerialization((Resource) m.getContent());
	if (sodapop.getID().equals(callingPeer))
	    // a local caller registered to the coordinator
	    replyToLocalCaller(m);
	else
	    sodapop.propagateMessage(bus, m);
    }

    /**
     * Translates the process outputs according to the bindings
     * 
     * @param outputs
     *            - a list of ProcessOutputs
     * @param context
     *            - hashtable of bindings for the ProcessOutputs
     */
    private void prepareRequestedOutput(List outputs, Hashtable context) {
	if (outputs != null && !outputs.isEmpty())
	    for (int i = outputs.size() - 1; i > -1; i--) {
		ProcessOutput po = (ProcessOutput) outputs.remove(i);
		if (po == null)
		    continue;
		Resource binding = (Resource) context.get(po.getURI());
		if (binding != null) {
		    String mappedURI = binding.getProperty(
			    OutputBinding.PROP_OWLS_BINDING_TO_PARAM)
			    .toString();
		    if (mappedURI == null)
			continue;
		    Object val = po.getParameterValue();
		    if (val == null)
			continue;
		    ProcessOutput substitution = new ProcessOutput(mappedURI);
		    substitution.setParameterValue(val);
		    outputs.add(substitution);
		} else {
		    // UNBOUND_OUTPUT_ALLOWED:
		    // if the binding for given output was not found but Service
		    // Response allows unbound output then the output is
		    // propagated as it is (with the URI specified on the server
		    // side).
		    ServiceResponse sr = (ServiceResponse) context
			    .get(CONTEXT_RESPONSE_MESSAGE);
		    if (sr.isUnboundOutputAllowed()) {
			outputs.add(po);
		    }
		}
	    }
    }

    /**
     * Return a list of non abstract super classes of the service passed as a
     * parameter
     * 
     * @param s
     *            - the service
     * @return Vector - the non-abstract superclasses
     */
    private Vector getNonAbstractSuperClasses(Service s) {
	return ManagedIndividual.getNonAbstractSuperClasses(s);
	// Vector result = new Vector();
	// Class superClass = s.getClass();
	// while (superClass != null) {
	// if (!Modifier.isAbstract(superClass.getModifiers())) {
	// String uri = ManagedIndividual.getRegisteredClassURI(superClass
	// .getName());
	// if (uri != null)
	// result.add(uri);
	// }
	// superClass = superClass.getSuperclass();
	// }
	// return result;
    }

    /**
     * Extract the output value from the context, according to the
     * AggregatingFilter passed as a parameter
     * 
     * @param context
     *            - the context
     * @param af
     *            - the aggregating filter
     * @return - the output
     */
    private Object getOutputValue(Hashtable context, AggregatingFilter af) {
	List outputs = ((ServiceResponse) context.get(CONTEXT_RESPONSE_MESSAGE))
		.getOutputs();
	if (outputs == null || outputs.isEmpty())
	    return null;

	for (Iterator i = context.keySet().iterator(); i.hasNext();) {
	    String key = i.next().toString();
	    Object o = context.get(key);
	    if (o instanceof Resource) {
		o = ((Resource) o)
			.getProperty(OutputBinding.PROP_OWLS_BINDING_VALUE_FUNCTION);
		if (o instanceof AggregatingFilter
			&& ((AggregatingFilter) o).getTheFunction() == af
				.getTheFunction()
			&& af.getFunctionParams().equals(
				((AggregatingFilter) o).getFunctionParams()))
		    for (Iterator j = outputs.iterator(); j.hasNext();) {
			ProcessOutput po = (ProcessOutput) j.next();
			if (key.equals(po.getURI()))
			    return po.getParameterValue();
		    }
	    }
	}

	return null;
    }

    /**
     * Extract profile parameter from the context, according to the property
     * passed as a parameter
     * 
     * @param context
     * @param prop
     *            - the property of the profile paramter to return
     * @return Object - the profile parameter
     */
    private Object getProfileParameter(Hashtable context, String prop) {
	Object o = context.get(prop);
	if (o == null)
	    o = ((ServiceProfile) context
		    .get(ServiceRealization.uAAL_SERVICE_PROFILE))
		    .getProperty(prop);
	return o;
    }

    /**
     * Returns a vector from a hashtable from Strings to Vectors. If no vector
     * exists in the hashtable according to the key passed as a parameter, an
     * empty vector is inserted in the hashtable according to the key
     * 
     * @param t
     *            - the hashtable
     * @param k
     *            - the key
     * @return Vector - the value of the key from the hashtable
     */
    private Vector getVector(Hashtable t, String k) {
	Vector m = (Vector) t.get(k);
	if (m == null) {
	    m = new Vector();
	    t.put(k, m);
	}
	return m;
    }

    /**
     * @see org.universAAL.middleware.sodapop.BusStrategy#handle(org.universAAL.middleware.sodapop.msg.Message,
     *      String)
     */
    public void handle(Message msg, String senderID) {
	Resource res = (Resource) msg.getContent();
	switch (msg.getType().ord()) {
	case MessageType.EVENT:
	    if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_NOTIFICATION))
		notifyLocalSubscriber(res.getProperty(
			PROP_uAAL_SERVICE_SUBSCRIBER).toString(), res
			.getProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST)
			.toString(), res.getProperty(
			PROP_uAAL_SERVICE_REALIZATION_ID).toString(),
			RES_STATUS_REGISTERED.equals(res
				.getProperty(PROP_uAAL_REGISTERATION_STATUS)));
	    break;
	case MessageType.P2P_EVENT:
	    if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION)
		    && isCoordinator) {
		if (RES_STATUS_DEREGISTERED.equals(res
			.getProperty(PROP_uAAL_REGISTERATION_STATUS))) {
		    String serviceURI = res.getProperty(PROP_uAAL_SERVICE_TYPE)
			    .toString(), subscriber = res.getURI(), requestURI = res
			    .getProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST)
			    .toString();
		    synchronized (allServicesIndex) {
			Vector v = (Vector) allSubscriptionsIndex
				.get(serviceURI);
			if (v != null)
			    for (Iterator i = v.iterator(); i.hasNext();) {
				AvailabilitySubscription as = (AvailabilitySubscription) i
					.next();
				if (as.id.equals(subscriber)
					&& as.reqOrSubs.toString().equals(
						requestURI)) {
				    i.remove();
				    return;
				}
			    }
		    }
		} else
		    addSubscriber(res.getURI(), (ServiceRequest) res
			    .getProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST));
	    } else if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_REGISTRATION)
		    && isCoordinator) {
		List profiles = (List) res
			.getProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE);
		String theCallee = res.getProperty(
			PROP_uAAL_SERVICE_PROVIDED_BY).toString();
		if (RES_STATUS_REGISTERED.equals(res
			.getProperty(PROP_uAAL_REGISTERATION_STATUS)))
		    for (Iterator i = profiles.iterator(); i.hasNext();) {
			ServiceProfile prof = (ServiceProfile) i.next();
			indexServices(prof, new ServiceRealization(theCallee,
				prof), prof.getProcessURI());
		    }
		else if (profiles == null)
		    unindexServices(theCallee, null);
		else
		    for (Iterator i = profiles.iterator(); i.hasNext();)
			unindexServices(theCallee, ((ServiceProfile) i.next())
				.getProcessURI());
	    } else if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_COORDINATOR)) {
		if (theCoordinator == null
			&& res.getURI().startsWith(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
		    synchronized (this) {
			theCoordinator = res.getURI().substring(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
					.length());
			notifyOnFoundCoordinator();
		    }
		}
	    }
	    break;
	case MessageType.P2P_REPLY:
	    if (res instanceof ServiceResponse) {
		if (isCoordinator) {
		    Hashtable callContext = (Hashtable) allWaitingCallers
			    .remove(msg.getInReplyTo());
		    if (callContext == null) {
			// this must be UI service response, because they are
			// answered
			// immediately after the request has been handled and no
			// call context
			// is put in allWaitingCallers
			// TODO: add a log entry for checking if the above
			// assumption is true
			return;
		    }
		    Message request = (Message) callContext
			    .get(CONTEXT_REQUEST_MESSAGE);
		    Vector allCalls = (Vector) allWaitingCallers.get(request
			    .getID());
		    if (allCalls == null)
			// response already timed out => ignore this delayed one
			// TODO: add a log entry
			return;
		    synchronized (allCalls) {
			callContext.put(CONTEXT_RESPONSE_MESSAGE, res);
			int pending = ((Integer) allCalls.remove(allCalls
				.size() - 1)).intValue() - 1;
			allCalls.add(new Integer(pending));
			if (pending == 0)
			    sendServiceResponse(request);
		    }
		} else if (msg.getReceivers()[0].compareTo(theCoordinator) == 0)// if
		    // (msg.getReceivers().equals(new
		    // String[]
		    // {theCoordinator}))
		    sodapop.propagateMessage(bus, msg);
		else {
		    // this case shouldn't occur at all!
		}
	    } else if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_COORDINATOR)) {
		if (theCoordinator == null
			&& res.getURI().startsWith(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
		    synchronized (this) {
			theCoordinator = res.getURI().substring(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
					.length());
			notifyOnFoundCoordinator();
		    }
		}
	    } else if (res.getType().equals(
		    TYPE_uAAL_SERVICE_PROFILE_INFORMATION)) {
		synchronized (this) {
		    String realizationID = (String) res
			    .getProperty(PROP_uAAL_SERVICE_REALIZATION_ID);
		    List profiles = (List) res
			    .getProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE);

		    localServiceSearchResults.addProfiles(realizationID,
			    profiles);

		    notifyAll();
		}
	    }
	    break;
	case MessageType.P2P_REQUEST:
	    if (res instanceof ServiceCall) {
		ServiceRealization sr = localServicesIndex
			.getServiceRealizationByID(((ServiceCall) res)
				.getProcessURI());
		if (sr != null) {
		    ServiceCallee callee = (ServiceCallee) getBusMember(sr
			    .getProperty(
				    ServiceRealization.uAAL_SERVICE_PROVIDER)
			    .toString().substring(
				    Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
					    .length()));
		    if (callee != null)
			callee.handleRequest(msg);
		}
	    } else if (isCoordinator
		    && res.getType().equals(TYPE_uAAL_SERVICE_BUS_COORDINATOR)) {
		res = new Resource(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			+ sodapop.getID());
		res.addType(TYPE_uAAL_SERVICE_BUS_COORDINATOR, true);
		ServiceBusImpl.assessContentSerialization(res);
		sodapop.propagateMessage(bus, msg.createReply(res));
	    } else if (isCoordinator
		    && res.getType().equals(
			    TYPE_uAAL_SERVICE_PROFILE_INFORMATION)) {

		Resource r = new Resource();
		String realizationID = (String) res
			.getProperty(PROP_uAAL_SERVICE_REALIZATION_ID);
		r.addType(TYPE_uAAL_SERVICE_PROFILE_INFORMATION, true);
		r.setProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE, Arrays
			.asList(getCoordinatorServices(realizationID)));
		r.setProperty(PROP_uAAL_SERVICE_REALIZATION_ID, realizationID);

		ServiceBusImpl.assessContentSerialization(r);

		sodapop.propagateMessage(bus, msg.createReply(r));
	    }
	    break;
	case MessageType.REPLY:
	    replyToLocalCaller(msg);
	    break;
	case MessageType.REQUEST:
	    if (!(res instanceof ServiceRequest))
		return;
	    ServiceRequest request = (ServiceRequest) res;
	    if (isCoordinator) {
		if (!msg.isRemote())
		    localWaitingCallers.addLocalWaitier(msg.getID(), senderID);
		if (request.getRequestedService() instanceof InitialServiceDialog) {
		    Service s = request.getRequestedService();
		    Object csc = s
			    .getInstanceLevelFixedValueOnProp(InitialServiceDialog.PROP_CORRELATED_SERVICE_CLASS);
		    if (csc instanceof Resource) {
			Object hv = s
				.getInstanceLevelFixedValueOnProp(InitialServiceDialog.PROP_HAS_VENDOR);
			if (request
				.getURI()
				.startsWith(
					InitialServiceDialog.SERVICE_REQUEST_URI_PREFIX_INFO)) {
			    synchronized (startDialogs) {
				Vector v = (Vector) startDialogs.get(csc
					.toString());
				if (hv instanceof Resource)
				    replyToInitialDialogInfoRequest(msg, v, hv
					    .toString());
				else
				    replyToInitialDialogInfoRequest(msg, v);
			    }
			} else if (hv instanceof Resource
				&& request
					.getURI()
					.startsWith(
						InitialServiceDialog.SERVICE_REQUEST_URI_PREFIX_START)) {
			    synchronized (startDialogs) {
				callStartDialog((Vector) startDialogs.get(csc
					.toString()), hv.toString(), msg);
			    }
			} else
			    sendNoMatchingFound(msg);
		    } else
			sendNoMatchingFound(msg);
		    return;
		}
		Vector matches = new Vector();
		String serviceURI = request.getRequestedService().getClassURI();
		synchronized (allServicesIndex) {
		    Vector v = (Vector) allServicesIndex.get(serviceURI);
		    if (v == null)
			sendNoMatchingFound(msg);
		    else {
			String caller = request.getProperty(
				ServiceRequest.PROP_uAAL_SERVICE_CALLER)
				.toString();

			Long logID = Long.valueOf(Thread.currentThread()
				.getId());
			LogUtils.logTrace(ServiceBusImpl.moduleContext,
				ServiceStrategy.class, "handle", new Object[] {
					ServiceBus.LOG_MATCHING_START,
					new UnmodifiableResource(request), " ",
					logID }, null);
			for (Iterator i = v.iterator(); i.hasNext();) {
			    ServiceRealization sr = (ServiceRealization) i
				    .next();
			    Service profileService = ((ServiceProfile) sr
				    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
				    .getTheService();
			    String profileServiceURI = profileService.getURI();

			    LogUtils.logTrace(ServiceBusImpl.moduleContext,
				    ServiceStrategy.class, "handle",
				    new Object[] {
					    ServiceBus.LOG_MATCHING_PROFILE,
					    profileService.getType(),
					    profileServiceURI, logID }, null);
			    Hashtable context = matches(caller, request, sr,
				    logID);
			    if (context != null) {
				matches.add(context);
				LogUtils
					.logTrace(
						ServiceBusImpl.moduleContext,
						ServiceStrategy.class,
						"handle",
						new Object[] {
							ServiceBus.LOG_MATCHING_SUCCESS,
							logID }, null);
			    } else
				LogUtils
					.logTrace(
						ServiceBusImpl.moduleContext,
						ServiceStrategy.class,
						"handle",
						new Object[] {
							ServiceBus.LOG_MATCHING_NOSUCCESS,
							logID }, null);
			}
			LogUtils.logTrace(ServiceBusImpl.moduleContext,
				ServiceStrategy.class, "handle", new Object[] {
					ServiceBus.LOG_MATCHING_END, "found ",
					Integer.valueOf(matches.size()),
					" matches", logID }, null);

		    }
		}
		Hashtable auxMap = new Hashtable();
		for (Iterator i = matches.iterator(); i.hasNext();) {
		    Hashtable match = (Hashtable) i.next();
		    ServiceRealization sr = (ServiceRealization) match
			    .get(Constants.VAR_uAAL_SERVICE_TO_SELECT);
		    if (sr.assertServiceCall(match)) {
			Hashtable otherMatch = (Hashtable) auxMap.get(sr
				.getProvider());
			if (otherMatch == null)
			    auxMap.put(sr.getProvider(), match);
			else {
			    // uAAL_SERVICE_URI_MATCHED:
			    // New strategy: if service matches exactly URI
			    // specified in Service Request than this service is
			    // always preferred over others.
			    if (match
				    .get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) != null) {
				if (otherMatch
					.get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) == null) {
				    // the new service matches better the
				    // request
				    auxMap.put(sr.getProvider(), match);
				    continue;
				}
			    }
			    if (otherMatch
				    .get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) != null) {
				if (match
					.get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) == null) {
				    // the new service won't match better the
				    // request
				    continue;
				}
			    }
			    // If two above are not true then either both
			    // services have matched their URIs or none of them
			    // had. Either way regular strategy is applied.

			    // The strategy: from each provider accept the one
			    // with more specialization
			    // and then the one with the smallest context, which
			    // produces shorter messages

			    // TODO: is the above strategy ok? The issues are:
			    // 1. is instance-match specialization really more
			    // important than class-match specialization?
			    // 2. is the length of messages a good criteria?
			    int sp0 = Boolean.TRUE.equals(match
				    .get(CONTEXT_SPECIALIZED_CLASS_MATCH)) ? 1
				    : 0;
			    if (Boolean.TRUE.equals(match
				    .get(CONTEXT_SPECIALIZED_INSTANCE_MATCH)))
				sp0 += 2;
			    int sp1 = Boolean.TRUE.equals(otherMatch
				    .get(CONTEXT_SPECIALIZED_CLASS_MATCH)) ? 1
				    : 0;
			    if (Boolean.TRUE.equals(otherMatch
				    .get(CONTEXT_SPECIALIZED_INSTANCE_MATCH)))
				sp1 += 2;
			    if (sp1 < sp0 || otherMatch.size() > match.size())
				auxMap.put(sr.getProvider(), match);
			}
		    }
		}
		matches = new Vector(auxMap.values());
		if (matches.size() > 1 && request.acceptsRandomSelection()) {
		    // the strategy is to select the match with the lowest
		    // number of entries in 'context'
		    // => first those services are preferred that are not used
		    // or rated at all
		    // => after all have been called and rated at least once,
		    // select those with lowest
		    // number of needed input, to produce shorter messages

		    // Comment added on 6.Oct.2009:
		    // the second argument above is not always the best:
		    // case 1: setBrightness(0) vs. turnOff()
		    // case 2: getLampsByAbsLocation(loc) vs. getAllLamps()
		    // where by class restrictions all lamps are in loc
		    // and generally, isn't it better to postpone this decision
		    // to a later phase where we have gathered all responses?
		    Hashtable context = (Hashtable) matches.remove(0);
		    while (!matches.isEmpty()) {
			Hashtable aux = (Hashtable) matches.remove(0);
			if (aux.size() < context.size())
			    context = aux;
		    }
		    matches.add(context);
		}
		int size = matches.size();
		if (size == 0)
		    sendNoMatchingFound(msg);
		else {
		    if (size > 1) {
			List filters = request.getFilters();
			if (filters != null) {
			    int[] points = new int[size];
			    for (int i = 0; i < points.length; i++)
				points[i] = 0;
			    for (Iterator i = filters.iterator(); i.hasNext();) {
				AggregatingFilter af = (AggregatingFilter) i
					.next();
				List params = af.getFunctionParams();
				String[] pp = null;
				if (params != null
					&& !params.isEmpty()
					&& params.get(0) instanceof PropertyPath)
				    pp = ((PropertyPath) params.get(0))
					    .getThePath();
				if (pp == null
					|| pp.length != 2
					|| !Service.PROP_OWLS_PRESENTS
						.equals(pp[0]))
				    continue;
				switch (af.getTheFunction().ord()) {
				case AggregationFunction.ONE_OF:
				    break;
				case AggregationFunction.MIN_OF:
				    for (int j = 0; j < size; j++) {
					Object oj = getProfileParameter(
						(Hashtable) matches.get(j),
						pp[1]);
					for (int k = j + 1; k < size; k++) {
					    Object ok = getProfileParameter(
						    (Hashtable) matches.get(k),
						    pp[1]);
					    if (oj instanceof Comparable)
						if (ok == null)
						    points[k]++;
						else {
						    int l = ((Comparable) oj)
							    .compareTo(ok);
						    if (l < 0)
							points[k]++;
						    else if (l > 0)
							points[j]++;
						}
					    else {
						points[j]++;
						if (!(ok instanceof Comparable))
						    points[k]++;
					    }
					}
				    }
				    break;
				case AggregationFunction.MAX_OF:
				    for (int j = 0; j < size; j++) {
					Object oj = getProfileParameter(
						(Hashtable) matches.get(j),
						pp[1]);
					for (int k = j + 1; k < size; k++) {
					    Object ok = getProfileParameter(
						    (Hashtable) matches.get(k),
						    pp[1]);
					    if (oj instanceof Comparable)
						if (ok == null)
						    points[k]++;
						else {
						    int l = ((Comparable) oj)
							    .compareTo(ok);
						    if (l > 0)
							points[k]++;
						    else if (l < 0)
							points[j]++;
						}
					    else {
						points[j]++;
						if (!(ok instanceof Comparable))
						    points[k]++;
					    }
					}
				    }
				    break;
				case AggregationFunction.MIN_DISTANCE_TO_REF_LOC:
				    for (int j = 0; j < size; j++) {
					Object oj = getProfileParameter(
						(Hashtable) matches.get(j),
						pp[1]);
					for (int k = j + 1; k < size; k++) {
					    Object ok = getProfileParameter(
						    (Hashtable) matches.get(k),
						    pp[1]);
					    if (oj instanceof AbsLocation)
						if (ok == null)
						    points[k]++;
						else {
						    float dj = ((AbsLocation) oj)
							    .getDistanceTo((AbsLocation) params
								    .get(1));
						    float dk = ((AbsLocation) ok)
							    .getDistanceTo((AbsLocation) params
								    .get(1));
						    if (dj < dk)
							points[k]++;
						    else if (dk < dj)
							points[j]++;
						}
					    else {
						points[j]++;
						if (!(ok instanceof AbsLocation))
						    points[k]++;
					    }
					}
				    }
				    break;
				case AggregationFunction.MAX_DISTANCE_TO_REF_LOC:
				    for (int j = 0; j < size; j++) {
					Object oj = getProfileParameter(
						(Hashtable) matches.get(j),
						pp[1]);
					for (int k = j + 1; k < size; k++) {
					    Object ok = getProfileParameter(
						    (Hashtable) matches.get(k),
						    pp[1]);
					    if (oj instanceof AbsLocation)
						if (ok == null)
						    points[k]++;
						else {
						    float dj = ((AbsLocation) oj)
							    .getDistanceTo((AbsLocation) params
								    .get(1));
						    float dk = ((AbsLocation) ok)
							    .getDistanceTo((AbsLocation) params
								    .get(1));
						    if (dj > dk)
							points[k]++;
						    else if (dk > dj)
							points[j]++;
						}
					    else {
						points[j]++;
						if (!(ok instanceof AbsLocation))
						    points[k]++;
					    }
					}
				    }
				    break;
				}
			    }
			    int ind = 0, min = points[0];
			    for (int i = 1; i < size; i++)
				if (points[i] < min) {
				    ind = i;
				    min = points[i];
				}
			    for (int j = 0; j < ind; j++, size--)
				matches.remove(0);
			    while (size > 1)
				matches.remove(--size);
			}
		    }
		    callServices(msg, matches);
		}
	    } else if (msg.isRemote()) {
		// strange situation: some peer has thought i am the
		// coordinator?!!
		// => ignore!
	    } else if (isCoordinatorKnown()) {
		localWaitingCallers.addLocalWaitier(msg.getID(), senderID);
		msg.setReceivers(new String[] { theCoordinator });
		sodapop.propagateMessage(bus, msg);
	    }
	    break;
	}
    }

    /**
     * Sends a reply to the initial dialog info request message. The reply will
     * contain the matched services.
     * 
     * @param m
     *            - the initial dialog info request message
     * @param matchingServices
     */
    private void replyToInitialDialogInfoRequest(Message m,
	    Vector matchingServices) {
	if (matchingServices == null) {
	    sendNoMatchingFound(m);
	    return;
	}

	List result = new ArrayList(matchingServices.size());
	for (Iterator i = matchingServices.iterator(); i.hasNext();) {
	    ServiceRealization sr = (ServiceRealization) i.next();
	    if (sr == null)
		continue;
	    ServiceProfile sp = (ServiceProfile) sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
	    if (sp == null)
		continue;
	    Service s = sp.getTheService();
	    if (s == null)
		continue;
	    result.add(s);
	}

	if (result.isEmpty()) {
	    sendNoMatchingFound(m);
	    return;
	}

	ProcessOutput output = new ProcessOutput(
		InitialServiceDialog.OUTPUT_INSTANCE_INFO);
	output.setParameterValue(result);
	ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
	resp.addOutput(output);
	ServiceBusImpl.assessContentSerialization(resp);
	m = m.createReply(resp);
	if (sodapop.getID().equals(m.getSource()))
	    replyToLocalCaller(m);
	else
	    sodapop.propagateMessage(bus, m);
    }

    /**
     * Sends a reply to the initial dialog info request message. The reply will
     * contain a description of a matched service of the vendor whose ID is
     * passed as a parameter
     * 
     * @param m
     *            - the initial dialog info request message
     * @param matchingServices
     */
    private void replyToInitialDialogInfoRequest(Message m,
	    Vector matchingServices, String vendor) {
	if (matchingServices == null) {
	    sendNoMatchingFound(m);
	    return;
	}

	Object description = null;
	for (Iterator i = matchingServices.iterator(); i.hasNext();) {
	    ServiceRealization sr = (ServiceRealization) i.next();
	    if (sr == null)
		continue;
	    ServiceProfile sp = (ServiceProfile) sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
	    if (sp == null)
		continue;
	    Service s = sp.getTheService();
	    if (s == null)
		continue;
	    if (vendor.equals(String.valueOf(s
		    .getProperty(InitialServiceDialog.PROP_HAS_VENDOR)))) {
		description = s
			.getProperty(InitialServiceDialog.PROP_DESCRIPTION);
		if (description instanceof String)
		    break;
	    }
	}

	if (description == null) {
	    sendNoMatchingFound(m);
	    return;
	}

	ProcessOutput output = new ProcessOutput(
		InitialServiceDialog.OUTPUT_INSTANCE_INFO);
	output.setParameterValue(description);
	ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
	resp.addOutput(output);
	ServiceBusImpl.assessContentSerialization(resp);
	m = m.createReply(resp);
	if (sodapop.getID().equals(m.getSource()))
	    replyToLocalCaller(m);
	else
	    sodapop.propagateMessage(bus, m);
    }

    /**
     * Send the reply message to a local caller
     * 
     * @param msg
     *            - the reply message
     */
    private void replyToLocalCaller(Message msg) {
	String replyOf = msg.getInReplyTo();
	if (replyOf == null) {
	    // very strange! a message of type REPLY without inReplyTo
	    // => ignore!
	} else {
	    String callerID = localWaitingCallers
		    .getAndRemoveLocalWaiterCallerID(replyOf);
	    if (callerID == null) {
		// very strange! why else may I receive a reply from a peer
		// => ignore!
	    } else {
		Object caller = getBusMember(callerID);
		if (caller instanceof ServiceCaller)
		    ((ServiceCaller) caller).handleReply(msg);
		else {
		    // very strange! why else may I receive a reply from a peer
		    // => ignore!
		}
	    }
	}
    }

    /**
     * Send a no-matching-found message as a reply to the message passed as a
     * parameter
     * 
     * @param m
     *            - the message to send a reply to
     */
    private void sendNoMatchingFound(Message m) {
	String callingPeer = m.getSource();
	m = m
		.createReply(new ServiceResponse(
			CallStatus.noMatchingServiceFound));
	if (sodapop.getID().equals(callingPeer))
	    // a local caller registered to the coordinator
	    replyToLocalCaller(m);
	else
	    sodapop.propagateMessage(bus, m);
    }

    /**
     * Adds a service with ServiceProfile, ServiceRealization and processURI to
     * the index of services
     * 
     * @param prof
     * @param registration
     * @param processURI
     */
    private void indexServices(ServiceProfile prof,
	    ServiceRealization registration, String processURI) {
	Service theService = prof.getTheService();
	if (theService == null)
	    return;
	if (theService instanceof InitialServiceDialog) {
	    Object correlService = theService
		    .getProperty(InitialServiceDialog.PROP_CORRELATED_SERVICE_CLASS);
	    if (!(correlService instanceof Resource)) {
		// TODO: add a log entry
		return;
	    }
	    synchronized (startDialogs) {
		getVector(startDialogs, correlService.toString()).add(
			registration);
	    }
	} else {
	    Vector serviceURIs = getNonAbstractSuperClasses(theService);
	    synchronized (allServicesIndex) {
		for (Iterator it = serviceURIs.iterator(); it.hasNext();) {
		    String serviceURI = (String) it.next();
		    getVector(allServicesIndex, serviceURI).add(registration);
		    Vector subscribers = (Vector) allSubscriptionsIndex
			    .get(serviceURI);
		    if (subscribers != null)
			for (Iterator j = subscribers.iterator(); j.hasNext();) {
			    AvailabilitySubscription as = (AvailabilitySubscription) j
				    .next();
			    if (null != matches(as.id,
				    (ServiceRequest) as.reqOrSubs, registration))
				notifySubscriber(as, processURI, true);
			}
		}
	    }
	}
    }

    private boolean isCoordinatorKnown() {
	if (theCoordinator == null) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_COORDINATOR, true);
	    ServiceBusImpl.assessContentSerialization(r);
	    Message m = new Message(MessageType.p2p_request, r);
	    sodapop.propagateMessage(bus, m);
	    synchronized (this) {
		try {
		    waitForCoordinatorToBeKnown();
		    // TODO We need to have some kind of management here
		    // or at least a timeout (but what then? Denote
		    // itself as coordinator until find the "real" one?)!
		} catch (Exception e) {
		}
	    }
	    return theCoordinator != null;
	} else
	    return true;
    }

    /**
     * Returns true iff a ServiceRealization passed as a parameter matches the
     * ServiceRequest
     * 
     * @param callerID
     *            - the caller ID of the ServiceRequest
     * @param request
     *            - the ServiceRequest
     * @param offer
     *            - the Service Realization being matched
     * @return Hashtable - a hashtable of the context of the matching or null if
     *         the ServiceRealization does not match the ServiceRequest
     */
    private Hashtable matches(String callerID, ServiceRequest request,
	    ServiceRealization offer) {
	return matches(callerID, request, offer, null);
    }

    /**
     * Returns true iff a ServiceRealization passed as a parameter matches the
     * ServiceRequest
     * 
     * @param callerID
     *            - the caller ID of the ServiceRequest
     * @param request
     *            - the ServiceRequest
     * @param offer
     *            - the Service Realization being matched
     * @param logID
     *            - an id to be used for logging, may be null
     * @return Hashtable - a hashtable of the context of the matching or null if
     *         the ServiceRealization does not match the ServiceRequest
     */
    private Hashtable matches(String callerID, ServiceRequest request,
	    ServiceRealization offer, Long logID) {
	Hashtable context = new Hashtable();
	context.put(Constants.VAR_uAAL_ACCESSING_BUS_MEMBER, callerID);
	context.put(Constants.VAR_uAAL_CURRENT_DATETIME, TypeMapper
		.getCurrentDateTime());
	context.put(Constants.VAR_uAAL_SERVICE_TO_SELECT, offer);
	Object o = request
		.getProperty(ServiceRequest.PROP_uAAL_INVOLVED_HUMAN_USER);
	if (o != null)
	    context.put(Constants.VAR_uAAL_ACCESSING_HUMAN_USER, o);
	return offer.matches(request, context, logID) ? context : null;
    }

    /**
     * Notify the Availability Subscribers about registration/unregistration of
     * Services (ServiceRealization representing the Services)
     * 
     * @param caller
     *            - the subscriber
     * @param request
     *            - the ID of the subscription
     * @param realization
     *            - the ID of the ServiceRealization
     * @param registers
     *            - boolean, true if the notification is about a registered
     *            service, false if the notification is about an unregistered
     *            service
     */

    private void notifyLocalSubscriber(String caller, String request,
	    String realization, boolean registers) {
	Vector v = (Vector) localSubscriptionsIndex.get(caller);
	if (v != null)
	    for (Iterator i = v.iterator(); i.hasNext();) {
		AvailabilitySubscription as = (AvailabilitySubscription) i
			.next();
		if (request.equals(as.id)) {
		    if (registers)
			((AvailabilitySubscriber) as.reqOrSubs)
				.serviceRegistered(request, realization);
		    else
			((AvailabilitySubscriber) as.reqOrSubs)
				.serviceUnregistered(request, realization);
		    break;
		}
	    }
    }

    /**
     * Notify the Availability Subscriber about registration/unregistration of
     * Services (ServiceRealization representing the Services)
     * 
     * @param as
     *            - the availability subscription
     * @param realizationID
     *            - the ID of the ServiceRealization
     * @param registers
     *            - boolean, true if the notification is about a registered
     *            service, false if the notification is about an unregistered
     *            service
     */

    private void notifySubscriber(AvailabilitySubscription as,
	    String realizationID, boolean registers) {
	String peerID = Constants.extractPeerID(as.id);
	if (sodapop.getID().equals(peerID))
	    notifyLocalSubscriber(as.id, ((Resource) as.reqOrSubs).getURI(),
		    realizationID, registers);
	else {
	    Resource res = new Resource();
	    res.addType(TYPE_uAAL_SERVICE_BUS_NOTIFICATION, true);
	    res.setProperty(PROP_uAAL_REGISTERATION_STATUS,
		    (registers ? RES_STATUS_REGISTERED
			    : RES_STATUS_DEREGISTERED));
	    res.setProperty(PROP_uAAL_SERVICE_REALIZATION_ID, new Resource(
		    realizationID));
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER, new Resource(as.id));
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST, new Resource(
		    ((Resource) as.reqOrSubs).getURI()));
	    ServiceBusImpl.assessContentSerialization(res);
	    Message m = new Message(MessageType.event, res);
	    m.setReceivers(new String[] { peerID });
	    sodapop.propagateMessage(bus, m);
	}
    }

    protected void waitForCoordinatorToBeKnown() throws InterruptedException {
	wait();
    }

    protected void notifyOnFoundCoordinator() {
	notifyAll();
    }

    /**
     * Remove availability subscriber passed as a parameter
     * 
     * @param callerID
     *            - the subscribing caller ID
     * @param subscriber
     *            - the subscriber object
     * @param requestURI
     *            - the URI of the request to subscribe
     */

    void removeAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, String requestURI) {
	if (requestURI == null || subscriber == null
		|| localSubscriptionsIndex.get(requestURI) == null)
	    // TODO - check if the above line should be:
	    // localSubscriptionsIndex.get(callerID)...
	    return;

	Vector v = (Vector) localSubscriptionsIndex.get(callerID);
	if (v != null)
	    for (Iterator i = v.iterator(); i.hasNext();) {
		AvailabilitySubscription as = (AvailabilitySubscription) i
			.next();
		if (requestURI.equals(as.id) && subscriber == as.reqOrSubs) {
		    i.remove();
		    break;
		}
	    }

	String serviceURI = (String) localSubscriptionsIndex.remove(requestURI);
	if (isCoordinator) {
	    v = (Vector) allSubscriptionsIndex.get(serviceURI);
	    if (v != null)
		for (Iterator i = v.iterator(); i.hasNext();) {
		    AvailabilitySubscription as = (AvailabilitySubscription) i
			    .next();
		    if (callerID.equals(as.id)
			    && ((Resource) as.reqOrSubs).getURI().equals(
				    requestURI)) {
			i.remove();
			break;
		    }
		}
	} else if (isCoordinatorKnown()) {
	    Resource res = new Resource(callerID);
	    res.addType(TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION, true);
	    res.setProperty(PROP_uAAL_SERVICE_TYPE, new Resource(serviceURI));
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST, new Resource(
		    requestURI));
	    res.setProperty(PROP_uAAL_REGISTERATION_STATUS,
		    RES_STATUS_DEREGISTERED);
	    ServiceBusImpl.assessContentSerialization(res);
	    Message m = new Message(MessageType.p2p_event, res);
	    m.setReceivers(new String[] { theCoordinator });
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * Remove service profiles to a previously registered ServiceCallee
     * 
     * @param calleeID
     *            - the URI of the ServiceCallee
     * @param realizedServices
     *            - the service profiles to remove
     */

    void removeMatchingRegParams(String calleeID,
	    ServiceProfile[] realizedServices) {
	if (realizedServices == null
		|| calleeID == null
		|| !(getBusMember(calleeID
			.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				.length())) instanceof ServiceCallee))
	    return;

	for (int i = 0; i < realizedServices.length; i++) {
	    if (realizedServices[i] == null)
		continue;

	    String processURI = realizedServices[i].getProcessURI();
	    if (processURI == null)
		continue;

	    ServiceRealization reg = localServicesIndex
		    .removeServiceRealization(processURI);
	    if (!calleeID.equals(reg
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER))
		    || !processURI
			    .equals(((ServiceProfile) reg
				    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
				    .getProcessURI())) {
		localServicesIndex.addServiceRealization(processURI, reg);
	    }

	    if (isCoordinator)
		unindexServices(calleeID, processURI);
	}

	if (!isCoordinator && isCoordinatorKnown()) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_REGISTRATION, true);
	    r.setProperty(PROP_uAAL_REGISTERATION_STATUS,
		    RES_STATUS_DEREGISTERED);
	    r.setProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE, Arrays
		    .asList(realizedServices));
	    r
		    .setProperty(PROP_uAAL_SERVICE_PROVIDED_BY, new Resource(
			    calleeID));
	    ServiceBusImpl.assessContentSerialization(r);
	    Message m = new Message(MessageType.p2p_event, r);
	    m.setReceivers(new String[] { theCoordinator });
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * Remove registration parameters for a calleID passed as a parameter
     * 
     * @param calleeID
     *            - the URI of the callee for which the registration parameters
     *            are removed
     */
    void removeRegParams(String calleeID) {
	if (calleeID == null
		|| !(getBusMember(calleeID
			.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				.length())) instanceof ServiceCallee))
	    return;

	String[] serviceRealizationsIds = localServicesIndex
		.getServiceRealizationIds();
	for (int i = 0; i < serviceRealizationsIds.length; i++) {
	    String id = serviceRealizationsIds[i];
	    ServiceRealization serviceRealization = localServicesIndex
		    .getServiceRealizationByID(id);
	    if (calleeID.equals(serviceRealization
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER))) {
		localServicesIndex.removeServiceRealization(id);
	    }
	}

	if (isCoordinator)
	    unindexServices(calleeID, null);
	else if (isCoordinatorKnown()) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_REGISTRATION, true);
	    r.setProperty(PROP_uAAL_REGISTERATION_STATUS,
		    RES_STATUS_DEREGISTERED);
	    r
		    .setProperty(PROP_uAAL_SERVICE_PROVIDED_BY, new Resource(
			    calleeID));
	    ServiceBusImpl.assessContentSerialization(r);
	    Message m = new Message(MessageType.p2p_event, r);
	    m.setReceivers(new String[] { theCoordinator });
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * Remove services of the callee passed as a parameter from the index of
     * services
     * 
     * @param calleeID
     *            - the URI of the caller
     * @param processURI
     *            - the URI of the process
     */

    private void unindexServices(String calleeID, String processURI) {

	boolean deleteAll = (processURI == null);
	synchronized (allServicesIndex) {
	    for (Iterator i = allServicesIndex.values().iterator(); i.hasNext();) {
		for (Iterator j = ((Vector) i.next()).iterator(); j.hasNext();) {
		    ServiceRealization reg = (ServiceRealization) j.next();
		    if (calleeID
			    .equals(reg
				    .getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER))) {
			if (deleteAll)
			    processURI = ((ServiceProfile) reg
				    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
				    .getProcessURI();
			else if (!processURI
				.equals(((ServiceProfile) reg
					.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
					.getProcessURI()))
			    continue;

			j.remove();
			String serviceURI = ((ServiceProfile) reg
				.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
				.getTheService().getClassURI();
			Vector subscribers = (Vector) allSubscriptionsIndex
				.get(serviceURI);
			if (subscribers != null)
			    for (Iterator k = subscribers.iterator(); k
				    .hasNext();) {
				AvailabilitySubscription as = (AvailabilitySubscription) k
					.next();
				if (null != matches(as.id,
					(ServiceRequest) as.reqOrSubs, reg))
				    notifySubscriber(as, processURI, false);
			    }
		    }
		}
	    }
	}
    }

    /**
     * This method returns all the globally registered Service Profiles for the
     * given service URI
     * 
     * @param serviceURI
     *            - the URI of the Service whose profiles are returned
     * @return ServiceProfile[] - the service profiles of the given service
     */
    public ServiceProfile[] getAllServiceProfiles(String serviceURI) {
	if (this.isCoordinator)
	    return getCoordinatorServices(serviceURI);

	Resource r = new Resource();
	r.addType(TYPE_uAAL_SERVICE_PROFILE_INFORMATION, true);
	r.setProperty(PROP_uAAL_SERVICE_REALIZATION_ID, serviceURI);
	ServiceBusImpl.assessContentSerialization(r);
	Message m = new Message(MessageType.p2p_request, r);
	m.setReceivers(new String[] { theCoordinator });
	sodapop.propagateMessage(bus, m);

	int msTimeout = 1000;
	int maxRetry = 5;
	int retryCount = 0;

	synchronized (this) {
	    while (!this.localServiceSearchResults.exist(serviceURI)
		    && maxRetry > retryCount) {
		try {
		    wait(msTimeout);
		    retryCount++;
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}

	List profiles = (List) this.localServiceSearchResults
		.getProfiles(serviceURI);
	return profileListToArray(profiles);
    }

    /**
     * Return the profiles registered for the service passed as a parameter,
     * only if this peer is a coordinator. Otherwise, an empty list is returned.
     * 
     * @param serviceURI
     *            - the URI of the service whose profiles are returned
     * @return - the profiles of the service passed as a parameter
     */
    private ServiceProfile[] getCoordinatorServices(String serviceURI) {

	ArrayList profiles = new ArrayList();

	if (this.isCoordinator) {
	    Vector neededProfiles = (Vector) this.allServicesIndex
		    .get(serviceURI);
	    if (neededProfiles != null)
		for (Iterator j = neededProfiles.iterator(); j.hasNext();) {
		    ServiceRealization reg = (ServiceRealization) j.next();
		    ServiceProfile profile = (ServiceProfile) reg
			    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
		    if (profile != null)
			profiles.add(profile);
		}
	}

	return profileListToArray(profiles);
    }

    /**
     * This method translates a List of ServiceProfiles into an array of
     * ServiceProfiles
     * 
     * @param list
     *            - the list to translate
     * @return ServiceProfile[] - the translated array of ServiceProfiles
     */
    private ServiceProfile[] profileListToArray(List list) {
	if (list == null)
	    return new ServiceProfile[0];
	ServiceProfile[] result = new ServiceProfile[list.size()];
	for (int i = 0; i < result.length; i++)
	    result[i] = (ServiceProfile) list.get(i);

	return result;
    }
}
