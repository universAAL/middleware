/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.context.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.BusStrategy;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageType;

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

    private class ContextFilterer {
	ContextSubscriber s;
	ContextEventPattern f;
    }

    private Hashtable provisions, numCalledPeers, allPropsOfDomain,
	    allPropsOfSubject, allSubjectsWithProp, specificDomainAndProp,
	    specificSubjectAndProp;
    private Vector allProvisions, notIndexedFilterers;

    public ContextStrategy(SodaPop sodapop) {
	super(sodapop);
	provisions = new Hashtable();
	numCalledPeers = new Hashtable();
	allPropsOfDomain = new Hashtable();
	allPropsOfSubject = new Hashtable();
	allSubjectsWithProp = new Hashtable();
	specificDomainAndProp = new Hashtable();
	specificSubjectAndProp = new Hashtable();
	notIndexedFilterers = new Vector();
	allProvisions = new Vector();
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
		|| provisions.containsKey(publisher))
	    return;

	provisions.put(publisher, providedEvents);
	allProvisions.addAll(Arrays.asList(providedEvents));
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
	    for (int j = 0; j < filterers.size(); j++)
		((Vector) filterers.get(j)).add(filterer);
	}
    }

    ContextEventPattern[] getAllProvisions(ContextSubscriber cs) {
	if (cs == null)
	    return null;

	// to simplify the implementation, parallel calls by the same subscriber
	// are handled sequentially by synchronizing on the subscriber
	synchronized (cs) {
	    // ask all peers to send their provisions and then add this
	    // instance's own provisions
	    // create a vector for collecting peer responses
	    Vector v = new Vector();
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_CONTEXT_BUS_PROVISIONS, true);
	    ContextBusImpl.assessContentSerialization(r);
	    Message m = new Message(MessageType.p2p_request, r);
	    int numPeers = sodapop.propagateMessage(bus, m);
	    if (numPeers > 0) {
		Integer latePeers = null;
		synchronized (v) {
		    // in #handle() the vector is found using the ID of the
		    // message sent to the peers
		    numCalledPeers.put(m.getID(), v);
		    // Contract: the first element in v is the number of
		    // responses waiting for
		    // in #handle() this number is reduced for each response
		    // received from the peers
		    v.add(new Integer(numPeers));
		    try {
			// if all responses are received before the 5 seconds
			// here are elapsed, then the last one notifies this
			// thread not to wait any more!
			v.wait(5000);
		    } catch (Exception e) {
		    }
		    if (v.get(0) instanceof Integer)
			// if the first element is still an integer, it
			// indicates the number of responses not received
			// it also means that we received no notification but
			// the whole 5 seconds were elapsed
			latePeers = (Integer) v.remove(0);
		    // after timeout resp. notify, the map entry must be removed
		    numCalledPeers.remove(m.getID());
		}
		if (latePeers != null && latePeers.intValue() > 0)
		    LogUtils
			    .logWarn(
				    ContextBusImpl.moduleContext,
				    getClass(),
				    "getAllProvisions",
				    new Object[] { latePeers,
					    " peers have still not replied to the query after 5 seconds waiting!" },
				    null);
	    }
	    v.addAll(allProvisions);
	    return (ContextEventPattern[]) v.toArray(new ContextEventPattern[v
		    .size()]);
	}
    }

    private Vector getFilterers(ContextEventPattern f) {
	Vector result = new Vector();
	String[] props = f.getIndices().getProperties();
	String[] subjects = f.getIndices().getSubjects();
	String[] subjectTypes = f.getIndices().getSubjectTypes();
	if (subjects.length == 0)
	    if (subjectTypes.length == 0)
		if (props.length == 0)
		    result.add(notIndexedFilterers);
		else
		    for (int i = 0; i < props.length; i++)
			result.add(getFilterers(allSubjectsWithProp, props[i]));
	    else if (props.length == 0)
		for (int i = 0; i < subjectTypes.length; i++)
		    result.add(getFilterers(allPropsOfDomain, subjectTypes[i]));
	    else
		for (int i = 0; i < subjectTypes.length; i++)
		    for (int j = 0; j < props.length; j++)
			result.add(getFilterers(specificDomainAndProp,
				subjectTypes[i] + COMPOUND_INDEX_CONNECTOR
					+ props[j]));
	else if (props.length == 0)
	    for (int i = 0; i < subjects.length; i++)
		result.add(getFilterers(allPropsOfSubject, subjects[i]));
	else
	    for (int i = 0; i < subjects.length; i++)
		for (int j = 0; j < props.length; j++)
		    result.add(getFilterers(specificSubjectAndProp, subjects[i]
			    + COMPOUND_INDEX_CONNECTOR + props[j]));
	return result;
    }

    private Vector getFilterers(Hashtable t, String k) {
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
	Object o = msg.getContent();
	switch (msg.getType().ord()) {
	case MessageType.EVENT:
	    if (!(o instanceof ContextEvent)) {
		LogUtils
			.logWarn(
				ContextBusImpl.moduleContext,
				getClass(),
				"handle",
				new Object[] { "Event to handle is no instance of ContextEvent!" },
				null);
		return;
	    }

	    if (!msg.isRemote())
		sodapop.propagateMessage(bus, msg);

	    HashSet allSubscribers = new HashSet();
	    ContextEvent event = (ContextEvent) o;
	    String propertyURI = event.getRDFPredicate(),
	    subjectURI = event.getSubjectURI(),
	    subjectTypeURI = event.getSubjectTypeURI();

	    Vector filterers = (Vector) specificSubjectAndProp.get(subjectURI
		    + COMPOUND_INDEX_CONNECTOR + propertyURI);
	    if (filterers != null)
		for (int i = 0; i < filterers.size(); i++)
		    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			allSubscribers
				.add(((ContextFilterer) filterers.get(i)).s);

	    filterers = (Vector) specificDomainAndProp.get(subjectTypeURI
		    + COMPOUND_INDEX_CONNECTOR + propertyURI);
	    if (filterers != null)
		for (int i = 0; i < filterers.size(); i++)
		    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			allSubscribers
				.add(((ContextFilterer) filterers.get(i)).s);

	    filterers = (Vector) allPropsOfSubject.get(subjectURI);
	    if (filterers != null)
		for (int i = 0; i < filterers.size(); i++)
		    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			allSubscribers
				.add(((ContextFilterer) filterers.get(i)).s);

	    filterers = (Vector) allPropsOfDomain.get(subjectTypeURI);
	    if (filterers != null)
		for (int i = 0; i < filterers.size(); i++)
		    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			allSubscribers
				.add(((ContextFilterer) filterers.get(i)).s);

	    filterers = (Vector) allSubjectsWithProp.get(propertyURI);
	    if (filterers != null)
		for (int i = 0; i < filterers.size(); i++)
		    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			allSubscribers
				.add(((ContextFilterer) filterers.get(i)).s);

	    for (int i = 0; i < notIndexedFilterers.size(); i++)
		if (((ContextFilterer) notIndexedFilterers.get(i)).f
			.matches(event))
		    allSubscribers.add(((ContextFilterer) notIndexedFilterers
			    .get(i)).s);

	    for (Iterator i = allSubscribers.iterator(); i.hasNext();)
		((ContextSubscriber) i.next()).handleEvent(msg);
	    break;
	case MessageType.P2P_EVENT:
	    break;
	case MessageType.P2P_REPLY:
	    if (o instanceof Resource
		    && TYPE_uAAL_CONTEXT_BUS_PROVISIONS.equals(((Resource) o)
			    .getType())) {
		o = ((Resource) o)
			.getProperty(PROP_uAAL_CONTEXT_PEER_PROVISIONS);
		if (o instanceof List && !((List) o).isEmpty()) {
		    Vector v = (Vector) numCalledPeers.get(msg.getInReplyTo());
		    if (v == null) {
			LogUtils
				.logDebug(
					ContextBusImpl.moduleContext,
					getClass(),
					"handle",
					new Object[] { "Ignoring peer provisions received after timeout!" },
					null);
		    } else {
			synchronized (v) {
			    v.addAll((List) o);
			    o = v.remove(0);
			    if (o instanceof Integer) {
				int remaining = ((Integer) o).intValue();
				if (remaining > 1)
				    v.insertElementAt(
					    new Integer(remaining - 1), 0);
				else
				    v.notify();
			    }
			}
		    }
		} else
		    LogUtils
			    .logDebug(
				    ContextBusImpl.moduleContext,
				    getClass(),
				    "handle",
				    new Object[] { "Ignoring a P2P-Reply not containing any peer provisions!" },
				    null);
	    } else
		LogUtils
			.logWarn(
				ContextBusImpl.moduleContext,
				getClass(),
				"handle",
				new Object[] { "P2P-Reply to handle does not contain peer provisions!" },
				null);
	    break;
	case MessageType.P2P_REQUEST:
	    if (o instanceof Resource
		    && TYPE_uAAL_CONTEXT_BUS_PROVISIONS.equals(((Resource) o)
			    .getType())
		    && ((Resource) o).numberOfProperties() == 1) {
		((Resource) o).setProperty(PROP_uAAL_CONTEXT_PEER_PROVISIONS,
			allProvisions);
		sodapop.propagateMessage(bus, msg.createReply(o));
	    } else
		LogUtils
			.logWarn(ContextBusImpl.moduleContext, getClass(),
				"handle",
				new Object[] { "Unknown P2P-Request!" }, null);
	    break;
	case MessageType.REPLY:
	    LogUtils.logWarn(ContextBusImpl.moduleContext, getClass(),
		    "handle",
		    new Object[] { "Unexpected Reply message ignored!" }, null);
	    break;
	case MessageType.REQUEST:
	    LogUtils.logWarn(ContextBusImpl.moduleContext, getClass(),
		    "handle",
		    new Object[] { "Unexpected Request message ignored!" },
		    null);
	    break;
	default:
	    LogUtils.logWarn(ContextBusImpl.moduleContext, getClass(),
		    "handle",
		    new Object[] { "Message of unknown type ignored!" }, null);
	    break;
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

	    for (Iterator j = filterers.iterator(); j.hasNext();)
		for (Iterator k = ((Vector) j.next()).iterator(); k.hasNext();)
		    if (((ContextFilterer) k.next()).s == subscriber)
			j.remove();
	}
    }

    private void remove(ContextSubscriber subscriber, Vector filterers) {
	for (Iterator i = filterers.iterator(); i.hasNext();)
	    if (((ContextFilterer) i.next()).s == subscriber)
		i.remove();
    }

    private void remove(ContextSubscriber subscriber, Hashtable filterers) {
	for (Iterator i = filterers.values().iterator(); i.hasNext();)
	    remove(subscriber, (Vector) i.next());
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

	remove(subscriber, notIndexedFilterers);

	remove(subscriber, allSubjectsWithProp);

	remove(subscriber, allPropsOfSubject);

	remove(subscriber, specificSubjectAndProp);

	remove(subscriber, allPropsOfDomain);

	remove(subscriber, specificDomainAndProp);
    }

}
