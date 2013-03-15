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
import org.universAAL.middleware.service.owl.UserInterfaceService;
import org.universAAL.middleware.service.owls.process.OutputBinding;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
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
	String callerID;
	String id;
	Object reqOrSubs;
    }

    private Map<String, List<ServiceRealization>> allServicesIndex;
    private Map<String, List<ServiceRealization>> startDialogs;
    private Map<String, List<AvailabilitySubscription>> allSubscriptionsIndex;

    /** request.msgID -> Vector(call.context) + call.msgID -> call.context */
    private Map<String, Object> allWaitingCallers; // Map<String,
    // List<Map<String, Object>>>
    private Map<String, Map<String, Object>> allCallContextsForWaitingMessageIDs;

    private Map<String, String> localSubscriptionsIndex;
    private Map<String, List<AvailabilitySubscription>> localSubscriptions;

    // request.msgID -> callerID
    protected ILocalWaitingCallersData localWaitingCallers;
    // processURI -> ServiceRealization
    protected ILocalServicesIndexData localServicesIndex;
    // serviceURI -> List(ServiceRealization) (was replaced with the new
    // mechanism)
    protected ILocalServiceSearchResultsData localServiceSearchResults;
    private boolean isCoordinator;
    protected PeerCard theCoordinator = null;

    public ServiceStrategy(CommunicationModule commModule, ModuleContext mc) {
	super(commModule);

	// Initiated the factory
	IServiceStrategyDataFactory factory = createServiceStrategyDataFactory();

	// dummy call to force the load of the class InitialServiceDialog
	StringUtils.isNonEmpty(InitialServiceDialog.MY_URI);

	localSubscriptionsIndex = new Hashtable<String, String>();
	localSubscriptions = new Hashtable<String, List<AvailabilitySubscription>>();
	localServicesIndex = factory.createLocalServicesIndexData();
	localWaitingCallers = factory.createLocalWaitingCallersData();
	localServiceSearchResults = factory
		.createLocalServiceSearchResultsData();
	isCoordinator = Constants.isCoordinatorInstance();
	LogUtils
		.logDebug(
			ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class,
			"ServiceStrategy",
			new Object[] { "This instance is ",
				isCoordinator ? "" : "NOT ", "the coordinator." },
			null);
	if (isCoordinator) {
	    allServicesIndex = new Hashtable<String, List<ServiceRealization>>();
	    allSubscriptionsIndex = new Hashtable<String, List<AvailabilitySubscription>>();
	    allWaitingCallers = new Hashtable<String, Object>();
	    allCallContextsForWaitingMessageIDs = new Hashtable<String, Map<String, Object>>();
	    startDialogs = new Hashtable<String, List<ServiceRealization>>();
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
    void addAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, ServiceRequest request) {
	if (request == null || subscriber == null || request.isAnon()) {
	    return;
	}

	AvailabilitySubscription availabilitySubscription = new AvailabilitySubscription();
	availabilitySubscription.callerID = callerID;
	availabilitySubscription.id = request.getURI();
	availabilitySubscription.reqOrSubs = subscriber;

	safeGet(localSubscriptions, callerID).add(availabilitySubscription);
	localSubscriptionsIndex.put(availabilitySubscription.id, request
		.getRequestedService().getType());

	if (isCoordinator) {
	    addSubscriber(callerID, request);
	} else if (isCoordinatorKnown()) {
	    Resource res = new Resource(callerID);
	    res.addType(TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION, true);
	    res.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST, request);
	    res.setProperty(PROP_uAAL_REGISTERATION_STATUS,
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
     *            - the id of the ServiceCallee
     * @param realizedServices
     *            - the profiles to add
     */
    void addRegParams(String calleeID, ServiceProfile[] realizedServices) {
	if (realizedServices == null
		|| calleeID == null
		|| !(getBusMember(calleeID) instanceof ServiceCallee)) {
	    return;
	}

	for (ServiceProfile realizedService : realizedServices) {
	    // check for qualifications of each realized service
	    if (realizedService == null
		    || realizedService.getTheService() == null) {
		// ignore not-qualified ones
		continue;
	    }

	    String processURI = realizedService.getProcessURI();
	    if (processURI == null) {
		// ignore not-qualified ones
		continue;
	    }

	    // qualifications fulfilled -> associate service with its provider
	    ServiceRealization registration = new ServiceRealization(calleeID,
		    realizedService);
	    // index it over the ID of the operation registered
	    localServicesIndex.addServiceRealization(processURI, registration);

	    if (isCoordinator) {
		// more complex indexing of services by the coordinator
		indexServices(realizedService, registration, processURI);
	    }
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
	    ((ServiceBusImpl) bus).assessContentSerialization(r);
	    BusMessage m = new BusMessage(MessageType.p2p_event, r, bus);
	    m.setReceiver(theCoordinator);
	    send(m);
	} else if (theCoordinator == null) {
	    // we get here only if this peer provides the coordinator instance
	    // and this is the first time that service profiles are being
	    // registered
	    // => use this deterministic situation for announcing to all other
	    // peers that the coordinator is here
	    // first prevent repeating the broadcast by making the variable
	    // not-null
	    theCoordinator = bus.getPeerCard();
	    // publish an event informing all peers started prior to the
	    // coordinator about the availability of the coordinator
	    // do this in a thread after waiting 10 seconds to give it a chance
	    // that any running join process within the AAL Space is finished
	    new Thread() {
		@Override
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
     *            - the id of the subscriber
     * @param request
     *            - the request to describe the desired services
     */
    private void addSubscriber(String callerID, ServiceRequest request) {
	String serviceURI = request.getRequestedService().getType();
	synchronized (allServicesIndex) {
	    AvailabilitySubscription availabilitySubscription = new AvailabilitySubscription();
	    availabilitySubscription.id = callerID;
	    availabilitySubscription.reqOrSubs = request;

	    allSubscriptionsIndex.put(serviceURI,
		    new Vector<AvailabilitySubscription>());
	    allSubscriptionsIndex.get(serviceURI).add(availabilitySubscription);

	    List<ServiceRealization> realizations = allServicesIndex
		    .get(serviceURI);
	    if (realizations != null) {
		for (ServiceRealization realization : realizations) {
		    if (null != matches(callerID, request, realization)) {
			notifySubscriber(availabilitySubscription,
				getProcessURIOfServiceProfile(realization),
				true);
		    }
		}
	    }
	}
    }

    /**
     * Pass the call message to the matching service callees
     * 
     * @param message
     *            - the message
     * @param matches
     *            - a list of hashtables that describe the matched services
     */
    private void callServices(BusMessage message, List matches) {
	int size = matches.size();
	matches.add(new Integer(size));
	allWaitingCallers.put(message.getID(), matches);
	int maxTimeout = 0;
	for (int i = 0; i < size; i++) {
	    Hashtable match = (Hashtable) matches.get(i);
	    match.put(CONTEXT_REQUEST_MESSAGE, message);
	    ServiceRealization sr = (ServiceRealization) match
		    .get(Constants.VAR_uAAL_SERVICE_TO_SELECT);
	    Object timeout = ((ServiceProfile) sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
		    .getProperty(ServiceProfile.PROP_uAAL_RESPONSE_TIMEOUT);
	    if (timeout instanceof Integer
		    && ((Integer) timeout).intValue() > maxTimeout) {
		maxTimeout = ((Integer) timeout).intValue();
	    }
	    PeerCard receiver = AbstractBus.getPeerFromBusResourceURI(sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER)
		    .toString());
	    ServiceCall sc = (ServiceCall) match
		    .remove(ServiceRealization.uAAL_ASSERTED_SERVICE_CALL);
	    ((ServiceBusImpl) bus).assessContentSerialization(sc);
	    BusMessage call = new BusMessage(MessageType.p2p_request, sc, bus);
	    allWaitingCallers.put(call.getID(), match);
	    // call.getSender is there at least since r2064
	    // (the first revision in saieds sandbox)
	    if (call.getSender().getPeerID().equals(receiver.getPeerID())) {
		handleMessage(call, null);
	    } else {
		List l = new ArrayList(1);
		l.add(receiver);
		call.setReceivers(l);
		send(call);
	    }
	}
	if (maxTimeout > 0) {
	    try {
		Thread.sleep(maxTimeout);
	    } catch (Exception e) {
	    }
	    sendServiceResponse(message);
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
    private void callStartDialog(List<ServiceRealization> matchingServices,
	    String vendor, BusMessage m) {
	if (matchingServices == null) {
	    sendNoMatchingFound(m);
	    return;
	}

	Object calleeID = null, processURI = null;
	for (ServiceRealization realization : matchingServices) {
	    if (realization == null) {
		continue;
	    }
	    ServiceProfile profile = (ServiceProfile) realization
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
	    if (profile == null) {
		continue;
	    }
	    Service service = profile.getTheService();
	    if (service == null) {
		continue;
	    }
	    if (isMatchingVendor(vendor, service)) {
		processURI = profile.getProcessURI();
		calleeID = realization
			.getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER);
		if (processURI instanceof String && calleeID instanceof String) {
		    Object user = (m.getContent() instanceof ServiceRequest) ? ((ServiceRequest) m
			    .getContent())
			    .getProperty(Resource.PROP_uAAL_INVOLVED_HUMAN_USER)
			    : null;
		    ServiceCall sc = new ServiceCall((String) processURI);
		    if (user instanceof Resource) {
			sc.setInvolvedUser((Resource) user);
		    }
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
		} else {
		    processURI = null;
		}
	    }
	}

	if (processURI == null) {
	    sendNoMatchingFound(m);
	} else {
	    ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
	    m = m.createReply(resp);
	    if (m.receiverResidesOnDifferentPeer()) {
		send(m);
	    } else {
		replyToLocalCaller(m);
	    }
	}
    }

    /**
     * Sends a response to the message passed as a parameter
     * 
     * @param m
     *            - the message, to which the response is sent
     */
    private void sendServiceResponse(BusMessage m) {
	Vector matches = (Vector) allWaitingCallers.remove(m.getID());
	if (matches == null) {
	    return;
	}

	synchronized (matches) {
	    int size = matches.size() - 1;
	    int numTimedOut = ((Integer) matches.remove(size)).intValue();
	    if (size == numTimedOut) {
		// there has been no one response => this method is called
		// because of timeout!
		m = m.createReply(new ServiceResponse(
			CallStatus.responseTimedOut));
	    } else {
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
		    if (bads == 0) {
			// shouldn't be the case...
			m = m.createReply(new ServiceResponse(
				CallStatus.responseTimedOut));
		    } else {
			Hashtable bad = null;
			// if there is one response with
			// SERVICE_SPECIFIC_FAILURE take that one
			for (int i = 0; i < size; i++) {
			    if (ssf[i]) {
				bad = (Hashtable) matches.get(i);
				break;
			    } else if (rto[i]) {
				bad = (Hashtable) matches.get(i);
			    } else if (bad == null) {
				bad = (Hashtable) matches.get(i);
			    }
			}
			ServiceResponse sr = (ServiceResponse) bad
				.get(CONTEXT_RESPONSE_MESSAGE);
			if (sr == null) {
			    // see the 'sr == null' comment a dozen lines above
			    sr = new ServiceResponse(
				    CallStatus.responseTimedOut);
			}
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
			for (int i = 0; i < points.length; i++) {
			    points[i] = 0;
			}
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
					if (oj instanceof Comparable) {
					    if (ok == null) {
						points[k]++;
					    } else {
						int l = ((Comparable) oj)
							.compareTo(ok);
						if (l < 0) {
						    points[k]++;
						} else if (l > 0) {
						    points[j]++;
						}
					    }
					} else {
					    points[j]++;
					    if (!(ok instanceof Comparable)) {
						points[k]++;
					    }
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
					if (oj instanceof Comparable) {
					    if (ok == null) {
						points[k]++;
					    } else {
						int l = ((Comparable) oj)
							.compareTo(ok);
						if (l > 0) {
						    points[k]++;
						} else if (l < 0) {
						    points[j]++;
						}
					    }
					} else {
					    points[j]++;
					    if (!(ok instanceof Comparable)) {
						points[k]++;
					    }
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
					if (oj instanceof AbsLocation) {
					    if (ok == null) {
						points[k]++;
					    } else {
						float dj = ((AbsLocation) oj)
							.getDistanceTo((AbsLocation) params
								.get(1));
						float dk = ((AbsLocation) ok)
							.getDistanceTo((AbsLocation) params
								.get(1));
						if (dj < dk) {
						    points[k]++;
						} else if (dk < dj) {
						    points[j]++;
						}
					    }
					} else {
					    points[j]++;
					    if (!(ok instanceof AbsLocation)) {
						points[k]++;
					    }
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
					if (oj instanceof AbsLocation) {
					    if (ok == null) {
						points[k]++;
					    } else {
						float dj = ((AbsLocation) oj)
							.getDistanceTo((AbsLocation) params
								.get(1));
						float dk = ((AbsLocation) ok)
							.getDistanceTo((AbsLocation) params
								.get(1));
						if (dj > dk) {
						    points[k]++;
						} else if (dk > dj) {
						    points[j]++;
						}
					    }
					} else {
					    points[j]++;
					    if (!(ok instanceof AbsLocation)) {
						points[k]++;
					    }
					}
				    }
				}
				break;
			    }
			}
			int ind = 0, min = points[0];
			for (int i = 1; i < size; i++) {
			    if (points[i] < min) {
				ind = i;
				min = points[i];
			    }
			}
			for (int j = 0; j < ind; j++, size--) {
			    goods.remove(0);
			}
			while (size > 1) {
			    goods.remove(--size);
			}
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
				if (tmp == resp) {
				    continue;
				}
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
     *            - a list of ProcessOutputs
     * @param context
     *            - hashtable of bindings for the ProcessOutputs
     */
    private void prepareRequestedOutput(List outputs, Hashtable context) {
	if (outputs != null && !outputs.isEmpty()) {
	    for (int i = outputs.size() - 1; i > -1; i--) {
		ProcessOutput po = (ProcessOutput) outputs.remove(i);
		if (po == null) {
		    continue;
		}
		Resource binding = (Resource) context.get(po.getURI());
		if (binding != null) {
		    String mappedURI = binding.getProperty(
			    OutputBinding.PROP_OWLS_BINDING_TO_PARAM)
			    .toString();
		    if (mappedURI == null) {
			continue;
		    }
		    Object val = po.getParameterValue();
		    if (val == null) {
			continue;
		    }
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
    }

    /**
     * Return a list of non abstract super classes of the service passed as a
     * parameter
     * 
     * @param s
     *            - the service
     * @return Vector - the non-abstract superclasses
     */
    @SuppressWarnings("unchecked")
    private List<String> getNonAbstractSuperClasses(Service s) {
	return ManagedIndividual.getNonAbstractSuperClasses(s);
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
	if (outputs == null || outputs.isEmpty()) {
	    return null;
	}

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
				((AggregatingFilter) o).getFunctionParams())) {
		    for (Iterator j = outputs.iterator(); j.hasNext();) {
			ProcessOutput po = (ProcessOutput) j.next();
			if (key.equals(po.getURI())) {
			    return po.getParameterValue();
			}
		    }
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
    private Object getProfileParameter(Map<String, Object> context, String prop) {
	Object o = context.get(prop);
	if (o == null) {
	    o = ((ServiceProfile) context
		    .get(ServiceRealization.uAAL_SERVICE_PROFILE))
		    .getProperty(prop);
	}
	return o;
    }

    /**
     * Returns a vector from a hashtable from Strings to Vectors. If no vector
     * exists in the hashtable according to the key passed as a parameter, an
     * empty vector is inserted in the hashtable according to the key
     * 
     * @param t
     *            - the hashtable
     * @param key
     *            - the key
     * @return Vector - the value of the key from the hashtable
     */
    private <T> List<T> safeGet(Map<String, List<T>> t, String key) {
	List<T> m = t.get(key);
	if (m == null) {
	    m = new Vector<T>();
	    t.put(key, m);
	}
	return m;
    }

    @Override
    protected void handleDeniedMessage(BusMessage message, String senderID) {
	sendMessageDenied(message);
    }

    /**
     * @see org.universAAL.middleware.sodapop.BusStrategy#handle(org.universAAL.middleware.sodapop.msg.BusMessage,
     *      String)
     */
    @Override
    public void handle(BusMessage msg, String senderID) {
	Resource resource = (Resource) msg.getContent();
	switch (msg.getType().ord()) {
	case MessageType.EVENT:
	    handleEvent(resource);
	    break;
	case MessageType.P2P_EVENT:
	    handleP2PEvent(resource);
	    break;
	case MessageType.P2P_REPLY:
	    handleP2PReply(msg);
	    break;
	case MessageType.P2P_REQUEST:
	    handleP2PRequest(msg);
	    break;
	case MessageType.REPLY:
	    replyToLocalCaller(msg);
	    break;
	case MessageType.REQUEST:
	    handleRequest(msg, senderID);
	    break;
	}
    }

    private void handleRequest(BusMessage msg, String senderID) {
	if (msg.getContent() instanceof ServiceRequest) {
	    handleServiceRequest(msg, senderID);
	} else {
	    LogUtils
		    .logWarn(
			    ServiceBusImpl.getModuleContext(),
			    ServiceStrategy.class,
			    "handleRequest",
			    new Object[] { "Request is no ServiceRequest. Aborted processing." },
			    null);
	}
    }

    private void handleServiceRequest(BusMessage message, String senderID) {
	if (isCoordinator) {
	    coordinateServiceRequest(message, senderID);
	} else if (message.senderResidesOnDifferentPeer()) {
	    LogUtils
		    .logWarn(
			    ServiceBusImpl.getModuleContext(),
			    ServiceStrategy.class,
			    "handleRequest",
			    new Object[] { "Someone thought I am the coordinator, but actually I am not! I'll ignore this message." },
			    null);
	} else if (isCoordinatorKnown()) {
	    forwardMessageToCoordinator(message, senderID);
	}
    }

    private void forwardMessageToCoordinator(BusMessage message, String senderID) {
	localWaitingCallers.addLocalWaitier(message.getID(), senderID);
	message.setReceiver(theCoordinator);
	send(message);
    }

    private void coordinateServiceRequest(BusMessage message, String senderID) {
	ServiceRequest request = (ServiceRequest) message.getContent();
	Service requestedService = request.getRequestedService();
	String serviceURI = requestedService.getClassURI();

	addLocalMessageToWaitingCallers(message, senderID);

	if (requestedService instanceof InitialServiceDialog) {
	    createInitialServiceDialog(message);
	} else {
	    callMatchingServices(message, serviceURI);
	}
    }

    private void callMatchingServices(BusMessage message, String serviceURI) {
	ServiceRequest request = (ServiceRequest) message.getContent();
	List<Map<String, Object>> matches = getBestMatchesForMessage(message,
		serviceURI);

	if (matches.isEmpty()) {
	    sendNoMatchingFound(message);
	    return;
	} else if (matches.size() > 1) {
	    matches = bestMatchingSublist(matches, request);
	}
	callServices(message, matches);
    }

    private List<Map<String, Object>> bestMatchingSublist(
	    List<Map<String, Object>> matches, ServiceRequest request) {
	List<AggregatingFilter> filters = request.getFilters();
	if (filters != null) {
	    int[] scores = scoresForFilters(matches, filters);
	    int minIndex = getIndexOfMinimalElement(scores);
	    matches = matches.subList(minIndex, minIndex + 1);
	}
	return matches;
    }

    private int getIndexOfMinimalElement(int[] array) {
	int index = 0;
	int minimal = array[index];
	for (int i = 1; i < array.length; i++) {
	    if (array[i] < minimal) {
		index = 0;
		minimal = array[i];
	    }
	}
	return index;
    }

    private int[] scoresForFilters(List<Map<String, Object>> matches,
	    List<AggregatingFilter> filters) {
	int[] scores = createZeroInitializedArrayOfLength(matches.size());
	for (AggregatingFilter filter : filters) {
	    updateScoresForFilter(matches, scores, filter);
	}
	return scores;
    }

    private void updateScoresForFilter(List<Map<String, Object>> matches,
	    int[] scores, AggregatingFilter filter) {
	List<?> functionParameterList = filter.getFunctionParams();

	if (startsWithWellformedPropertyPath(functionParameterList)) {
	    String[] propertyPath = ((PropertyPath) functionParameterList
		    .get(0)).getThePath();
	    String target = propertyPath[1];
	    AbsLocation referencedLocation = (AbsLocation) functionParameterList
		    .get(1);

	    switch (filter.getTheFunction().ord()) {
	    case AggregationFunction.ONE_OF:
		break;
	    case AggregationFunction.MIN_OF:
		scoresForMinOf(matches, scores, target);
		break;
	    case AggregationFunction.MAX_OF:
		scoresForMaxOf(matches, scores, target);
		break;
	    case AggregationFunction.MIN_DISTANCE_TO_REF_LOC:
		scoresForMinDistanceToRefLoc(matches, scores, target,
			referencedLocation);
		break;
	    case AggregationFunction.MAX_DISTANCE_TO_REF_LOC:
		scoresForMaxDistanceToRefLoc(matches, scores, target,
			referencedLocation);
		break;
	    }
	}
    }

    private void scoresForMaxDistanceToRefLoc(
	    List<Map<String, Object>> matches, int[] scores, String target,
	    AbsLocation referencedLocation) {
	int numberOfMatches = matches.size();
	for (int currentMatch = 0; currentMatch < numberOfMatches; currentMatch++) {
	    Object current = getProfileParameter(matches.get(currentMatch),
		    target);
	    for (int followingMatch = currentMatch + 1; followingMatch < numberOfMatches; followingMatch++) {
		Object following = getProfileParameter(matches
			.get(followingMatch), target);
		if (current instanceof AbsLocation) {
		    if (following == null) {
			scores[followingMatch]++;
		    } else {
			float currentDistance = ((AbsLocation) current)
				.getDistanceTo(referencedLocation);
			float followingDistance = ((AbsLocation) following)
				.getDistanceTo(referencedLocation);
			if (currentDistance > followingDistance) {
			    scores[followingMatch]++;
			} else if (followingDistance > currentDistance) {
			    scores[currentMatch]++;
			}
		    }
		} else {
		    scores[currentMatch]++;
		    if (!(following instanceof AbsLocation)) {
			scores[followingMatch]++;
		    }
		}
	    }
	}
    }

    private void scoresForMinDistanceToRefLoc(
	    List<Map<String, Object>> matches, int[] scores, String target,
	    AbsLocation referencedLocation) {
	int numberOfMatches = matches.size();
	for (int currentMatch = 0; currentMatch < numberOfMatches; currentMatch++) {
	    Object current = getProfileParameter(matches.get(currentMatch),
		    target);
	    for (int followingMatch = currentMatch + 1; followingMatch < numberOfMatches; followingMatch++) {
		Object following = getProfileParameter(matches
			.get(followingMatch), target);
		if (current instanceof AbsLocation) {
		    if (following == null) {
			scores[followingMatch]++;
		    } else {
			float currentDistance = ((AbsLocation) current)
				.getDistanceTo(referencedLocation);
			float followingDistance = ((AbsLocation) following)
				.getDistanceTo(referencedLocation);
			if (currentDistance < followingDistance) {
			    scores[followingMatch]++;
			} else if (followingDistance < currentDistance) {
			    scores[currentMatch]++;
			}
		    }
		} else {
		    scores[currentMatch]++;
		    if (!(following instanceof AbsLocation)) {
			scores[followingMatch]++;
		    }
		}
	    }
	}
    }

    @SuppressWarnings( { "rawtypes", "unchecked" })
    private void scoresForMaxOf(List<Map<String, Object>> matches, int[] score,
	    String target) {
	int numberOfMatches = matches.size();
	for (int currentMatch = 0; currentMatch < numberOfMatches; currentMatch++) {
	    Object current = getProfileParameter(matches.get(currentMatch),
		    target);
	    for (int followingMatch = currentMatch + 1; followingMatch < numberOfMatches; followingMatch++) {
		Object following = getProfileParameter(matches
			.get(followingMatch), target);
		if (current instanceof Comparable) {
		    if (following == null) {
			score[followingMatch]++;
		    } else {
			int comparison = ((Comparable) current)
				.compareTo(following);
			if (comparison > 0) {
			    score[followingMatch]++;
			} else if (comparison < 0) {
			    score[currentMatch]++;
			}
		    }
		} else {
		    score[currentMatch]++;
		    if (!(following instanceof Comparable)) {
			score[followingMatch]++;
		    }
		}
	    }
	}
    }

    @SuppressWarnings( { "rawtypes", "unchecked" })
    private void scoresForMinOf(List<Map<String, Object>> matches, int[] score,
	    String target) {
	int numberOfMatches = matches.size();
	for (int currentMatch = 0; currentMatch < numberOfMatches; currentMatch++) {
	    Object current = getProfileParameter(matches.get(currentMatch),
		    target);
	    for (int followingMatch = currentMatch + 1; followingMatch < numberOfMatches; followingMatch++) {
		Object following = getProfileParameter(matches
			.get(followingMatch), target);
		if (current instanceof Comparable) {
		    if (following == null) {
			score[followingMatch]++;
		    } else {
			int comparison = ((Comparable) current)
				.compareTo(following);
			if (comparison < 0) {
			    score[followingMatch]++;
			} else if (comparison > 0) {
			    score[currentMatch]++;
			}
		    }
		} else {
		    score[currentMatch]++;
		    if (!(following instanceof Comparable)) {
			score[followingMatch]++;
		    }
		}
	    }
	}
    }

    private boolean startsWithWellformedPropertyPath(List<?> params) {
	return startsWithPropertyPath(params)
		&& isWellformedPropertyPath(((PropertyPath) params.get(0))
			.getThePath());
    }

    private boolean isWellformedPropertyPath(String[] propertyPath) {
	return propertyPath != null && propertyPath.length == 2
		&& Service.PROP_OWLS_PRESENTS.equals(propertyPath[0]);
    }

    private boolean startsWithPropertyPath(List<?> params) {
	return params != null && !params.isEmpty()
		&& params.get(0) instanceof PropertyPath;
    }

    private int[] createZeroInitializedArrayOfLength(int length) {
	int[] array = new int[length];
	for (int i = 0; i < array.length; i++) {
	    array[i] = 0;
	}
	return array;
    }

    private void addLocalMessageToWaitingCallers(BusMessage message,
	    String senderID) {
	if (!message.senderResidesOnDifferentPeer()) {
	    localWaitingCallers.addLocalWaitier(message.getID(), senderID);
	}
    }

    private List<Map<String, Object>> getBestMatchesForMessage(
	    BusMessage message, String serviceURI) {
	ServiceRequest request = (ServiceRequest) message.getContent();

	List<Map<String, Object>> matches = matchingContextsForServiceRealizations(
		message, serviceURI);
	Map<Object, Map<String, Object>> currentBestMatches = findBestMatches(matches);
	matches = new Vector<Map<String, Object>>(currentBestMatches.values());
	reduceNumberOfMatchesIfAllowed(request, matches);

	for (Map<String, Object> match : matches) {
	    ServiceRealization sr = (ServiceRealization) match
		    .get(Constants.VAR_uAAL_SERVICE_TO_SELECT);
	    ServiceProfile profile = (ServiceProfile) sr
		    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
	}

	return matches;
    }

    /**
     * the strategy is to select the match with the lowest number of entries in
     * 'context' => first those services are preferred that are not used or
     * rated at all => after all have been called and rated at least once,
     * select those with lowest number of needed input, to produce shorter
     * messages
     * 
     * Comment added on 6.Oct.2009: the second argument above is not always the
     * best:
     * <ul>
     * <li>case 1: setBrightness(0) vs. turnOff()</li>
     * <li>case 2: getLampsByAbsLocation(loc) vs. getAllLamps()</li>
     * </ul>
     * where by class restrictions all lamps are in loc and generally, isn't it
     * better to postpone this decision to a later phase where we have gathered
     * all responses?
     */
    private void reduceNumberOfMatchesIfAllowed(ServiceRequest request,
	    List<Map<String, Object>> matches) {
	if (request.acceptsRandomSelection()) {
	    Map<String, Object> context = matches.remove(0);
	    for (Map<String, Object> match : matches) {
		if (match.size() < context.size()) {
		    context = match;
		}
	    }
	    matches.add(context);
	}
    }

    private Map<Object, Map<String, Object>> findBestMatches(
	    List<Map<String, Object>> matches) {
	Map<Object, Map<String, Object>> currentBestMatches = new Hashtable<Object, Map<String, Object>>();
	for (Map<String, Object> match : matches) {
	    ServiceRealization serviceRealization = (ServiceRealization) match
		    .get(Constants.VAR_uAAL_SERVICE_TO_SELECT);
	    if (serviceRealization
		    .assertServiceCall((Hashtable<String, Object>) match)) {
		Map<String, Object> otherMatch = currentBestMatches
			.get(serviceRealization.getProvider());
		if (otherMatch == null) {
		    currentBestMatches.put(serviceRealization.getProvider(),
			    match);
		} else {
		    if (matchesServiceURIExactly(match, otherMatch)) {
			currentBestMatches.put(
				serviceRealization.getProvider(), match);
		    } else if (!matchesServiceURIExactly(otherMatch, match)
			    && isOtherMatchPreferred(match, otherMatch)) {
			currentBestMatches.put(
				serviceRealization.getProvider(), match);
		    }
		}
	    }
	}
	return currentBestMatches;
    }

    /**
     * If two above are not true then either both services have matched their
     * URIs or none of them had. Either way regular strategy is applied.
     * 
     * The strategy: from each provider accept the one with more specialization
     * and then the one with the smallest context, which produces shorter
     * messages
     * 
     * TODO: is the above strategy ok? The issues are:
     * <ol>
     * <li>is instance-match specialization really more important than
     * class-match specialization?</li>
     * <li>2. is the length of messages a good criteria?</li>
     * </ol>
     */
    private boolean isOtherMatchPreferred(Map<String, Object> match,
	    Map<String, Object> otherMatch) {
	int sp0 = getSpecializationIndex(match);
	int sp1 = getSpecializationIndex(otherMatch);
	return sp1 < sp0 || (sp1 == sp0 && otherMatch.size() > match.size());
    }

    private int getSpecializationIndex(Map<String, Object> match) {
	int sp0 = Boolean.TRUE.equals(match
		.get(CONTEXT_SPECIALIZED_CLASS_MATCH)) ? 1 : 0;
	if (Boolean.TRUE.equals(match.get(CONTEXT_SPECIALIZED_INSTANCE_MATCH))) {
	    sp0 += 2;
	}
	return sp0;
    }

    /**
     * uAAL_SERVICE_URI_MATCHED:
     * 
     * New strategy: if service matches exactly URI specified in Service Request
     * than this service is always preferred over others.
     */
    private boolean matchesServiceURIExactly(Map<String, Object> match,
	    Map<String, Object> otherMatch) {
	return match.get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) != null
		&& otherMatch.get(ServiceRealization.uAAL_SERVICE_URI_MATCHED) == null;
    }

    private List<Map<String, Object>> matchingContextsForServiceRealizations(
	    BusMessage message, String serviceURI) {
	ServiceRequest request = (ServiceRequest) message.getContent();
	List<Map<String, Object>> matches = new Vector<Map<String, Object>>();
	synchronized (allServicesIndex) {
	    List<ServiceRealization> serviceRealizations = allServicesIndex
		    .get(serviceURI);
	    if (serviceRealizations == null) {
		sendNoMatchingFound(message);
	    } else {
		String caller = request.getProperty(
			ServiceRequest.PROP_uAAL_SERVICE_CALLER).toString();

		Long logID = Long.valueOf(Thread.currentThread().getId());
		LogUtils
			.logTrace(ServiceBusImpl.getModuleContext(),
				ServiceStrategy.class, "handle", new Object[] {
					ServiceBus.LOG_MATCHING_START,
					new UnmodifiableResource(request), " ",
					logID }, null);
		for (ServiceRealization serviceRealization : serviceRealizations) {
		    Service profileService = ((ServiceProfile) serviceRealization
			    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
			    .getTheService();
		    String profileServiceURI = profileService.getURI();

		    LogUtils.logTrace(ServiceBusImpl.getModuleContext(),
			    ServiceStrategy.class, "handle", new Object[] {
				    ServiceBus.LOG_MATCHING_PROFILE,
				    profileService.getType(),
				    profileServiceURI, logID }, null);
		    Map<String, Object> context = matches(caller, request,
			    serviceRealization, logID);
		    if (context != null) {
			matches.add(context);
			LogUtils.logTrace(ServiceBusImpl.getModuleContext(),
				ServiceStrategy.class, "handle",
				new Object[] { ServiceBus.LOG_MATCHING_SUCCESS,
					logID }, null);
		    } else {
			LogUtils.logTrace(ServiceBusImpl.getModuleContext(),
				ServiceStrategy.class, "handle", new Object[] {
					ServiceBus.LOG_MATCHING_NOSUCCESS,
					logID }, null);
		    }
		}
		LogUtils.logTrace(ServiceBusImpl.getModuleContext(),
			ServiceStrategy.class, "handle", new Object[] {
				ServiceBus.LOG_MATCHING_END, "found ",
				Integer.valueOf(matches.size()), " matches",
				logID }, null);
	    }
	}
	return matches;
    }

    private void createInitialServiceDialog(BusMessage message) {
	ServiceRequest request = (ServiceRequest) message.getContent();
	Service requestedService = request.getRequestedService();

	Object correlatedServiceClass = requestedService
		.getInstanceLevelFixedValueOnProp(UserInterfaceService.PROP_CORRELATED_SERVICE_CLASS);
	if (correlatedServiceClass instanceof Resource) {
	    Object vendor = requestedService
		    .getInstanceLevelFixedValueOnProp(UserInterfaceService.PROP_HAS_VENDOR);
	    if (request.getURI().startsWith(
		    UserInterfaceService.SERVICE_REQUEST_URI_PREFIX_INFO)) {
		synchronized (startDialogs) {
		    List<ServiceRealization> serviceRealizations = startDialogs
			    .get(correlatedServiceClass.toString());
		    if (vendor instanceof Resource) {
			replyToInitialDialogInfoRequest(message,
				serviceRealizations, vendor.toString());
		    } else {
			replyToInitialDialogInfoRequest(message,
				serviceRealizations);
		    }
		}
	    } else if (vendor instanceof Resource
		    && request
			    .getURI()
			    .startsWith(
				    UserInterfaceService.SERVICE_REQUEST_URI_PREFIX_START)) {
		synchronized (startDialogs) {
		    callStartDialog(startDialogs.get(correlatedServiceClass
			    .toString()), vendor.toString(), message);
		}
	    } else {
		sendNoMatchingFound(message);
	    }
	} else {
	    sendNoMatchingFound(message);
	}
    }

    private void handleP2PRequest(BusMessage msg) {
	Resource resource = (Resource) msg.getContent();
	if (resource instanceof ServiceCall) {
	    ServiceRealization serviceRealization = localServicesIndex
		    .getServiceRealizationByID(((ServiceCall) resource)
			    .getProcessURI());
	    if (serviceRealization != null) {
		ServiceCallee callee = (ServiceCallee) getBusMember(serviceRealization
			.getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER)
			.toString());
		if (callee != null) {
		    callee.handleRequest(msg);
		}
	    }
	} else if (isCoordinator && isServiceBusCoordinator(resource)) {
	    resource = new Resource(bus.getURI());
	    resource.addType(TYPE_uAAL_SERVICE_BUS_COORDINATOR, true);
	    ((ServiceBusImpl) bus).assessContentSerialization(resource);
	    send(msg.createReply(resource));
	} else if (isCoordinator
		&& resource.getType().equals(
			TYPE_uAAL_SERVICE_PROFILE_INFORMATION)) {

	    Resource r = new Resource();
	    String realizationID = (String) resource
		    .getProperty(PROP_uAAL_SERVICE_REALIZATION_ID);
	    r.addType(TYPE_uAAL_SERVICE_PROFILE_INFORMATION, true);
	    r.setProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE, Arrays
		    .asList(getCoordinatorServices(realizationID)));
	    r.setProperty(PROP_uAAL_SERVICE_REALIZATION_ID, realizationID);

	    ((ServiceBusImpl) bus).assessContentSerialization(r);

	    send(msg.createReply(r));
	}
    }

    private void handleP2PReply(BusMessage msg) {
	Resource resource = (Resource) msg.getContent();
	if (resource instanceof ServiceResponse) {
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
		BusMessage request = (BusMessage) callContext
			.get(CONTEXT_REQUEST_MESSAGE);
		Vector allCalls = (Vector) allWaitingCallers.get(request
			.getID());
		if (allCalls == null) {
		    // response already timed out => ignore this delayed one
		    // TODO: add a log entry
		    return;
		}
		synchronized (allCalls) {
		    callContext.put(CONTEXT_RESPONSE_MESSAGE, resource);
		    int pending = ((Integer) allCalls
			    .remove(allCalls.size() - 1)).intValue() - 1;
		    allCalls.add(new Integer(pending));
		    if (pending == 0) {
			sendServiceResponse(request);
		    }
		}
	    } else
	    // msg.hasReceiver was there at least since r2064
	    // (the first revision in saieds sandbox)
	    if (msg.hasReceiver(theCoordinator)) {
		send(msg);
	    } else {
		// this case shouldn't occur at all!
	    }
	} else if (isServiceBusCoordinator(resource)) {
	    PeerCard coord = AbstractBus.getPeerFromBusResourceURI(resource
		    .getURI());
	    if (theCoordinator == null && coord != null) {
		synchronized (this) {
		    theCoordinator = coord;
		    notifyOnFoundCoordinator();
		}
	    }
	} else if (resource.getType().equals(
		TYPE_uAAL_SERVICE_PROFILE_INFORMATION)) {
	    synchronized (this) {
		String realizationID = (String) resource
			.getProperty(PROP_uAAL_SERVICE_REALIZATION_ID);
		List profiles = (List) resource
			.getProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE);

		localServiceSearchResults.addProfiles(realizationID, profiles);

		notifyAll();
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private void handleP2PEvent(Resource resource) {
	if (isServiceBusSubscription(resource) && isCoordinator) {
	    if (isDeregistered(resource)) {
		String subscriber = resource.getURI();
		String serviceURI = resource
			.getProperty(PROP_uAAL_SERVICE_TYPE).toString();
		String requestURI = resource.getProperty(
			PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST).toString();
		synchronized (allServicesIndex) {
		    List<AvailabilitySubscription> subscriptions = allSubscriptionsIndex
			    .get(serviceURI);
		    if (subscriptions != null) {
			for (AvailabilitySubscription subscription : subscriptions) {
			    if (subscription.id.equals(subscriber)
				    && subscription.reqOrSubs.toString()
					    .equals(requestURI)) {
				subscriptions.remove(subscription);
				return;
			    }
			}
		    }
		}
	    } else {
		addSubscriber(resource.getURI(), (ServiceRequest) resource
			.getProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST));
	    }
	} else if (isServiceBusRegistration(resource) && isCoordinator) {
	    List<ServiceProfile> profiles = (List<ServiceProfile>) resource
		    .getProperty(PROP_uAAL_SERVICE_REGISTERED_PROFILE);
	    String theCallee = resource.getProperty(
		    PROP_uAAL_SERVICE_PROVIDED_BY).toString();
	    if (isRegistered(resource)) {
		for (ServiceProfile profile : profiles) {
		    indexServices(profile, new ServiceRealization(theCallee,
			    profile), profile.getProcessURI());
		}
	    } else if (profiles == null) {
		unindexServices(theCallee, null);
	    } else {
		for (ServiceProfile profile : profiles) {
		    unindexServices(theCallee, profile.getProcessURI());
		}
	    }
	} else if (isServiceBusCoordinator(resource)) {
	    PeerCard coord = AbstractBus.getPeerFromBusResourceURI(resource
		    .getURI());
	    if (theCoordinator == null && coord != null) {
		synchronized (this) {
		    theCoordinator = coord;
		    notifyOnFoundCoordinator();
		}
	    }
	}
    }

    private boolean isRegistered(Resource resource) {
	return RES_STATUS_REGISTERED.equals(resource
		.getProperty(PROP_uAAL_REGISTERATION_STATUS));
    }

    private boolean isDeregistered(Resource resource) {
	return RES_STATUS_DEREGISTERED.equals(resource
		.getProperty(PROP_uAAL_REGISTERATION_STATUS));
    }

    private boolean isServiceBusRegistration(Resource resource) {
	return resource.getType().equals(TYPE_uAAL_SERVICE_BUS_REGISTRATION);
    }

    private boolean isServiceBusCoordinator(Resource resource) {
	return resource.getType().equals(TYPE_uAAL_SERVICE_BUS_COORDINATOR);
    }

    private boolean isServiceBusSubscription(Resource resource) {
	return resource.getType().equals(TYPE_uAAL_SERVICE_BUS_SUBSCRIPTION);
    }

    private void handleEvent(Resource res) {
	if (res.getType().equals(TYPE_uAAL_SERVICE_BUS_NOTIFICATION)) {
	    notifyLocalSubscriber(res.getProperty(PROP_uAAL_SERVICE_SUBSCRIBER)
		    .toString(), res.getProperty(
		    PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST).toString(), res
		    .getProperty(PROP_uAAL_SERVICE_REALIZATION_ID).toString(),
		    isRegistered(res));
	}
    }

    /**
     * Sends a reply to the initial dialog info request message. The reply will
     * contain the matched services.
     * 
     * @param message
     *            - the initial dialog info request message
     * @param matchingServices
     */
    private void replyToInitialDialogInfoRequest(BusMessage message,
	    List<ServiceRealization> matchingServices) {
	if (matchingServices == null) {
	    sendNoMatchingFound(message);
	} else {
	    List<Service> matching = extractMatchingServices(matchingServices);

	    if (matching.isEmpty()) {
		sendNoMatchingFound(message);
	    } else {
		sendMatchingFound(message, matching);
	    }
	}
    }

    private void sendMatchingFound(BusMessage message, Object value) {
	ProcessOutput instanceInfo = new ProcessOutput(
		UserInterfaceService.OUTPUT_INSTANCE_INFO);
	instanceInfo.setParameterValue(value);
	ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	response.addOutput(instanceInfo);
	((ServiceBusImpl) bus).assessContentSerialization(response);
	message = message.createReply(response);
	if (message.receiverResidesOnDifferentPeer()) {
	    send(message);
	} else {
	    replyToLocalCaller(message);
	}
    }

    private List<Service> extractMatchingServices(
	    List<ServiceRealization> matchingServices) {
	List<Service> result = new Vector<Service>();
	for (ServiceRealization realization : matchingServices) {
	    if (realization != null) {
		ServiceProfile profile = (ServiceProfile) realization
			.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
		if (profile != null) {
		    Service service = profile.getTheService();
		    if (service != null) {
			result.add(service);
		    }
		}
	    }
	}
	return result;
    }

    /**
     * Sends a reply to the initial dialog info request message. The reply will
     * contain a description of a matched service of the vendor whose ID is
     * passed as a parameter
     * 
     * @param message
     *            - the initial dialog info request message
     * @param matchingServices
     */
    private void replyToInitialDialogInfoRequest(BusMessage message,
	    List<ServiceRealization> matchingServices, String vendor) {
	if (matchingServices == null) {
	    sendNoMatchingFound(message);
	} else {
	    Object description = extractServiceDescription(matchingServices,
		    vendor);

	    if (description == null) {
		sendNoMatchingFound(message);
	    } else {
		sendMatchingFound(message, description);
	    }
	}
    }

    private Object extractServiceDescription(
	    List<ServiceRealization> matchingServices, String vendor) {
	Object description = null;
	for (ServiceRealization realization : matchingServices) {
	    if (realization != null) {
		ServiceProfile profile = (ServiceProfile) realization
			.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
		if (profile != null) {
		    Service service = profile.getTheService();
		    if (service != null) {
			if (isMatchingVendor(vendor, service)) {
			    description = service
				    .getProperty(UserInterfaceService.PROP_DESCRIPTION);
			    if (description instanceof String) {
				break;
			    }
			}
		    }
		}
	    }
	}
	return description;
    }

    private boolean isMatchingVendor(String vendor, Service service) {
	return vendor.equals(String.valueOf(service
		.getProperty(UserInterfaceService.PROP_HAS_VENDOR)));
    }

    /**
     * Send the reply message to a local caller
     * 
     * @param msg
     *            - the reply message
     */
    private void replyToLocalCaller(BusMessage msg) {
	String replyOf = msg.getInReplyTo();
	if (replyOf == null) {
	    LogUtils
		    .logDebug(
			    ServiceBusImpl.getModuleContext(),
			    ServiceStrategy.class,
			    "replyToLocalCaller",
			    new Object[] { "Message of type REPLY, but not containing inReplyTo. Ignoring it." },
			    null);
	} else {
	    String callerID = localWaitingCallers
		    .getAndRemoveLocalWaiterCallerID(replyOf);
	    if (callerID == null) {
		LogUtils
			.logDebug(
				ServiceBusImpl.getModuleContext(),
				ServiceStrategy.class,
				"replyToLocalCaller",
				new Object[] { "There is no caller. To whom should I then reply? Ignoring it." },
				null);
	    } else {
		Object caller = getBusMember(callerID);
		if (caller instanceof ServiceCaller) {
		    ((ServiceCaller) caller).handleReply(msg);
		} else {
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
     * @param message
     *            - the message to send a reply to
     */
    private void sendNoMatchingFound(BusMessage message) {
	sendSimpleReply(message, CallStatus.noMatchingServiceFound);
    }

    /**
     * Send a denied! message as a reply to the message passed as a parameter
     * 
     * @param message
     *            the message to send a reply to
     */
    private void sendMessageDenied(BusMessage message) {
	sendSimpleReply(message, CallStatus.denied);
    }

    /**
     * Send a message of type <tt>status</tt> as a reply to the message passed
     * as parameter
     * 
     * @param message
     *            the message to reply to
     * @param status
     *            the {@link CallStatus} this reply should have
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
	if (theService == null) {
	    return;
	}
	if (theService instanceof InitialServiceDialog) {
	    Object correlService = theService
		    .getProperty(UserInterfaceService.PROP_CORRELATED_SERVICE_CLASS);
	    if (!(correlService instanceof Resource)) {
		// TODO: add a log entry
		return;
	    }
	    synchronized (startDialogs) {
		safeGet(startDialogs, correlService.toString()).add(
			registration);
	    }
	} else {
	    List<String> serviceURIs = getNonAbstractSuperClasses(theService);
	    synchronized (allServicesIndex) {
		for (String serviceURI : serviceURIs) {
		    safeGet(allServicesIndex, serviceURI).add(registration);
		    List<AvailabilitySubscription> subscribers = allSubscriptionsIndex
			    .get(serviceURI);
		    if (subscribers != null) {
			for (AvailabilitySubscription as : subscribers) {
			    if (null != matches(as.id,
				    (ServiceRequest) as.reqOrSubs, registration)) {
				notifySubscriber(as, processURI, true);
			    }
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
	    BusMessage message = new BusMessage(MessageType.p2p_request, r, bus);
	    send(message);
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
	} else {
	    return true;
	}
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
    private Map<String, Object> matches(String callerID,
	    ServiceRequest request, ServiceRealization offer) {
	return matches(callerID, request, offer, null);
    }

    /**
     * Returns the context in which the offer matches the request
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
    private Map<String, Object> matches(String callerID,
	    ServiceRequest request, ServiceRealization offer, Long logID) {
	Hashtable<String, Object> context = new Hashtable<String, Object>();
	context.put(ServiceBus.uAAL_SERVICE_BUS_MODULE_CONTEXT, busModule);
	context.put(Constants.VAR_uAAL_ACCESSING_BUS_MEMBER, callerID);
	context.put(Constants.VAR_uAAL_CURRENT_DATETIME, TypeMapper
		.getCurrentDateTime());
	context.put(Constants.VAR_uAAL_SERVICE_TO_SELECT, offer);
	Object involvedHumanUser = request
		.getProperty(Resource.PROP_uAAL_INVOLVED_HUMAN_USER);
	if (involvedHumanUser != null) {
	    context.put(Constants.VAR_uAAL_ACCESSING_HUMAN_USER,
		    involvedHumanUser);
	}
	return offer.matches(request, context, logID) ? context : null;
    }

    /**
     * Notify the Availability Subscribers about registration/unregistration of
     * Services (ServiceRealization representing the Services)
     * 
     * @param callerID
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

    private void notifyLocalSubscriber(String callerID, String request,
	    String realization, boolean registers) {
	List<AvailabilitySubscription> subscriptions = localSubscriptions
		.get(callerID);
	if (subscriptions != null) {
	    for (AvailabilitySubscription subscription : subscriptions) {
		if (request.equals(subscription.id)) {
		    if (registers) {
			((AvailabilitySubscriber) subscription.reqOrSubs)
				.serviceRegistered(request, realization);
		    } else {
			((AvailabilitySubscriber) subscription.reqOrSubs)
				.serviceUnregistered(request, realization);
		    }
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
	if (bus.isValidMember(as.callerID)) {
	    notifyLocalSubscriber(as.id, ((Resource) as.reqOrSubs).getURI(),
		    realizationID, registers);
	} else {
	    Resource resource = new Resource();
	    resource.addType(TYPE_uAAL_SERVICE_BUS_NOTIFICATION, true);
	    resource.setProperty(PROP_uAAL_REGISTERATION_STATUS,
		    (registers ? RES_STATUS_REGISTERED
			    : RES_STATUS_DEREGISTERED));
	    resource.setProperty(PROP_uAAL_SERVICE_REALIZATION_ID,
		    new Resource(realizationID));
	    resource.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER, new Resource(
		    as.id));
	    resource.setProperty(PROP_uAAL_SERVICE_SUBSCRIBER_REQUEST,
		    new Resource(((Resource) as.reqOrSubs).getURI()));
	    ((ServiceBusImpl) bus).assessContentSerialization(resource);
	    BusMessage message = new BusMessage(MessageType.event, resource,
		    bus);
	    message.setReceiver(AbstractBus
		    .getPeerFromBusResourceURI(as.callerID));
	    send(message);
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
		|| localSubscriptionsIndex.get(requestURI) == null) {
	    // TODO - check if the above line should be:
	    // localSubscriptionsIndex.get(callerID)...
	    return;
	}

	List<AvailabilitySubscription> subscriptions = localSubscriptions
		.get(callerID);
	if (subscriptions != null) {
	    for (AvailabilitySubscription subscription : subscriptions) {
		if (requestURI.equals(subscription.id)
			&& subscriber == subscription.reqOrSubs) {
		    subscriptions.remove(subscription);
		    break;
		}
	    }
	}

	String serviceURI = localSubscriptionsIndex.remove(requestURI);
	if (isCoordinator) {
	    List<AvailabilitySubscription> allSubscriptions = allSubscriptionsIndex
		    .get(serviceURI);
	    if (allSubscriptions != null) {
		for (AvailabilitySubscription subscription : allSubscriptions) {
		    if (callerID.equals(subscription.id)
			    && ((Resource) subscription.reqOrSubs).getURI()
				    .equals(requestURI)) {
			allSubscriptions.remove(subscription);
			break;
		    }
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
     *            - the URI of the ServiceCallee
     * @param realizedServices
     *            - the service profiles to remove
     */

    void removeMatchingRegParams(String calleeID,
	    ServiceProfile[] realizedServices) {
	if (realizedServices == null
		|| calleeID == null
		|| !(getBusMember(calleeID) instanceof ServiceCallee)) {
	    return;
	}

	for (ServiceProfile realizedService : realizedServices) {
	    if (realizedService == null) {
		continue;
	    }

	    String processURI = realizedService.getProcessURI();
	    if (processURI == null) {
		continue;
	    }

	    ServiceRealization reg = localServicesIndex
		    .removeServiceRealization(processURI);
	    if (!isMatchingServiceProvider(calleeID, reg)
		    || !processURI.equals(getProcessURIOfServiceProfile(reg))) {
		localServicesIndex.addServiceRealization(processURI, reg);
	    }

	    if (isCoordinator) {
		unindexServices(calleeID, processURI);
	    }
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
     *            - the URI of the callee for which the registration parameters
     *            are removed
     */
    void removeRegParams(String calleeID) {
	if (calleeID == null
		|| !(getBusMember(calleeID) instanceof ServiceCallee)) {
	    return;
	}

	String[] serviceRealizationsIds = localServicesIndex
		.getServiceRealizationIds();
	for (String id : serviceRealizationsIds) {
	    ServiceRealization serviceRealization = localServicesIndex
		    .getServiceRealizationByID(id);
	    if (isMatchingServiceProvider(calleeID, serviceRealization)) {
		localServicesIndex.removeServiceRealization(id);
	    }
	}

	if (isCoordinator) {
	    unindexServices(calleeID, null);
	} else if (isCoordinatorKnown()) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_SERVICE_BUS_REGISTRATION, true);
	    r.setProperty(PROP_uAAL_REGISTERATION_STATUS,
		    RES_STATUS_DEREGISTERED);
	    r
		    .setProperty(PROP_uAAL_SERVICE_PROVIDED_BY, new Resource(
			    calleeID));
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
     *            - the URI of the caller
     * @param processURI
     *            - the URI of the process
     */

    private void unindexServices(String calleeID, String processURI) {

	boolean deleteAll = (processURI == null);
	synchronized (allServicesIndex) {
	    for (List<ServiceRealization> element : allServicesIndex.values()) {
		for (ServiceRealization reg : element) {
		    if (isMatchingServiceProvider(calleeID, reg)) {
			if (deleteAll) {
			    processURI = getProcessURIOfServiceProfile(reg);
			} else if (processURI
				.equals(getProcessURIOfServiceProfile(reg))) {
			    continue;
			}

			element.remove(reg);
			String serviceURI = getServiceURIOfServiceProfile(reg);
			List<AvailabilitySubscription> subscribers = allSubscriptionsIndex
				.get(serviceURI);
			if (subscribers != null) {
			    for (AvailabilitySubscription subscriber : subscribers) {
				if (null != matches(subscriber.id,
					(ServiceRequest) subscriber.reqOrSubs,
					reg)) {
				    notifySubscriber(subscriber, processURI,
					    false);
				}
			    }
			}
		    }
		}
	    }
	}
    }

    private String getServiceURIOfServiceProfile(ServiceRealization reg) {
	return ((ServiceProfile) reg
		.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
		.getTheService().getClassURI();
    }

    private String getProcessURIOfServiceProfile(ServiceRealization reg) {
	return ((ServiceProfile) reg
		.getProperty(ServiceRealization.uAAL_SERVICE_PROFILE))
		.getProcessURI();
    }

    private boolean isMatchingServiceProvider(String calleeID,
	    ServiceRealization reg) {
	return calleeID.equals(reg
		.getProperty(ServiceRealization.uAAL_SERVICE_PROVIDER));
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
	if (isCoordinator) {
	    return getCoordinatorServices(serviceURI);
	}

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
	    while (!localServiceSearchResults.exist(serviceURI)
		    && maxRetry > retryCount) {
		try {
		    wait(msTimeout);
		    retryCount++;
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}

	List profiles = localServiceSearchResults.getProfiles(serviceURI);
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
	List<ServiceProfile> profiles = new ArrayList<ServiceProfile>();

	if (isCoordinator) {
	    List<ServiceRealization> neededProfiles = allServicesIndex
		    .get(serviceURI);
	    if (neededProfiles != null) {
		for (ServiceRealization realization : neededProfiles) {
		    ServiceProfile profile = (ServiceProfile) realization
			    .getProperty(ServiceRealization.uAAL_SERVICE_PROFILE);
		    if (profile != null) {
			profiles.add(profile);
		    }
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
     *            - the list to translate
     * @return ServiceProfile[] - the translated array of ServiceProfiles
     */
    private ServiceProfile[] profileListToArray(List<ServiceProfile> list) {
	if (list != null) {
	    ServiceProfile[] result = new ServiceProfile[list.size()];
	    int index = 0;
	    for (ServiceProfile profile : list) {
		result[index++] = profile;
	    }

	    return result;
	} else {
	    return new ServiceProfile[0];
	}
    }
}
