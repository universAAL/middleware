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
package org.universAAL.middleware.context.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.data.IAllProvisionData;
import org.universAAL.middleware.context.data.ICalledPeers;
import org.universAAL.middleware.context.data.IFiltererContainer;
import org.universAAL.middleware.context.data.INumCalledPeersData;
import org.universAAL.middleware.context.data.IPropsData;
import org.universAAL.middleware.context.data.IProvisionsData;
import org.universAAL.middleware.context.data.factory.ContextStrategyDataFactory;
import org.universAAL.middleware.context.data.factory.IContextStrategyDataFactory;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ContextStrategy extends BusStrategy {

    private static final String COMPOUND_INDEX_CONNECTOR = "";

    private static final String PROP_uAAL_CONTEXT_PEER_PROVISIONS = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "myContextProvisions";

    private static final String TYPE_uAAL_CONTEXT_BUS_PROVISIONS = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "ContextProvisions";

    public static class ContextFilterer {
	public ContextSubscriber s;
	public ContextEventPattern f;
    }

    private IProvisionsData provisions;
    private INumCalledPeersData numCalledPeers;
    private IPropsData allPropsOfDomain;
    private IPropsData allPropsOfSubject;
    private IPropsData allSubjectsWithProp;
    private IPropsData specificDomainAndProp;
    private IPropsData specificSubjectAndProp;
    private IPropsData notIndexedProps;
    private IAllProvisionData allProvisions;

    public ContextStrategy(CommunicationModule cm) {
	super(cm);

	// Initiated the factory
	IContextStrategyDataFactory factory = createContextStrategyDataFactory();

	provisions = factory.createProvisionsData();
	numCalledPeers = factory.createNumCalledPeersData();
	allPropsOfDomain = factory.createAllPropsOfDomain();
	allPropsOfSubject = factory.createAllPropsOfSubject();
	allSubjectsWithProp = factory.createAllSubjectsWithProp();
	specificDomainAndProp = factory.createSpecificDomainAndProp();
	specificSubjectAndProp = factory.createSpecificSubjectAndProp();
	notIndexedProps = factory.createNonIndexedProps();
	allProvisions = factory.createAllProvisions();
    }

    protected IContextStrategyDataFactory createContextStrategyDataFactory() {
	return new ContextStrategyDataFactory();
    }

    /**
     * Allows a Context Publisher to announce which events it is going to
     * publish during its membership at context bus.
     * 
     * @param publisher
     *            The Publisher that wants to announce Patterns
     * @param providedEvents
     *            An array of ConntextEventPattern that define the classes of
     *            context events expected to be provided by the given publisher
     */
    void addRegParams(ContextPublisher publisher,
	    ContextEventPattern[] providedEvents) {
	if (providedEvents == null || publisher == null
		|| provisions.exist(publisher))
	    return;

	provisions.addProvision(publisher);
	allProvisions.addContextEventPatterns(Arrays.asList(providedEvents));
    }

    /**
     * * Allows a Context Subscriber to register to events in the bus that match
     * the given patterns
     * 
     * @param subscriber
     *            The Subscriber that wants to register Patterns
     * @param initialSubscriptions
     *            An array of ConntextEventPattern containing the restrictions
     *            on Context Events that define the patterns to register to
     */
    void addRegParams(ContextSubscriber subscriber,
	    ContextEventPattern[] initialSubscriptions) {
	if (initialSubscriptions == null || subscriber == null)
	    return;

	for (int i = 0; i < initialSubscriptions.length; i++) {
	    ContextFilterer filterer = new ContextFilterer();
	    filterer.s = subscriber;
	    filterer.f = initialSubscriptions[i];

	    Vector filterers = getFilterers(filterer.f);
	    for (int j = 0; j < filterers.size(); j++) {
		IFiltererContainer container = (IFiltererContainer) filterers
			.get(j);
		container.addFilterer(filterer);
	    }
	}
    }

    ContextEventPattern[] getAllProvisions(ContextPublisher cs) {
	if (cs == null)
	    return null;

	// to simplify the implementation, parallel calls by the same subscriber
	// are handled sequentially by synchronizing on the subscriber
	synchronized (cs) {
	    // ask all peers to send their provisions and then add this
	    // instance's own provisions
	    // create a CalledPeers for collecting peer responses
	    ICalledPeers calledPeers = createContextStrategyDataFactory()
		    .createCalledPeers();
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_CONTEXT_BUS_PROVISIONS, true);
	    ((ContextBusImpl) bus).assessContentSerialization(r);
	    BusMessage message = new BusMessage(MessageType.p2p_request, r, bus);
	    send(message);
	    int numPeers = AbstractBus.getCurrentNumberOfPeers();
	    if (numPeers > 0) {
		Integer latePeers = null;
		synchronized (calledPeers) {
		    // in #handle() the vector is found using the ID of the
		    // message sent to the peers
		    numCalledPeers.addCalledPeers(message.getID(), calledPeers);
		    // Set the the number of responses waiting for
		    // in #handle() this number is reduced for each response
		    // received from the peers
		    calledPeers.setNumOfCalledPeers(numPeers);
		    try {
			// if all responses are received before the 5 seconds
			// here are elapsed, then the last one notifies this
			// thread not to wait any more!
			calledPeers.wait(5000);
		    } catch (Exception e) {
		    }

		    if (!calledPeers.gotResponsesFromAllPeers()) {
			// Still no all responses were received
			// it also means that we received no notification but
			// the whole 5 seconds were elapsed
			latePeers = new Integer(calledPeers
				.getNumOfCalledPeers());
		    }
		    // after timeout resp. notify, the map entry must be removed
		    numCalledPeers.removeCalledPeers(message.getID());
		}
		if (latePeers != null && latePeers.intValue() > 0) {
		    LogUtils
			    .logWarn(
				    busModule,
				    getClass(),
				    "getAllProvisions",
				    new Object[] { latePeers,
					    " peers have still not replied to the query after 5 seconds waiting!" },
				    null);
		}
	    }
	    calledPeers.addProvisions(allProvisions.getContextEventPatterns());
	    return (ContextEventPattern[]) calledPeers.getProvisions()
		    .toArray(
			    new ContextEventPattern[calledPeers.getProvisions()
				    .size()]);
	}
    }

    private String[] getSubClasses(String[] superClasses) {
	if (superClasses.length == 0)
	    return null;

	Set s = new HashSet();
	for (int i = 0; i < superClasses.length; i++) {
	    Set stemp = OntologyManagement.getInstance().getNamedSubClasses(
		    superClasses[i], true, false);
	    if (stemp != null)
		s.addAll(stemp);
	    s.add(superClasses[i]);
	}

	return (String[]) s.toArray(new String[] {});
    }

    private Vector getFilterers(ContextEventPattern f) {
	Vector result = new Vector();
	String[] props = f.getIndices().getProperties();
	String[] subjects = f.getIndices().getSubjects();
	String[] subjectTypes = f.getIndices().getSubjectTypes();

	if (subjects.length == 0) {
	    if (subjectTypes.length == 0) {
		if (props.length == 0) {
		    result.add(notIndexedProps.getFiltererContainer(""));
		} else {
		    for (int i = 0; i < props.length; i++) {
			result.add(allSubjectsWithProp
				.getFiltererContainer(props[i]));
		    }
		}
	    } else if (props.length == 0) {
		String[] subjectTypesSubClasses = getSubClasses(subjectTypes);
		for (int i = 0; i < subjectTypesSubClasses.length; i++) {
		    result.add(allPropsOfDomain
			    .getFiltererContainer(subjectTypesSubClasses[i]));
		}
	    } else {
		String[] subjectTypesSubClasses = getSubClasses(subjectTypes);
		for (int i = 0; i < subjectTypesSubClasses.length; i++) {
		    for (int j = 0; j < props.length; j++) {
			result.add(specificDomainAndProp
				.getFiltererContainer(subjectTypesSubClasses[i]
					+ COMPOUND_INDEX_CONNECTOR + props[j]));
		    }
		}
	    }
	} else if (props.length == 0) {
	    for (int i = 0; i < subjects.length; i++) {
		result.add(allPropsOfSubject.getFiltererContainer(subjects[i]));
	    }
	} else {
	    for (int i = 0; i < subjects.length; i++) {
		for (int j = 0; j < props.length; j++) {
		    result.add(specificSubjectAndProp
			    .getFiltererContainer(subjects[i]
				    + COMPOUND_INDEX_CONNECTOR + props[j]));
		}
	    }
	}
	return result;
    }

    protected void handleDeniedMessage(BusMessage message, String senderID) {
	// TODO Auto-generated method stub
    }

    /**
     * @see BusStrategy#handleMessage(BusMessage, String)
     */
    public void handle(BusMessage message, String senderID) {
	switch (message.getType().ord()) {
	case MessageType.EVENT:
	    handleEvent(message);
	    break;
	case MessageType.P2P_EVENT:
	    LogUtils.logWarn(busModule, getClass(), "handle",
		    new Object[] { "Unexpected P2P_EVENT message ignored!" },
		    null);
	    break;
	case MessageType.P2P_REPLY:
	    handleP2PReply(message);
	    break;
	case MessageType.P2P_REQUEST:
	    handleP2PRequest(message);
	    break;
	case MessageType.REPLY:
	    LogUtils.logWarn(busModule, getClass(), "handle",
		    new Object[] { "Unexpected Reply message ignored!" }, null);
	    break;
	case MessageType.REQUEST:
	    LogUtils.logWarn(busModule, getClass(), "handle",
		    new Object[] { "Unexpected Request message ignored!" },
		    null);
	    break;
	default:
	    LogUtils.logWarn(busModule, getClass(), "handle",
		    new Object[] { "Message of unknown type ignored!" }, null);
	    break;
	}
    }

    private void handleP2PRequest(BusMessage message) {
	Object content = message.getContent();
	if (content instanceof Resource) {
	    Resource resource = (Resource) content;
	    if (isContextBusProvisionList(resource)
		    && resource.numberOfProperties() == 1) {
		resource.setProperty(PROP_uAAL_CONTEXT_PEER_PROVISIONS,
			allProvisions.getContextEventPatterns());
		send(message.createReply(content));
	    }
	} else {
	    LogUtils.logWarn(busModule, getClass(), "handle",
		    new Object[] { "Unknown P2P-Request!" }, null);
	}
    }

    private void handleP2PReply(BusMessage message) {
	Object content = message.getContent();
	if (content instanceof Resource) {
	    Resource resource = (Resource) content;
	    if (isContextBusProvisionList(resource)) {
		Object provisionList = resource
			.getProperty(PROP_uAAL_CONTEXT_PEER_PROVISIONS);
		if (provisionList instanceof List
			&& !((List) provisionList).isEmpty()) {
		    ICalledPeers calledPeers = numCalledPeers
			    .getCalledPeers(message.getInReplyTo());
		    if (calledPeers != null) {
			List<ContextEventPattern> contextEventPatterns = (List<ContextEventPattern>) provisionList;
			synchronized (calledPeers) {
			    calledPeers.addProvisions(contextEventPatterns);
			    if (!calledPeers.gotResponsesFromAllPeers()) {
				int remaining = calledPeers
					.getNumOfCalledPeers();
				if (remaining > 1) {
				    calledPeers.reduceNumOfCalledPeers();
				} else {
				    calledPeers.notify();
				}
			    }
			}
		    } else {
			LogUtils
				.logDebug(
					busModule,
					getClass(),
					"handle",
					new Object[] { "Ignoring peer provisions received after timeout!" },
					null);
		    }
		} else {
		    LogUtils
			    .logDebug(
				    busModule,
				    getClass(),
				    "handle",
				    new Object[] { "Ignoring a P2P-Reply not containing any peer provisions!" },
				    null);
		}
	    }
	} else {
	    LogUtils
		    .logWarn(
			    busModule,
			    getClass(),
			    "handle",
			    new Object[] { "P2P-Reply to handle does not contain peer provisions!" },
			    null);
	}
    }

    private boolean isContextBusProvisionList(Resource resource) {
	return TYPE_uAAL_CONTEXT_BUS_PROVISIONS.equals(resource.getType());
    }

    private void handleEvent(BusMessage message) {
	if (!(message.getContent() instanceof ContextEvent)) {
	    LogUtils
		    .logWarn(
			    busModule,
			    getClass(),
			    "handle",
			    new Object[] { "Event to handle is no instance of ContextEvent!" },
			    null);
	    return;
	}

	if (!message.senderResidesOnDifferentPeer())
	    send(message);
	notifyAllLocalSubscribers(message);
    }

    // the message content must be a ContextEvent
    private void notifyAllLocalSubscribers(BusMessage message) {
	ContextEvent event = (ContextEvent) message.getContent();
	Set<ContextSubscriber> subscribers = getAllSubscribersOfEvent(event);
	for (ContextSubscriber subscriber : subscribers)
	    subscriber.handleEvent(message);
    }

    private Set<ContextSubscriber> getAllSubscribersOfEvent(ContextEvent event) {
	Set<ContextSubscriber> subscribers = new HashSet<ContextSubscriber>();

	String propertyURI = event.getRDFPredicate();
	String subjectURI = event.getSubjectURI();
	String subjectTypeURI = event.getSubjectTypeURI();
	String specificSubjectProperty = subjectURI + COMPOUND_INDEX_CONNECTOR
		+ propertyURI;
	String specificDomainProperty = subjectTypeURI
		+ COMPOUND_INDEX_CONNECTOR + propertyURI;

	addSubscribersForFilter(specificSubjectAndProp,
		specificSubjectProperty, event, subscribers);
	addSubscribersForFilter(specificDomainAndProp, specificDomainProperty,
		event, subscribers);
	addSubscribersForFilter(allPropsOfSubject, subjectURI, event,
		subscribers);
	addSubscribersForFilter(allPropsOfDomain, subjectTypeURI, event,
		subscribers);
	addSubscribersForFilter(allSubjectsWithProp, propertyURI, event,
		subscribers);

	List<ContextFilterer> notIndexedPropsFilterers = notIndexedProps
		.getFiltererContainer("").getFilterers();
	addSubscribersForMatchingFilterers(notIndexedPropsFilterers, event,
		subscribers);

	return subscribers;
    }

    private void addSubscribersForFilter(IPropsData propsData, String filter,
	    ContextEvent event, Set<ContextSubscriber> allSubscribers) {
	IFiltererContainer filtererContainer = propsData
		.getFiltererContainer(filter);
	if (filtererContainer != null) {
	    addSubscribersForMatchingFilterers(
		    filtererContainer.getFilterers(), event, allSubscribers);
	}
    }

    private void addSubscribersForMatchingFilterers(
	    List<ContextFilterer> notIndexedPropsFilterers, ContextEvent event,
	    Set<ContextSubscriber> subscribers) {
	for (ContextFilterer filterer : notIndexedPropsFilterers) {
	    if (filterer.f.matches(event)) { // NOTE
		subscribers.add(filterer.s);
	    }
	}
    }

    /**
     * Remove the patterns of Context Events that a Context Subscriber is
     * interested in, so it no longer receives Events matching them
     * 
     * @param subscriber
     *            The Subscriber that wants to remove its Patterns
     * @param initialSubscriptions
     *            An array of ConntextEventPattern containing the restrictions
     *            on Context Events that define the patterns to unregister. Must
     *            be equal to those registered at first.
     */
    void removeMatchingRegParams(ContextSubscriber subscriber,
	    ContextEventPattern[] initialSubscriptions) {
	if (initialSubscriptions == null || subscriber == null)
	    return;

	for (int i = 0; i < initialSubscriptions.length; i++) {
	    Vector filterers = getFilterers(initialSubscriptions[i]);
	    if (filterers == null)
		continue;

	    for (Iterator j = filterers.iterator(); j.hasNext();) {
		IFiltererContainer container = (IFiltererContainer) j.next();

		container.removeFilterers(subscriber);
	    }
	}
    }

    void removeMatchingRegParams(ContextPublisher publisher,
	    ContextEventPattern[] oldParams) {
	// TODO:
    }

    void removeRegParams(ContextPublisher publisher) {
	// TODO:
    }

    private void remove(ContextSubscriber subscriber, IPropsData propsData) {
	for (Iterator i = propsData.getAllFiltererContainers().iterator(); i
		.hasNext();) {
	    IFiltererContainer container = (IFiltererContainer) i.next();
	    container.removeFilterers(subscriber);
	}
    }

    /**
     * Remove ALL patterns of Context Events that a Context Subscriber is
     * interested in, so it no longer receives Events OF ANY KIND
     * 
     * @param subscriber
     *            The Subscriber that wants to remove its Patterns
     */
    void removeRegParams(ContextSubscriber subscriber) {
	if (subscriber == null)
	    return;

	remove(subscriber, notIndexedProps);

	remove(subscriber, allSubjectsWithProp);

	remove(subscriber, allPropsOfSubject);

	remove(subscriber, specificSubjectAndProp);

	remove(subscriber, allPropsOfDomain);

	remove(subscriber, specificDomainAndProp);
    }
}
