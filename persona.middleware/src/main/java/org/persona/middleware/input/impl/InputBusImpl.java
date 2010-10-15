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
package org.persona.middleware.input.impl;

import org.persona.middleware.Activator;
import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.input.InputBus;
import org.persona.middleware.input.InputEvent;
import org.persona.middleware.input.InputPublisher;
import org.persona.middleware.input.InputSubscriber;
import org.persona.middleware.output.impl.OutputStrategy;

import de.fhg.igd.ima.sodapop.AbstractBus;
import de.fhg.igd.ima.sodapop.BusMember;
import de.fhg.igd.ima.sodapop.SodaPop;
import de.fhg.igd.ima.sodapop.msg.Message;
import de.fhg.igd.ima.sodapop.msg.MessageType;

/**
 * @author mtazari
 *
 */
public class InputBusImpl extends AbstractBus implements InputBus {
	
	public InputBusImpl(SodaPop g) {
		super(MiddlewareConstants.PERSONA_BUS_NAME_INPUT, new InputStrategy(g), g);
		busStrategy.setBus(this);
	}
	
	public void abortDialog(OutputStrategy os, String dialogID) {
		if (os != null  &&  dialogID != null)
			((InputStrategy) busStrategy).abortDialog(dialogID);
	}
	
	public String register(BusMember member) {
		return null;
	}

	public String register(InputPublisher publisher) {
		return MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + super.register(publisher);
	}

	public String register(InputSubscriber subscriber) {
		return MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + super.register(subscriber);
	}
	
	public void sendMessage(String senderID, Message msg) {}

	public void sendMessage(String publisherID, InputEvent msg) {
		Activator.assessContentSerialization(msg);
		if (publisherID != null
				&&  publisherID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(
					publisherID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					new Message(MessageType.event, msg));
	}

	public void unregister(String publisherID, InputPublisher publisher) {
		if (publisherID != null
				&&  publisherID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(
					publisherID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					publisher);
	}

	public void unregister(String subscriberID, InputSubscriber subscriber) {
		if (subscriberID != null
				&&  subscriberID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
		super.unregister(
				subscriberID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
				subscriber);
		((InputStrategy) busStrategy).removeRegParams(subscriber);
	}
	
	public void unregister(String id, BusMember member) {}

	public void addNewRegParams(String subscriberID, String dialogID) {
		if (subscriberID != null
				&&  subscriberID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(
					subscriberID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof InputSubscriber)
				((InputStrategy) busStrategy).addRegParams((InputSubscriber) o, dialogID);
		}
	}

	public void removeMatchingRegParams(String subscriberID, String dialogID) {
		if (subscriberID != null
				&&  subscriberID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(
					subscriberID.substring(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof InputSubscriber)
				((InputStrategy) busStrategy).removeMatchingRegParams((InputSubscriber) o, dialogID);
		}
	}
}
