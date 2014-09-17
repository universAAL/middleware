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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.OntClassInfo;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.UnmodifiableResource;
import org.universAAL.middleware.service.AggregatingFilter;
import org.universAAL.middleware.service.AggregationFunction;
import org.universAAL.middleware.service.AvailabilitySubscriber;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ProfileExistsException;
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
import org.universAAL.middleware.util.Constants;

/**
 * This class implements the BusStrategy for the ServiceBus
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public class ServiceStrategy extends BusStrategy {
    private static final String PROP_uAAL_REGISTRATION_STATUS = Resource.uAAL_VOCABULARY_NAMESPACE
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
    private static final String CONTEXT_INJECT_CALLER = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "injectCaller";

    private class AvailabilitySubscription {
	String id;
	String callerID;
	String serviceClassURI;
	Object reqOrSubs;
    }

    private class WaitingRequest {
	Vector<HashMap<String, Object>> matches;
	int pendingCalls;
    }

    /**
     * The set of all service realizations for a service URI. It maps the URI of
     * a service (subclasses of {@link Service}) to a list of
     * {@link ServiceRealization}s that stores the service profile together with
     * some metadata like the callee and quality-of-service parameters.
     */
    private HashMap<String, ArrayList<ServiceRealization>> allServicesIndex;

    // serviceClassURI -> Vector(AvailabilitySubscription)
    private HashMap<String, ArrayList<AvailabilitySubscription>> allSubscriptionsIndex;

    /**
     * The set of all waiting requests. When a request is received by the bus,
     * the matching callees are found out and the call context of each match is
     * stored in this map before a call is sent to each of the callees. When the
     * responses from all callees are received, the data in this map can be used
     * to send an aggregated reponse back to the caller.
     * 
     * It makes use of {@link #allWaitingCalls} which stores information about a
     * single call.
     * 
     * It maps the ID of the bus message of the request to a set of call
     * contexts (one entry for each matching callee).
     */
    private Hashtable<String, WaitingRequest> allWaitingRequests;

    /**
     * The set of all waiting calls. When the matching callees for a request
     * have been found out, a call is sent to each callee. This map stores the
     * call context for each call. When a response is received, the mapping
     * between URIs in request and response can be done.
     * 
     * It maps the ID of the bus message of the call to the call context for
     * this callee.
     */
    private Hashtable<String, HashMap<String, Object>> allWaitingCalls;

    // callerURI -> Vector(AvailabilitySubscription)
    private Hashtable<String, Vector<AvailabilitySubscription>> localSubscriptionsIndex;

    // serviceURI -> ArrayList(ServiceRealization)
    private HashMap<String, ArrayList<ServiceRealization>> startDialogs;

    // request.msgID -> callerID
    protected ILocalWaitingCallersData localWaitingCallers;
    // processURI -> ServiceRealization
    protected ILocalServicesIndexData localServicesIndex;
    // serviceURI -> List(ServiceRealization) (was replaced with the new
    // mechanism)
    protected ILocalServiceSearchResultsData localServiceSearchResults;

    private boolean isCoordinator;
    protected PeerCard theCoordinator = null;

    public ServiceStrategy(CommunicationModule commModule) {
	super(commModule, "Service Bus Strategy");

	// Initiated the factory
	IServiceStrategyDataFactory factory = createServiceStrategyDataFactory();

	// dummy action to force the load of the class InitialServiceDialog
	StringUtils.isNonEmpty(InitialServiceDialog.MY_URI);

	// end of dummy action: we had to set the coordinator ID back to null
	// until the real ID is found out
	localSubscriptionsIndex = new Hashtable<String, Vector<AvailabilitySubscription>>();
	localServicesIndex = factory.createLocalServicesIndexData();
	localWaitingCallers = factory.createLocalWaitingCallersData();
	localServiceSearchResults = factory
		.createLocalServiceSearchResultsData();
	isCoordinator = Constants.isCoordinatorInstance();
	LogUtils.logDebug(ServiceBusImpl.getModuleContext(),
		ServiceStrategy.class, "ServiceStrategy", new Object[] {
			"This instance is ", isCoordinator ? "" : "NOT ",
			"the coordinator." }, null);
	allWaitingCalls = new Hashtable<String, HashMap<String, Object>>();
	if (isCoordinator) {
	    allServicesIndex = new HashMap<String, ArrayList<ServiceRealization>>();
	    allSubscriptionsIndex = new HashMap<String, ArrayList<AvailabilitySubscription>>();
	    startDialogs = new HashMap<String, ArrayList<ServiceRealization>>();
	    allWaitingRequests = new Hashtable<String, WaitingRequest>();
	}
    }

    protected IServiceStrategyDataFactory createServiceStrategyDataFactory() {
	return new ServiceStrategyDataFactory();
    }

    /**
     * Adds availability subscription (registration and un-registration of
     * services), according to the ServiceRequest
     * 
     * @param callerID
     *            the ID of the caller who asked to make the subscription
     * @param subscriber
     *            the object to be notified about registration event
     * @param request
     *            the service request to match the the service profiles. The
     *            notifications will be fired only regarding the
     *            registration/unregistration of services with the matching
     *            service profiles.
     */
    void addAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, ServiceRequest request) {
	if (request == null || subscriber == null || request.isAnon())
	    return;

	AvailabilitySubscription as = new AvailabilitySubscription();
	as.id = request.getURI();
	as.callerID = callerID;
	as.serviceClassURI = request.getRequestedService().getType();
	as.reqOrSubs = subscriber;
	getVector(localSubscriptionsIndex, callerID).add(as);

	if (isCoordinator)
	    addSubscriber(callerID, request);
	else if (isCoordinatorKnown()) {
	    Resource res = new Resource(callerID);
	    res.addType(TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION, true);
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST, request);
	    res.setProperty(PROP_uAAL_REGISTRATION_STATUS,
		    RES_STATUS_REGISTERED);
	    ((ServiceBusImpl) bus).assessContentSerialization(res);
	    BusMessage m = new BusMessage(MessageType.p2p_event, res, bus);
	    m.setReceiver(theCoordinator);
	    send(m);
	}
    }

    /**
     * Add service profiles to a previously registered ServiceCallee
     * 
     * @param calleeID
     *            The id of the ServiceCallee
     * @param realizedServices
     *            The profiles to add
     * @param throwOnDuplicateReg
     *            Specifies whether this method should throw an exception or
     *            just ignore it when a profile is registered with a process URI
     *            that is already registered.
     * @throws ProfileExistsException
     *             if one of the profiles exists already. In that case, none of
     *             the profiles will be registered.
     */
    void addRegParams(String calleeID, ServiceProfile[] realizedServices,
	    boolean throwOnDuplicateReg) {
	if (realizedServices == null || calleeID == null
		|| !(getBusMember(calleeID) instanceof ServiceCallee))
	    return;

	class Data {
	    String processURI;
	    ServiceRealization registration;
	    ServiceProfile profile;
	}

	ArrayList<Data> tmp = new ArrayList<Data>();
	synchronized (localServicesIndex) {
	    // we first check that all services can be added, i.e. that there is
	    // no service already added with the same process URI
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

		if (localServicesIndex.getServiceRealizationByID(processURI) != null) {
		    // this process URI is already available!
		    if (throwOnDuplicateReg) {
			throw new ProfileExistsException(realizedServices[i], i);
		    } else {
			// just ignore this profile, the profile is not
			// registered for this callee
			continue;
		    }
		}

		// qualifications fulfilled -> associate service with its
		// provider
		ServiceRealization registration = new ServiceRealization(
			calleeID, realizedServices[i]);

		// store in tmp
		Data dat = new Data();
		dat.processURI = processURI;
		dat.profile = realizedServices[i];
		dat.registration = registration;
		tmp.add(dat);
	    }

	    // now store the registrations
	    for (Data dat : tmp) {
		// index it over the ID of the operation registered
		localServicesIndex.addServiceRealization(dat.processURI,
			dat.registration);

		if (isCoordinator)
		    // more complex indexing of services by the coordinator
		    indexServices(dat.profile, dat.registration, dat.processURI);
	    }
	}

	if (!isCoordinator && isCoordinatorKnown()) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_REGISTRATION, true);
	    r.setProperty(PROP_uAAL_REGISTRATION_STATUS, RES_STATUS_REGISTERED);
	    r.setProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE,
		    Arrays.asList(realizedServices));
	    r.setProperty(PROP_uAAL_SERVICE_PROVIDED_BY, new Resource(calleeID));
	    ((ServiceBusImpl) bus).assessContentSerialization(r);
	    BusMessage m = new BusMessage(MessageType.p2p_event, r, bus);
	    m.setReceiver(theCoordinator);
	    send(m);
	} else if (theCoordinator == null) {
	    // using the dummy value "this" to indicate that the coordinator has
	    // at least one registration
	    theCoordinator = bus.getPeerCard();
	    // publish an event informing all peers started prior to the
	    // coordinator
	    // about the availability of the coordinator
	    // do this in a thread after waiting 10 seconds to make sure that
	    // the join process within the sodapop engine is closed
	    new Thread("Service Bus Strategy - Waiting for Bus Coordinator") {
		public void run() {
		    try {
			sleep(10000);
			Resource res = new Resource(bus.getURI());
			res.addType(TYPE_uAAL_SERVICE_BUS_COORDINATOR, true);
			((ServiceBusImpl) bus).assessContentSerialization(res);
			send(new BusMessage(MessageType.p2p_event, res, bus));
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
     *            the id of the subscriber
     * @param request
     *            the request to describe the desired services
     */
    private void addSubscriber(String callerID, ServiceRequest request) {
	String serviceURI = request.getRequestedService().getType();
	synchronized (allServicesIndex) {
	    AvailabilitySubscription as = new AvailabilitySubscription();
	    as.id = callerID;
	    as.reqOrSubs = request;
	    getList(allSubscriptionsIndex, serviceURI).add(as);
	    ArrayList<ServiceRealization> realizations = allServicesIndex
		    .get(serviceURI);
	    if (realizations != null)
		for (ServiceRealization sr : realizations) {
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
     * Pass the service call message to all matching service callees.
     * 
     * @param m
     *            the message.
     * @param matches
     *            a list of maps that describe the context of the matched
     *            services.
     */
    private void callServices(BusMessage m,
	    Vector<HashMap<String, Object>> matches) {
	int size = matches.size();

	WaitingRequest wr = new WaitingRequest();
	wr.matches = matches;
	wr.pendingCalls = size;

	allWaitingRequests.put(m.getID(), wr);
	int maxTimeout = 0;
	for (int i = 0; i < size; i++) {
	    HashMap<String, Object> match = matches.get(i);
	    match.put(CONTEXT_REQUEST_MESSAGE, m);
	    ServiceRealization sr = (ServiceRealization) match
		    .get(Constants.VAR_uAAL_SERVICE_TO_SELECT);
	    Object timeout = ((ServiceProfile) sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
		    .getProperty(ServiceProfile.PROP_uAAL_RESPONSE_TIMEOUT);
	    if (timeout instanceof Integer
		    && ((Integer) timeout).intValue() > maxTimeout)
		maxTimeout = ((Integer) timeout).intValue();
	    PeerCard receiver = AbstractBus.getPeerFromBusResourceURI(sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER)
		    .toString());
	    ServiceCall sc = (ServiceCall) match
		    .remove(ServiceRealization.uAAL_ASSERTED_SERVICE_CALL);
	    ((ServiceBusImpl) bus).assessContentSerialization(sc);
	    BusMessage call = new BusMessage(MessageType.p2p_request, sc, bus);

	    callService(call, receiver, match);
	}
	if (maxTimeout > 0) {
	    try {
		Thread.sleep(maxTimeout);
	    } catch (Exception e) {
	    }
	    sendServiceResponse(m);
	}
    }

    public void injectCall(String callerID, BusMessage call, PeerCard receiver) {
	localWaitingCallers.addLocalWaitier(call.getID(), callerID);

	HashMap<String, Object> match = new HashMap<String, Object>();
	match.put(CONTEXT_INJECT_CALLER, callerID);
	callService(call, receiver, match);
    }

    /**
     * Call a specific service.
     * 
     * @param call
     *            the bus message that contains a {@link ServiceCall}
     * @param receiver
     *            the receiving peer
     * @param match
     *            the call context
     */
    private void callService(BusMessage call, PeerCard receiver,
	    HashMap<String, Object> match) {
	boolean handleLocally = true;
	try {
	    handleLocally = call.getSender().getPeerID()
		    .equals(receiver.getPeerID());
	} catch (NullPointerException e) {
	    // find out which element is null and log
	    if (call.getSender() == null) {
		LogUtils.logError(
			ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class,
			"callServices",
			new Object[] { "Call.getSender() is null - ignoring." },
			null);
	    } else if (call.getSender().getPeerID() == null) {
		LogUtils.logError(
			ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class,
			"callServices",
			new Object[] { "Call.getSender().getPeerID() is null - ignoring." },
			null);
	    }

	    if (receiver == null) {
		LogUtils.logError(ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class, "callServices",
			new Object[] { "Receiver is null - ignoring." }, null);
	    } else if (receiver.getPeerID() == null) {
		LogUtils.logError(
			ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class,
			"callServices",
			new Object[] { "Receiver.getPeerID() is null - ignoring." },
			null);
	    }

	    // don't handle
	    return;
	}

	allWaitingCalls.put(call.getID(), match);

	if (handleLocally)
	    handleMessage(call, null);
	else {
	    call.setReceiver(receiver);
	    send(call);
	}
    }

    /**
     * This method starts a general purpose user interaction related to a
     * certain service class. The decision about a concrete goal to reach (if at
     * all) will be taken later during exploring the user interface
     * 
     * @param matchingServices
     *            the currently matching services for the general purpose user
     *            interaction request
     * @param vendor
     *            the vendor who provides the currently matching services
     * @param m
     *            the message request for general purpose user interaction
     */
    private void callStartDialog(
	    ArrayList<ServiceRealization> matchingServices, String vendor,
	    BusMessage m) {
	if (matchingServices == null) {
	    sendNoMatchingFound(m);
	    return;
	}

	Object calleeID = null;
	Object processURI = null;
	for (ServiceRealization sr : matchingServices) {
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
		    ServiceCall sc = new ServiceCall(new Resource(
			    (String) processURI));
		    if (user instanceof Resource)
			sc.setInvolvedUser((Resource) user);
		    ((ServiceBusImpl) bus).assessContentSerialization(sc);
		    BusMessage call = new BusMessage(MessageType.p2p_request,
			    sc, bus);
		    PeerCard receiver = AbstractBus
			    .getPeerFromBusResourceURI((String) calleeID);
		    call.setReceiver(receiver);
		    if (call.receiverResidesOnDifferentPeer()) {
			send(call);
		    } else {
			handleMessage(call, null);
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
	    if (m.receiverResidesOnDifferentPeer())
		send(m);
	    else
		replyToLocalCaller(m);
	}
    }

    /**
     * Sends a response to the message passed as a parameter
     * 
     * @param m
     *            the message, to which the response is sent
     */
    private void sendServiceResponse(BusMessage m) {
	WaitingRequest wr = allWaitingRequests.remove(m.getID());
	Vector<HashMap<String, Object>> matches = wr.matches;
	if (matches == null)
	    return;

	synchronized (matches) {
	    int size = matches.size();
	    int numTimedOut = wr.pendingCalls;
	    if (size == numTimedOut)
		// there has been no one response => this method is called
		// because of timeout!
		m = m.createReply(new ServiceResponse(
			CallStatus.responseTimedOut));
	    else {
		// boolean arrays to indicate which of the responses had which
		// kind of failure:
		// - nmsf for NO_MATCHING_SERVICE_FOUND
		// - rto for RESPONSE_TIMED_OUT
		// - ssf for SERVICE_SPECIFIC_FAILURE
		boolean[] nmsf = new boolean[size], rto = new boolean[size], ssf = new boolean[size];
		// responses with CallStatus.SUCCEEDED
		ArrayList<HashMap<String, Object>> goods = new ArrayList<HashMap<String, Object>>(
			size);
		int bads = 0; // the total number of responses with failure
		for (int i = 0; i < size; i++) {
		    HashMap<String, Object> match = matches.get(i);
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
			HashMap<String, Object> bad = null;
			// if there is one response with
			// SERVICE_SPECIFIC_FAILURE take that one
			for (int i = 0; i < size; i++) {
			    if (ssf[i]) {
				bad = matches.get(i);
				break;
			    } else if (rto[i])
				bad = matches.get(i);
			    else if (bad == null)
				bad = matches.get(i);
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
		    HashMap<String, Object> match = goods.get(0);
		    ServiceResponse sr = (ServiceResponse) match
			    .get(CONTEXT_RESPONSE_MESSAGE);
		    prepareRequestedOutput(sr.getOutputs(), match);
		    m = m.createReply(sr);
		    break;
		default:
		    size = goods.size();
		    List<AggregatingFilter> aggregations = ((ServiceRequest) m
			    .getContent()).getOutputAggregations();
		    if (!aggregations.isEmpty()) {
			int[] points = new int[size];
			for (int i = 0; i < points.length; i++)
			    points[i] = 0;
			for (Iterator<AggregatingFilter> i = aggregations
				.iterator(); i.hasNext();) {
			    AggregatingFilter af = (AggregatingFilter) i.next();
			    List<?> params = af.getFunctionParams();
			    switch (af.getTheFunction().ord()) {
			    case AggregationFunction.ONE_OF:
				break;
			    case AggregationFunction.MIN_OF:
				for (int j = 0; j < size; j++) {
				    Object oj = getOutputValue(goods.get(j), af);
				    for (int k = j + 1; k < size; k++) {
					Object ok = getOutputValue(
						goods.get(k), af);
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
				    Object oj = getOutputValue(goods.get(j), af);
				    for (int k = j + 1; k < size; k++) {
					Object ok = getOutputValue(
						goods.get(k), af);
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
				    Object oj = getOutputValue(goods.get(j), af);
				    for (int k = j + 1; k < size; k++) {
					Object ok = getOutputValue(
						goods.get(k), af);
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
				    Object oj = getOutputValue(goods.get(j), af);
				    for (int k = j + 1; k < size; k++) {
					Object ok = getOutputValue(
						goods.get(k), af);
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
			HashMap<String, Object> ctxt = goods.get(0);
			ServiceResponse sresp = (ServiceResponse) ctxt
				.get(CONTEXT_RESPONSE_MESSAGE);
			prepareRequestedOutput(sresp.getOutputs(), ctxt);
			m = m.createReply(sresp);
		    } else {
			// combine all the good responses:
			// get all outputs of all responses, call
			// 'prepareRequestedOutput' for a mapping of URIs of the
			// caller, then rewrite a response and set
			// combined outputs

			// the outputs: maps URI of ProcessOutput to a list of
			// ProcessOutputs
			HashMap<String, ArrayList<ProcessOutput>> outputs = new HashMap<String, ArrayList<ProcessOutput>>();
			HashMap<?, ?> ctxt = null;
			ServiceResponse resp = null;

			// 1. get all outputs of all responses
			for (Object o : goods) {
			    ctxt = (HashMap<?, ?>) o;
			    resp = (ServiceResponse) ctxt
				    .get(CONTEXT_RESPONSE_MESSAGE);
			    List<ProcessOutput> lstResp = resp.getOutputs();
			    prepareRequestedOutput(lstResp, ctxt);
			    for (ProcessOutput i : lstResp) {
				ArrayList<ProcessOutput> tmp = outputs.get(i
					.getURI());
				if (tmp == null) {
				    tmp = new ArrayList<ProcessOutput>();
				    outputs.put(i.getURI(), tmp);
				}
				tmp.add(i);
			    }
			}

			// resp is a valid response and we will rewrite its
			// outputs
			List<ProcessOutput> lstResp = resp.getOutputs();
			lstResp.clear();

			// 2. rewrite
			for (ArrayList<ProcessOutput> lst : outputs.values()) {
			    // combine all the values into one list
			    ArrayList<Object> l = new ArrayList<Object>();
			    for (ProcessOutput po : lst) {
				Object val = po.getParameterValue();
				if (val instanceof List) {
				    // the service has responded with a list of
				    // objects for this parameter
				    l.addAll((List<?>) val);
				} else {
				    // the service has responded with a single
				    // object for this parameter
				    l.add(val);
				}
			    }
			    // create a new ProcessOutput with the calculated
			    // list l
			    ProcessOutput po = lst.get(0);
			    po = new ProcessOutput(po.getURI());
			    po.setParameterValue(l);
			    lstResp.add(po);
			}

			// 3. prepare combined response for sending
			m = m.createReply(resp);
		    }
		    break;
		}
	    }
	}

	((ServiceBusImpl) bus).assessContentSerialization((Resource) m
		.getContent());
	if (m.receiverResidesOnDifferentPeer()) {
	    send(m);
	} else {
	    // a local caller registered to the coordinator
	    replyToLocalCaller(m);
	}
    }

    /**
     * Translates the process outputs according to the bindings
     * 
     * @param outputs
     *            a list of ProcessOutputs
     * @param context
     *            HashMap of bindings for the ProcessOutputs
     */
    private void prepareRequestedOutput(List<ProcessOutput> outputs,
	    HashMap<?, ?> context) {
	if (outputs != null && !outputs.isEmpty())
	    for (int i = outputs.size() - 1; i > -1; i--) {
		ProcessOutput po = outputs.remove(i);
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
     * parameter including the given service.
     * 
     * @param s
     *            the service.
     * @return a list with the class URIs of all non-abstract super classes.
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    private List<String> getNonAbstractSuperClasses(Service s) {
	List<String> lst = new ArrayList<String>();
	String classURI = s.getClassURI();

	// add the service class itself
	OntClassInfo info = OntologyManagement.getInstance().getOntClassInfo(
		classURI);
	if (info != null) {
	    if (OntologyManagement.getInstance().isRegisteredClass(
		    s.getClassURI(), false))
		lst.add(classURI);
	}

	// get named super classes and add them to the list
	String[] res = ManagedIndividual.getNonabstractSuperClasses(classURI);
	for (int i = 0; i < res.length; i++)
	    lst.add(res[i]);

	return lst;
    }

    /**
     * Extract the output value from the context, according to the
     * AggregatingFilter passed as a parameter
     * 
     * @param context
     *            the context
     * @param af
     *            the aggregating filter
     * @return the output
     */
    private Object getOutputValue(Map<String, Object> context,
	    AggregatingFilter af) {
	List<ProcessOutput> outputs = ((ServiceResponse) context
		.get(CONTEXT_RESPONSE_MESSAGE)).getOutputs();
	if (outputs == null || outputs.isEmpty())
	    return null;

	for (String key : context.keySet()) {
	    Object o = context.get(key);
	    if (o instanceof Resource) {
		o = ((Resource) o)
			.getProperty(OutputBinding.PROP_OWLS_BINDING_VALUE_FUNCTION);
		if (o instanceof AggregatingFilter
			&& ((AggregatingFilter) o).getTheFunction() == af
				.getTheFunction()
			&& af.getFunctionParams().equals(
				((AggregatingFilter) o).getFunctionParams()))
		    for (ProcessOutput po : outputs) {
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
     *            the property of the profile paramter to return
     * @return the profile parameter
     */
    private Object getProfileParameter(HashMap<String, Object> context,
	    String prop) {
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
     * @param table
     *            the hashtable
     * @param key
     *            the key
     * @return the value of the key from the hashtable
     */
    private <V> Vector<V> getVector(Hashtable<String, Vector<V>> table,
	    String key) {
	Vector<V> m = table.get(key);
	if (m == null) {
	    m = new Vector<V>();
	    table.put(key, m);
	}
	return m;
    }

    /**
     * Returns a list that is stored as value in a {@link Map}. If the list does
     * not exist for the given key, the list is created and added to the map.
     * 
     * @param map
     *            the map that should contain the list under the given key.
     * @param key
     *            the key for which the list is stored in the map.
     * @return the non-null list.
     */
    private <V> ArrayList<V> getList(Map<String, ArrayList<V>> map, String key) {
	ArrayList<V> val = map.get(key);
	if (val == null) {
	    val = new ArrayList<V>();
	    map.put(key, val);
	}
	return val;
    }

    private void logTrace(String methodName, Object[] obj) {
	try {
	    Long id = (Long) obj[obj.length - 1];
	    if (id != null)
		LogUtils.logTrace(ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class, methodName, obj, null);
	} catch (Exception e) {
	    LogUtils.logDebug(ServiceBusImpl.getModuleContext(),
		    ServiceStrategy.class, "logTrace",
		    new Object[] { "Exception in logging" }, e);
	}
    }

    /**
     * @see BusStrategy #handle(BusMessage, String)
     */
    public void handle(BusMessage msg, String senderID) {
	Resource res = (Resource) msg.getContent();
	switch (msg.getType().ord()) {
	case MessageType.EVENT:
	    if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_NOTIFICATION))
		notifyLocalSubscriber(
			res.getProperty(PROP_uAAL_SERVICE_SUBSCRIBER)
				.toString(),
			res.getProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST)
				.toString(),
			res.getProperty(PROP_uAAL_SERVICE_REALIZATION_ID)
				.toString(), RES_STATUS_REGISTERED.equals(res
				.getProperty(PROP_uAAL_REGISTRATION_STATUS)));
	    break;
	case MessageType.P2P_EVENT:
	    if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION)
		    && isCoordinator) {
		if (RES_STATUS_DEREGISTERED.equals(res
			.getProperty(PROP_uAAL_REGISTRATION_STATUS))) {
		    String serviceURI = res.getProperty(PROP_uAAL_SERVICE_TYPE)
			    .toString(), subscriber = res.getURI(), requestURI = res
			    .getProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST)
			    .toString();
		    synchronized (allServicesIndex) {
			ArrayList<AvailabilitySubscription> arrAS = allSubscriptionsIndex
				.get(serviceURI);
			if (arrAS != null) {
			    for (Iterator<AvailabilitySubscription> i = arrAS
				    .iterator(); i.hasNext();) {
				AvailabilitySubscription as = i.next();
				if (as.id.equals(subscriber)
					&& as.reqOrSubs.toString().equals(
						requestURI)) {
				    i.remove();
				    return;
				}
			    }
			}
		    }
		} else
		    addSubscriber(
			    res.getURI(),
			    (ServiceRequest) res
				    .getProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST));
	    } else if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_REGISTRATION)
		    && isCoordinator) {
		List<?> profiles = (List<?>) res
			.getProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE);
		String theCallee = res.getProperty(
			PROP_uAAL_SERVICE_PROVIDED_BY).toString();
		if (RES_STATUS_REGISTERED.equals(res
			.getProperty(PROP_uAAL_REGISTRATION_STATUS))) {
		    if (profiles != null) {
			for (Iterator<?> i = profiles.iterator(); i.hasNext();) {
			    ServiceProfile prof = (ServiceProfile) i.next();
			    indexServices(prof, new ServiceRealization(
				    theCallee, prof), prof.getProcessURI());
			}
		    }
		} else if (profiles == null) {
		    unindexServices(theCallee, null);
		} else {
		    for (Iterator<?> i = profiles.iterator(); i.hasNext();)
			unindexServices(theCallee,
				((ServiceProfile) i.next()).getProcessURI());
		}
	    } else if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_COORDINATOR)) {
		PeerCard coord = AbstractBus.getPeerFromBusResourceURI(res
			.getURI());
		if (theCoordinator == null && coord != null) {
		    synchronized (this) {
			theCoordinator = coord;
			notifyAll();
		    }
		}
	    }
	    break;
	case MessageType.P2P_REPLY:
	    if (res instanceof ServiceResponse) {
		HashMap<String, Object> callContext = allWaitingCalls
			.remove(msg.getInReplyTo());
		if (handleResponseOfInjectedCall(msg, callContext)) {
		    break;
		}
		if (isCoordinator) {
		    if (callContext == null) {
			// this must be UI service response, because they are
			// answered immediately after the request has been
			// handled and no call context is put in
			// allWaitingCallers
			// TODO: add a log entry for checking if the above
			// assumption is true
			
			// it can also be an injected call -> send (if it is for
			// another node)
			if (!msg.hasReceiver(theCoordinator))
			    send(msg);
			return;
		    }
		    BusMessage request = (BusMessage) callContext
			    .get(CONTEXT_REQUEST_MESSAGE);

		    WaitingRequest wr = allWaitingRequests.get(request.getID());
		    Vector<HashMap<String, Object>> allCalls = wr.matches;
		    if (allCalls == null) {
			// response already timed out => ignore this delayed one
			LogUtils.logDebug(
				ServiceBusImpl.getModuleContext(),
				ServiceStrategy.class,
				"handle",
				"Received a ServiceReponse but there is no request waiting for the response. "
					+ "Maybe a timeout occurred and this response arrived too late.");
			return;
		    }
		    synchronized (allCalls) {
			callContext.put(CONTEXT_RESPONSE_MESSAGE, res);
			wr.pendingCalls--;
			if (wr.pendingCalls == 0)
			    sendServiceResponse(request);
		    }
		} else {
		    // normally, it is sufficient to check
		    // if (msg.hasReceiver(theCoordinator)) {
		    // but there is one case where we have to send anyway: when
		    // the call was injected from a node that was not the
		    // coordinator
		    send(msg);
		}
	    } else if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_COORDINATOR)) {
		PeerCard coord = AbstractBus.getPeerFromBusResourceURI(res
			.getURI());
		if (theCoordinator == null && coord != null) {
		    synchronized (this) {
			theCoordinator = coord;
			notifyAll();
		    }
		}
	    } else if (res.getType().equals(
		    TYPE_uAAL_SERVICE_PROFILE_INFORMATION)) {
		synchronized (this) {
		    String realizationID = (String) res
			    .getProperty(PROP_uAAL_SERVICE_REALIZATION_ID);
		    List<?> profiles = (List<?>) res
			    .getProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE);

		    localServiceSearchResults.addProfiles(realizationID,
			    profiles);

		    notifyAll();
		}
	    }
	    break;
	case MessageType.P2P_REQUEST:
	    if (res instanceof ServiceCall) {
		ServiceRealization sr;
		synchronized (localServicesIndex) {
		    sr = localServicesIndex
			    .getServiceRealizationByID(((ServiceCall) res)
				    .getProcessURI());
		}
		if (sr != null) {
		    ServiceCallee callee = (ServiceCallee) getBusMember(sr
			    .getProperty(
				    ServiceRealization.uAAL_SERVICE_PROVIDER)
			    .toString());
		    if (callee != null) {
			callee.handleCall(msg);
			break;
		    }
		}
		// we could not get the service realization or the bus member
		// for the call, this should not happen
		// TODO: handle somehow, e.g. send an empty/error-response
	    } else if (isCoordinator
		    && res.getType().equals(TYPE_uAAL_SERVICE_BUS_COORDINATOR)) {
		res = new Resource(bus.getURI());
		res.addType(TYPE_uAAL_SERVICE_BUS_COORDINATOR, true);
		((ServiceBusImpl) bus).assessContentSerialization(res);
		send(msg.createReply(res));
	    } else if (isCoordinator
		    && res.getType().equals(
			    TYPE_uAAL_SERVICE_PROFILE_INFORMATION)) {

		Resource r = new Resource();
		String realizationID = (String) res
			.getProperty(PROP_uAAL_SERVICE_REALIZATION_ID);
		r.addType(TYPE_uAAL_SERVICE_PROFILE_INFORMATION, true);
		r.setProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE,
			Arrays.asList(getCoordinatorServices(realizationID)));
		r.setProperty(PROP_uAAL_SERVICE_REALIZATION_ID, realizationID);

		((ServiceBusImpl) bus).assessContentSerialization(r);

		send(msg.createReply(r));
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
		if (!msg.senderResidesOnDifferentPeer())
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
				ArrayList<ServiceRealization> v = startDialogs
					.get(csc.toString());
				if (hv instanceof Resource)
				    replyToInitialDialogInfoRequest(msg, v,
					    hv.toString());
				else
				    replyToInitialDialogInfoRequest(msg, v);
			    }
			} else if (hv instanceof Resource
				&& request
					.getURI()
					.startsWith(
						InitialServiceDialog.SERVICE_REQUEST_URI_PREFIX_START)) {
			    synchronized (startDialogs) {
				callStartDialog(
					startDialogs.get(csc.toString()),
					hv.toString(), msg);
			    }
			} else
			    sendNoMatchingFound(msg);
		    } else
			sendNoMatchingFound(msg);
		    return;
		}
		Vector<HashMap<String, Object>> matches = new Vector<HashMap<String, Object>>();
		String serviceURI = request.getRequestedService().getClassURI();
		// start the logging with trace messages about matchmaking
		// the logID as last parameter in each message is used to
		// identify different log messages that belong to each other
		// TODO: make this configurable
		Long logID = Long.valueOf(Thread.currentThread().getId());
		synchronized (allServicesIndex) {
		    LogUtils.logTrace(ServiceBusImpl.getModuleContext(),
			    ServiceStrategy.class, "handle", new Object[] {
				    ServiceBus.LOG_MATCHING_START,
				    new UnmodifiableResource(request), " ",
				    logID }, null);

		    ArrayList<ServiceRealization> arrServices = allServicesIndex
			    .get(serviceURI);
		    if (arrServices == null) {
			logTrace(
				"handle",
				new Object[] {
					ServiceBus.LOG_MATCHING_END,
					" No service available. ",
					ServiceBus.LOG_MATCHING_MISMATCH_CODE,
					Integer.valueOf(1030),
					ServiceBus.LOG_MATCHING_MISMATCH_DETAILS,
					" No service has registered for the requested serviceURI.",
					logID });
			logID = null; // no more trace log messages
			sendNoMatchingFound(msg);
		    } else {
			String caller = request.getProperty(
				ServiceRequest.PROP_uAAL_SERVICE_CALLER)
				.toString();

			for (ServiceRealization sr : arrServices) {
			    Service profileService = ((ServiceProfile) sr
				    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
				    .getTheService();
			    String profileServiceURI = profileService.getURI();
			    String profileProviderURI = (String) sr
				    .getProvider();

			    logTrace("handle", new Object[] {
				    ServiceBus.LOG_MATCHING_PROFILE,
				    profileService.getType(),
				    profileServiceURI, profileProviderURI,
				    logID });
			    HashMap<String, Object> context = matches(caller,
				    request, sr, logID);
			    if (context != null) {
				matches.add(context);
				logTrace(
					"handle",
					new Object[] {
						ServiceBus.LOG_MATCHING_SUCCESS,
						logID });
			    } else
				logTrace("handle", new Object[] {
					ServiceBus.LOG_MATCHING_NOSUCCESS,
					logID });
			}
		    }
		}
		int matchesFound = 0;
		HashMap<String, HashMap<String, Object>> auxMap = new HashMap<String, HashMap<String, Object>>();
		for (HashMap<String, Object> match : matches) {
		    ServiceRealization sr = (ServiceRealization) match
			    .get(Constants.VAR_uAAL_SERVICE_TO_SELECT);
		    if (sr.assertServiceCall(match, request)) {
			matchesFound++;
			HashMap<String, Object> otherMatch = auxMap.get(sr
				.getProvider());
			if (otherMatch == null)
			    auxMap.put(sr.getProvider(), match);
			else {
			    // uAAL_SERVICE_URI_MATCHED:
			    // New strategy: if service matches exactly URI
			    // specified in Service Request than this service is
			    // always preferred over others.
			    if ((match
				    .get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) != null)
				    && (otherMatch
					    .get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) == null)) {
				// the new service matches better the
				// request
				auxMap.put(sr.getProvider(), match);
				continue;
			    }
			    if ((otherMatch
				    .get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) != null)
				    && (match
					    .get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) == null)) {
				// the new service won't match better the
				// request
				continue;
			    }
			    // If two above are not true then either both
			    // services have matched their URIs or none of them
			    // had. Either way regular strategy is applied.

			    // The strategy: from each provider accept the one
			    // with more specialization
			    // and then the one with the smallest context, which
			    // produces shorter messages

			    // TODO: is the above strategy ok? The issues is:
			    // is the length of messages a good criteria?
			    if (otherMatch.size() > match.size())
				auxMap.put(sr.getProvider(), match);
			}
		    } else {
			// we could not create the ServiceCall, this most
			// probably happened because of a missing input
			// -> log message
			logTrace(
				"handle",
				new Object[] {
					ServiceBus.LOG_MATCHING_MISMATCH,
					"input in profile not given in request",
					"\nprofile URI: ",
					((ServiceProfile) sr
						.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
						.getTheService().getURI(),
					ServiceBus.LOG_MATCHING_MISMATCH_CODE,
					Integer.valueOf(1031),
					ServiceBus.LOG_MATCHING_MISMATCH_DETAILS,
					"ServiceCall could not be created. The most probable reason is"
						+ " that a mandatory input required by the service profile"
						+ " is not provided by the service request.",
					logID });
		    }
		}
		matches = new Vector<HashMap<String, Object>>(auxMap.values());

		if (logID != null) {
		    // first log the number of matches before provider filtering
		    logTrace("handle", new Object[] {
			    ServiceBus.LOG_MATCHING_PROFILES_END, " Found ",
			    Integer.valueOf(matchesFound), " matches", logID });

		    // then log the number (and profile URIs) after provider
		    // filtering
		    Object obj[] = new Object[5 + matches.size()];
		    obj[0] = ServiceBus.LOG_MATCHING_PROVIDER_END;
		    obj[1] = " Found ";
		    obj[2] = Integer.valueOf(matches.size());
		    obj[3] = " matches. The matching profiles are: ";
		    int i = 4;
		    for (HashMap<String, Object> match : matches) {
			ServiceRealization sr = (ServiceRealization) match
				.get(Constants.VAR_uAAL_SERVICE_TO_SELECT);
			Service profileService = ((ServiceProfile) sr
				.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
				.getTheService();
			String profileServiceURI = profileService.getURI();
			obj[i++] = profileServiceURI;
		    }
		    obj[i] = logID;

		    logTrace("handle", obj);
		}

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
		    HashMap<String, Object> context = matches.remove(0);
		    while (!matches.isEmpty()) {
			HashMap<String, Object> aux = matches.remove(0);
			if (aux.size() < context.size())
			    context = aux;
		    }
		    matches.add(context);
		}
		int size = matches.size();
		if (size == 0) {
		    sendNoMatchingFound(msg);
		    logTrace("handle",
			    new Object[] { ServiceBus.LOG_MATCHING_END,
				    "found ", Integer.valueOf(matches.size()),
				    " matches", logID });
		} else {
		    if (size > 1) {
			List<AggregatingFilter> filters = request.getFilters();
			if (filters != null && filters.size() > 0) {
			    int[] points = new int[size];
			    for (int i = 0; i < points.length; i++)
				points[i] = 0;
			    for (AggregatingFilter af : filters) {
				List<?> params = af.getFunctionParams();
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
						matches.get(j), pp[1]);
					for (int k = j + 1; k < size; k++) {
					    Object ok = getProfileParameter(
						    matches.get(k), pp[1]);
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
						matches.get(j), pp[1]);
					for (int k = j + 1; k < size; k++) {
					    Object ok = getProfileParameter(
						    matches.get(k), pp[1]);
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
						matches.get(j), pp[1]);
					for (int k = j + 1; k < size; k++) {
					    Object ok = getProfileParameter(
						    matches.get(k), pp[1]);
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
						matches.get(j), pp[1]);
					for (int k = j + 1; k < size; k++) {
					    Object ok = getProfileParameter(
						    matches.get(k), pp[1]);
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
		    logTrace("handle",
			    new Object[] { ServiceBus.LOG_MATCHING_END,
				    "found ", Integer.valueOf(matches.size()),
				    " matches", logID });
		    callServices(msg, matches);
		}

	    } else if (msg.senderResidesOnDifferentPeer()) {
		// strange situation: some peer has thought i am the
		// coordinator?!!
		// => ignore!
		LogUtils.logDebug(
			ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class,
			"handle",
			new Object[] { "Received a message of type 'request'. A peer has send this message and thought I would be the coordinator, but I'm not. Ignoring it." },
			null);
	    } else if (isCoordinatorKnown()) {
		localWaitingCallers.addLocalWaitier(msg.getID(), senderID);
		msg.setReceiver(theCoordinator);
		send(msg);
	    }
	    break;
	}
    }

    private boolean handleResponseOfInjectedCall(BusMessage msg,
	    HashMap<String, Object> callContext) {
	if (callContext == null)
	    return false;
	String callerID = (String) callContext.get(CONTEXT_INJECT_CALLER);
	if (callerID == null)
	    return false;

	// the service call is of type MessageType.P2P_REPLY, but we need a
	// MessageType.reply -> rewrite the type
	msg.setType(MessageType.reply);

	replyToLocalCaller(msg);
	return true;
    }

    /**
     * Sends a reply to the initial dialog info request message. The reply will
     * contain the matched services.
     * 
     * @param m
     *            the initial dialog info request message
     * @param matchingServices
     */
    private void replyToInitialDialogInfoRequest(BusMessage m,
	    ArrayList<ServiceRealization> matchingServices) {
	if (matchingServices == null) {
	    sendNoMatchingFound(m);
	    return;
	}

	List<Service> result = new ArrayList<Service>(matchingServices.size());
	for (ServiceRealization sr : matchingServices) {
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
	((ServiceBusImpl) bus).assessContentSerialization(resp);
	m = m.createReply(resp);
	if (m.receiverResidesOnDifferentPeer())
	    send(m);
	else
	    replyToLocalCaller(m);
    }

    /**
     * Sends a reply to the initial dialog info request message. The reply will
     * contain a description of a matched service of the vendor whose ID is
     * passed as a parameter
     * 
     * @param m
     *            the initial dialog info request message
     * @param matchingServices
     */
    private void replyToInitialDialogInfoRequest(BusMessage m,
	    ArrayList<ServiceRealization> matchingServices, String vendor) {
	if (matchingServices == null) {
	    sendNoMatchingFound(m);
	    return;
	}

	Object description = null;
	for (ServiceRealization sr : matchingServices) {
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
	((ServiceBusImpl) bus).assessContentSerialization(resp);
	m = m.createReply(resp);
	if (m.receiverResidesOnDifferentPeer())
	    send(m);
	else
	    replyToLocalCaller(m);
    }

    /**
     * Send the reply message to a local caller
     * 
     * @param msg
     *            the reply message
     */
    private void replyToLocalCaller(BusMessage msg) {
	String replyOf = msg.getInReplyTo();
	if (replyOf == null) {
	    // very strange! a message of type REPLY without inReplyTo
	    // => ignore!
	    LogUtils.logDebug(
		    ServiceBusImpl.getModuleContext(),
		    ServiceStrategy.class,
		    "replyToLocalCaller",
		    new Object[] { "Message of type REPLY, but not containing inReplyTo. Ignoring it." },
		    null);
	} else {
	    String callerID = localWaitingCallers
		    .getAndRemoveLocalWaiterCallerID(replyOf);
	    if (callerID == null) {
		// very strange! why else may I receive a reply from a peer
		// => ignore!
		LogUtils.logDebug(
			ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class,
			"replyToLocalCaller",
			new Object[] { "There is no caller. To whom should I then reply? Ignoring it." },
			null);
	    } else {
		Object caller = getBusMember(callerID);
		if (caller instanceof ServiceCaller)
		    ((ServiceCaller) caller).handleReply(msg);
		else {
		    // very strange! why else may I receive a reply from a peer
		    // => ignore!
		    LogUtils.logDebug(ServiceBusImpl.getModuleContext(),
			    ServiceStrategy.class, "replyToLocalCaller",
			    new Object[] { "The caller '" + callerID
				    + "' is no ServiceCaller. Ignoring it." },
			    null);
		}
	    }
	}
    }

    /**
     * Send a no-matching-found message as a reply to the message passed as a
     * parameter
     * 
     * @param m
     *            the message to send a reply to
     */
    private void sendNoMatchingFound(BusMessage m) {
	sendSimpleReply(m, CallStatus.noMatchingServiceFound);
    }

    /**
     * Send a message of type <tt>status</tt> as a reply to the message passed
     * as parameter
     * 
     * @param message
     *            the message to reply to
     * @param status
     *            the {@link CallStatus} this reply should have
     */
    private void sendSimpleReply(BusMessage message, CallStatus status) {
	BusMessage reply = message.createReply(new ServiceResponse(status));
	if (reply.receiverResidesOnDifferentPeer()) {
	    send(reply);
	} else {
	    replyToLocalCaller(reply);
	}
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
		LogUtils.logWarn(
			ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class,
			"indexServices",
			"Trying to index a ui service (InitialServiceDialog), but the correlatedServiceClass is not a resource: "
				+ correlService + " -> ignoring it!");
		return;
	    }
	    synchronized (startDialogs) {
		String key = correlService.toString();
		ArrayList<ServiceRealization> lst = startDialogs.get(key);
		if (lst == null) {
		    lst = new ArrayList<ServiceRealization>();
		    startDialogs.put(key, lst);
		}
		lst.add(registration);
	    }
	} else {
	    List<String> serviceURIs = getNonAbstractSuperClasses(theService);
	    synchronized (allServicesIndex) {
		for (String serviceURI : serviceURIs) {
		    ArrayList<ServiceRealization> arrsr = allServicesIndex
			    .get(serviceURI);
		    if (arrsr == null) {
			arrsr = new ArrayList<ServiceRealization>();
			allServicesIndex.put(serviceURI, arrsr);
		    }
		    arrsr.add(registration);
		    ArrayList<AvailabilitySubscription> subscribers = allSubscriptionsIndex
			    .get(serviceURI);
		    if (subscribers != null) {
			for (AvailabilitySubscription as : subscribers) {
			    if (null != matches(as.id,
				    (ServiceRequest) as.reqOrSubs, registration))
				notifySubscriber(as, processURI, true);
			}
		    }
		}
	    }
	}
    }

    private boolean isCoordinatorKnown() {
	if (theCoordinator == null) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_COORDINATOR, true);
	    ((ServiceBusImpl) bus).assessContentSerialization(r);
	    BusMessage m = new BusMessage(MessageType.p2p_request, r, bus);
	    send(m);
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
     *            the caller ID of the ServiceRequest
     * @param request
     *            the ServiceRequest
     * @param offer
     *            the Service Realization being matched
     * @return a map of the context of the matching or null if the
     *         ServiceRealization does not match the ServiceRequest
     */
    private HashMap<String, Object> matches(String callerID,
	    ServiceRequest request, ServiceRealization offer) {
	return matches(callerID, request, offer, null);
    }

    /**
     * Returns true iff a ServiceRealization passed as a parameter matches the
     * ServiceRequest
     * 
     * @param callerID
     *            the caller ID of the ServiceRequest
     * @param request
     *            the ServiceRequest
     * @param offer
     *            the Service Realization being matched
     * @param logID
     *            an id to be used for logging, may be null
     * @return a HashMap of the context of the matching or null if the
     *         ServiceRealization does not match the ServiceRequest
     */
    private HashMap<String, Object> matches(String callerID,
	    ServiceRequest request, ServiceRealization offer, Long logID) {
	HashMap<String, Object> context = new HashMap<String, Object>();
	context.put(Constants.VAR_uAAL_ACCESSING_BUS_MEMBER, callerID);
	context.put(Constants.VAR_uAAL_CURRENT_DATETIME,
		TypeMapper.getCurrentDateTime());
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
     *            the subscriber
     * @param request
     *            the ID of the subscription
     * @param realization
     *            the ID of the ServiceRealization
     * @param registers
     *            boolean, true if the notification is about a registered
     *            service, false if the notification is about an unregistered
     *            service
     */
    private void notifyLocalSubscriber(String caller, String request,
	    String realization, boolean registers) {
	Vector<AvailabilitySubscription> v = localSubscriptionsIndex
		.get(caller);
	if (v != null) {
	    for (AvailabilitySubscription as : v) {
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
    }

    /**
     * Notify the Availability Subscriber about registration/unregistration of
     * Services (ServiceRealization representing the Services)
     * 
     * @param as
     *            the availability subscription
     * @param realizationID
     *            the ID of the ServiceRealization
     * @param registers
     *            boolean, true if the notification is about a registered
     *            service, false if the notification is about an unregistered
     *            service
     */
    private void notifySubscriber(AvailabilitySubscription as,
	    String realizationID, boolean registers) {
	if (bus.isValidMember(as.callerID))
	    notifyLocalSubscriber(as.id, ((Resource) as.reqOrSubs).getURI(),
		    realizationID, registers);
	else {
	    Resource res = new Resource();
	    res.addType(TYPE_uAAL_SERVICE_BUS_NOTIFICATION, true);
	    res.setProperty(PROP_uAAL_REGISTRATION_STATUS,
		    (registers ? RES_STATUS_REGISTERED
			    : RES_STATUS_DEREGISTERED));
	    res.setProperty(PROP_uAAL_SERVICE_REALIZATION_ID, new Resource(
		    realizationID));
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER, new Resource(as.id));
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST, new Resource(
		    ((Resource) as.reqOrSubs).getURI()));
	    ((ServiceBusImpl) bus).assessContentSerialization(res);
	    BusMessage m = new BusMessage(MessageType.event, res, bus);
	    m.setReceiver(AbstractBus.getPeerFromBusResourceURI(as.callerID));
	    send(m);
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
     *            the subscribing caller ID
     * @param subscriber
     *            the subscriber object
     * @param requestURI
     *            the URI of the request to subscribe
     */
    void removeAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, String requestURI) {
	if (requestURI == null || subscriber == null
		|| localSubscriptionsIndex.get(callerID) == null)
	    return;

	Vector<AvailabilitySubscription> v = localSubscriptionsIndex
		.get(callerID);
	String serviceClassURI = null;
	if (v != null) {
	    for (Iterator<AvailabilitySubscription> i = v.iterator(); i
		    .hasNext();) {
		AvailabilitySubscription as = i.next();
		if (requestURI.equals(as.id) && subscriber == as.reqOrSubs) {
		    serviceClassURI = as.serviceClassURI;
		    i.remove();
		    break;
		}
	    }
	}

	if (isCoordinator) {
	    synchronized (allServicesIndex) {
		ArrayList<AvailabilitySubscription> arrAS = allSubscriptionsIndex
			.get(serviceClassURI);
		if (arrAS != null)
		    for (Iterator<AvailabilitySubscription> i = arrAS
			    .iterator(); i.hasNext();) {
			AvailabilitySubscription as = i.next();
			if (callerID.equals(as.id)
				&& ((Resource) as.reqOrSubs).getURI().equals(
					requestURI)) {
			    i.remove();
			    break;
			}
		    }
	    }
	} else if (isCoordinatorKnown()) {
	    Resource res = new Resource(callerID);
	    res.addType(TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION, true);
	    res.setProperty(PROP_uAAL_SERVICE_TYPE, new Resource(
		    serviceClassURI));
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST, new Resource(
		    requestURI));
	    res.setProperty(PROP_uAAL_REGISTRATION_STATUS,
		    RES_STATUS_DEREGISTERED);
	    ((ServiceBusImpl) bus).assessContentSerialization(res);
	    BusMessage m = new BusMessage(MessageType.p2p_event, res, bus);
	    m.setReceiver(theCoordinator);
	    send(m);
	}
    }

    /**
     * Remove service profiles to a previously registered ServiceCallee
     * 
     * @param calleeID
     *            the URI of the ServiceCallee
     * @param realizedServices
     *            the service profiles to remove
     */
    void removeMatchingRegParams(String calleeID,
	    ServiceProfile[] realizedServices) {
	if (realizedServices == null || calleeID == null
		|| !(getBusMember(calleeID) instanceof ServiceCallee))
	    return;

	for (int i = 0; i < realizedServices.length; i++) {
	    if (realizedServices[i] == null)
		continue;

	    String processURI = realizedServices[i].getProcessURI();
	    if (processURI == null)
		continue;

	    synchronized (localServicesIndex) {
		ServiceRealization reg = localServicesIndex
			.removeServiceRealization(processURI);
		if (reg == null)
		    continue;
		if (!calleeID.equals(reg
			.getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER))
			|| !processURI
				.equals(((ServiceProfile) reg
					.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
					.getProcessURI())) {
		    localServicesIndex.addServiceRealization(processURI, reg);
		}
	    }

	    if (isCoordinator)
		unindexServices(calleeID, processURI);
	}

	if (!isCoordinator && isCoordinatorKnown()) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_REGISTRATION, true);
	    r.setProperty(PROP_uAAL_REGISTRATION_STATUS,
		    RES_STATUS_DEREGISTERED);
	    r.setProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE,
		    Arrays.asList(realizedServices));
	    r.setProperty(PROP_uAAL_SERVICE_PROVIDED_BY, new Resource(calleeID));
	    ((ServiceBusImpl) bus).assessContentSerialization(r);
	    BusMessage m = new BusMessage(MessageType.p2p_event, r, bus);
	    m.setReceiver(theCoordinator);
	    send(m);
	}
    }

    /**
     * Remove registration parameters for a calleID passed as a parameter
     * 
     * @param calleeID
     *            the URI of the callee for which the registration parameters
     *            are removed
     */
    void removeRegParams(String calleeID) {
	if (calleeID == null
		|| !(getBusMember(calleeID) instanceof ServiceCallee))
	    return;

	synchronized (localServicesIndex) {
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
	}

	if (isCoordinator)
	    unindexServices(calleeID, null);
	else if (isCoordinatorKnown()) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_REGISTRATION, true);
	    r.setProperty(PROP_uAAL_REGISTRATION_STATUS,
		    RES_STATUS_DEREGISTERED);
	    r.setProperty(PROP_uAAL_SERVICE_PROVIDED_BY, new Resource(calleeID));
	    ((ServiceBusImpl) bus).assessContentSerialization(r);
	    BusMessage m = new BusMessage(MessageType.p2p_event, r, bus);
	    m.setReceiver(theCoordinator);
	    send(m);
	}
    }

    /**
     * Remove services of the callee passed as a parameter from the index of
     * services
     * 
     * @param calleeID
     *            the URI of the caller
     * @param processURI
     *            the URI of the process
     */
    private void unindexServices(String calleeID, String processURI) {
	boolean deleteAll = (processURI == null);
	synchronized (allServicesIndex) {
	    for (ArrayList<ServiceRealization> i : allServicesIndex.values()) {
		for (Iterator<ServiceRealization> j = i.iterator(); j.hasNext();) {
		    ServiceRealization reg = j.next();
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
			ArrayList<AvailabilitySubscription> subscribers = allSubscriptionsIndex
				.get(serviceURI);
			if (subscribers != null) {
			    for (AvailabilitySubscription as : subscribers) {
				if (null != matches(as.id,
					(ServiceRequest) as.reqOrSubs, reg))
				    notifySubscriber(as, processURI, false);
			    }
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
     *            the URI of the Service whose profiles are returned
     * @return the service profiles of the given service
     */
    public ServiceProfile[] getAllServiceProfiles(String serviceURI) {
	if (this.isCoordinator)
	    return getCoordinatorServices(serviceURI);

	Resource r = new Resource();
	r.addType(TYPE_uAAL_SERVICE_PROFILE_INFORMATION, true);
	r.setProperty(PROP_uAAL_SERVICE_REALIZATION_ID, serviceURI);
	((ServiceBusImpl) bus).assessContentSerialization(r);
	BusMessage m = new BusMessage(MessageType.p2p_request, r, bus);
	m.setReceiver(theCoordinator);
	send(m);

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

	List<?> profiles = (List<?>) this.localServiceSearchResults
		.getProfiles(serviceURI);
	return profileListToArray(profiles);
    }

    public HashMap<String, List<ServiceProfile>> getAllServiceProfilesWithCalleeIDs(
	    String serviceURI) {
	return getCoordinatorServicesWithCalleeIDs(serviceURI);
    }

    private HashMap<String, List<ServiceProfile>> getCoordinatorServicesWithCalleeIDs(
	    String serviceURI) {
	HashMap<String, List<ServiceProfile>> map = new HashMap<String, List<ServiceProfile>>();
	if (this.isCoordinator) {
	    synchronized (allServicesIndex) {
		ArrayList<ServiceRealization> neededProfiles = allServicesIndex
			.get(serviceURI);
		if (neededProfiles != null) {
		    for (ServiceRealization reg : neededProfiles) {
			ServiceProfile profile = (ServiceProfile) reg
				.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);

			String provider = (String) reg.getProvider();
			if (map.get(provider) == null) {
			    map.put(provider, new ArrayList<ServiceProfile>());
			}
			map.get(provider).add(profile);
		    }
		}
	    }
	}

	return map;
    }

    /**
     * Return the profiles registered for the service passed as a parameter,
     * only if this peer is a coordinator. Otherwise, an empty list is returned.
     * 
     * @param serviceURI
     *            the URI of the service whose profiles are returned
     * @return the profiles of the service passed as a parameter
     */
    private ServiceProfile[] getCoordinatorServices(String serviceURI) {
	ArrayList<ServiceProfile> profiles = new ArrayList<ServiceProfile>();
	if (this.isCoordinator) {
	    synchronized (allServicesIndex) {
		ArrayList<ServiceRealization> neededProfiles = allServicesIndex
			.get(serviceURI);
		if (neededProfiles != null)
		    for (ServiceRealization reg : neededProfiles) {
			ServiceProfile profile = (ServiceProfile) reg
				.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
			if (profile != null)
			    profiles.add(profile);
		    }
	    }
	}

	return profileListToArray(profiles);
    }

    /**
     * This method translates a List of ServiceProfiles into an array of
     * ServiceProfiles
     * 
     * @param list
     *            the list to translate
     * @return the translated array of ServiceProfiles
     */
    private ServiceProfile[] profileListToArray(List<?> list) {
	if (list == null)
	    return new ServiceProfile[0];
	ServiceProfile[] result = new ServiceProfile[list.size()];
	for (int i = 0; i < result.length; i++)
	    result[i] = (ServiceProfile) list.get(i);

	return result;
    }
}
