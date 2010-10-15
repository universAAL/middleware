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
package org.persona.middleware.context.impl;

import org.persona.middleware.Activator;
import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.context.ContextBus;
import org.persona.middleware.context.ContextEvent;
import org.persona.middleware.context.ContextEventPattern;
import org.persona.middleware.context.ContextPublisher;
import org.persona.middleware.context.ContextSubscriber;

import de.fhg.igd.ima.sodapop.AbstractBus;
import de.fhg.igd.ima.sodapop.BusMember;
import de.fhg.igd.ima.sodapop.SodaPop;
import de.fhg.igd.ima.sodapop.msg.Message;
import de.fhg.igd.ima.sodapop.msg.MessageType;

/**
 * @author mtazari
 *
 */
public class ContextBusImpl extends AbstractBus implements ContextBus {
	
	public ContextBusImpl(SodaPop g) {
		super(MiddlewareConstants.PERSONA_BUS_NAME_CONTEXT, new ContextStrategy(g), g);
		busStrategy.setBus(this);
	}
	
	public String register(BusMember member) {
		return null;
	}

	public String register(ContextPublisher publisher) {
		return MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + super.register(publisher);
	}

	public String register(ContextSubscriber subscriber, ContextEventPattern[] initialSubscriptions) {
		String id = super.register(subscriber);
		if (initialSubscriptions != null)
			((ContextStrategy) busStrategy).addRegParams(subscriber, initialSubscriptions);
		return MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + id;
	}
	
	public void sendMessage(String senderID, Message msg) {}

	public void sendMessage(String publisherID, ContextEvent msg) {
		Activator.assessContentSerialization(msg);
		if (publisherID != null
				&&  publisherID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(
					publisherID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					new Message(MessageType.event, msg));
	}

	public void unregister(String publisherID, ContextPublisher publisher) {
		if (publisherID != null
				&&  publisherID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(
					publisherID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					publisher);
	}

	public void unregister(String subscriberID, ContextSubscriber subscriber) {
		if (subscriberID != null
				&&  subscriberID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
		super.unregister(
				subscriberID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
				subscriber);
		((ContextStrategy) busStrategy).removeRegParams(subscriber);
	}
	
	public void unregister(String id, BusMember member) {}

	public void addNewRegParams(String subscriberID, ContextEventPattern[] newSubscriptions) {
		if (subscriberID != null
				&&  subscriberID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(
					subscriberID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof ContextSubscriber  &&  newSubscriptions != null)
				((ContextStrategy) busStrategy).addRegParams((ContextSubscriber) o, newSubscriptions);
		}
	}

	public void removeMatchingRegParams(String subscriberID,
			ContextEventPattern[] oldSubscriptions) {
		if (subscriberID != null
				&&  subscriberID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(
					subscriberID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof ContextSubscriber  &&  oldSubscriptions != null)
				((ContextStrategy) busStrategy).removeMatchingRegParams((ContextSubscriber) o, oldSubscriptions);
		}
	}
}
