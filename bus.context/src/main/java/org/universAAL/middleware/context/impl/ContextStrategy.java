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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.rdf.ContextEvent;
import org.universAAL.middleware.context.rdf.ContextEventPattern;
import org.universAAL.middleware.sodapop.BusStrategy;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageType;


/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * 
 */
public class ContextStrategy extends BusStrategy {
	
	private static final String COMPOUND_INDEX_CONNECTOR = "";

    private class ContextFilterer {
        ContextSubscriber s;
        ContextEventPattern f;
    } 
    
	private Hashtable allPropsOfDomain, allPropsOfSubject, allSubjectsWithProp,
						specificDomainAndProp, specificSubjectAndProp;
	private Vector notIndexedFilterers;

	public ContextStrategy(SodaPop sodapop) {
		super(sodapop);
		allPropsOfDomain = new Hashtable();
		allPropsOfSubject = new Hashtable();
		allSubjectsWithProp = new Hashtable();
		specificDomainAndProp = new Hashtable();
		specificSubjectAndProp = new Hashtable();
		notIndexedFilterers = new Vector();
	}

	void addRegParams(ContextSubscriber subscriber, ContextEventPattern[] initialSubscriptions) {
		if (initialSubscriptions == null || subscriber == null)
			return;

		for (int i=0; i<initialSubscriptions.length; i++) {
			ContextFilterer filterer = new ContextFilterer();
			filterer.s = subscriber;
			filterer.f = initialSubscriptions[i];
			
			Vector filterers = getFilterers(filterer.f);
			for (int j=0; j<filterers.size(); j++)
				((Vector) filterers.get(j)).add(filterer);
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
					for (int i=0; i<props.length; i++)
						result.add(getFilterers(allSubjectsWithProp, props[i]));
			else if (props.length == 0)
				for (int i=0; i<subjectTypes.length; i++)
					result.add(getFilterers(allPropsOfDomain, subjectTypes[i]));
			else
				for (int i=0; i<subjectTypes.length; i++)
					for (int j=0; j<props.length; j++)
						result.add(getFilterers(specificDomainAndProp,
								subjectTypes[i]+COMPOUND_INDEX_CONNECTOR+props[j]));
		else if (props.length == 0)
			for (int i=0; i<subjects.length; i++)
				result.add(getFilterers(allPropsOfSubject, subjects[i]));
		else
			for (int i=0; i<subjects.length; i++)
				for (int j=0; j<props.length; j++)
					result.add(getFilterers(specificSubjectAndProp,
							subjects[i]+COMPOUND_INDEX_CONNECTOR+props[j]));
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
	 * @see org.universAAL.middleware.sodapop.BusStrategy#handle(org.universAAL.middleware.sodapop.msg.Message, String)
	 */
	public void handle(Message msg, String senderID) {
		if (msg.getType() != MessageType.event
				|| !(msg.getContent() instanceof ContextEvent))
			return;

		if (!msg.isRemote())
			sodapop.propagateMessage(bus, msg);

		HashSet allSubscribers = new HashSet();
		ContextEvent event = (ContextEvent) msg.getContent();
		String propertyURI = event.getRDFPredicate(),
				subjectURI = event.getSubjectURI(),
				subjectTypeURI = event.getSubjectTypeURI();

		Vector filterers = (Vector) specificSubjectAndProp.get(subjectURI
				+ COMPOUND_INDEX_CONNECTOR + propertyURI);
		if (filterers != null)
			for (int i=0; i < filterers.size(); i++)
			    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			    	allSubscribers.add(((ContextFilterer) filterers.get(i)).s); 
		
		filterers = (Vector) specificDomainAndProp.get(subjectTypeURI
				+ COMPOUND_INDEX_CONNECTOR + propertyURI);
		if (filterers != null)
			for (int i=0; i < filterers.size(); i++)
			    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			    	allSubscribers.add(((ContextFilterer) filterers.get(i)).s);

		filterers = (Vector) allPropsOfSubject.get(subjectURI);
		if (filterers != null)
			for (int i=0; i < filterers.size(); i++)
			    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			    	allSubscribers.add(((ContextFilterer) filterers.get(i)).s); 

		filterers = (Vector) allPropsOfDomain.get(subjectTypeURI);
		if (filterers != null)
			for (int i=0; i < filterers.size(); i++)
			    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			    	allSubscribers.add(((ContextFilterer) filterers.get(i)).s);

		filterers = (Vector) allSubjectsWithProp.get(propertyURI);
		if (filterers != null)
			for (int i=0; i < filterers.size(); i++)
			    if (((ContextFilterer) filterers.get(i)).f.matches(event))
			    	allSubscribers.add(((ContextFilterer) filterers.get(i)).s); 

		for (int i=0; i < notIndexedFilterers.size(); i++)
		    if (((ContextFilterer) notIndexedFilterers.get(i)).f.matches(event))
		    	allSubscribers.add(((ContextFilterer) notIndexedFilterers.get(i)).s); 

		for (Iterator i = allSubscribers.iterator(); i.hasNext();)
			((ContextSubscriber) i.next()).handleContextEvent(event);
	}

	void removeMatchingRegParams(ContextSubscriber subscriber, ContextEventPattern[] initialSubscriptions) {
		if (initialSubscriptions == null || subscriber == null)
			return;

		for (int i=0; i<initialSubscriptions.length; i++) {
			Vector filterers = getFilterers(initialSubscriptions[i]);
			if (filterers == null)
				continue;
			
			for (Iterator j=filterers.iterator(); j.hasNext();)
				for (Iterator k=((Vector) j.next()).iterator(); k.hasNext();)
					if (((ContextFilterer) k.next()).s == subscriber)
						j.remove();
		}
	}
	
	private void remove(ContextSubscriber subscriber, Vector filterers) {
		for (Iterator i=filterers.iterator(); i.hasNext();)
			if (((ContextFilterer) i.next()).s == subscriber)
				i.remove();
	}
	
	private void remove(ContextSubscriber subscriber, Hashtable filterers) {
		for (Iterator i = filterers.values().iterator();  i.hasNext(); )
			remove(subscriber, (Vector) i.next());
	}

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
