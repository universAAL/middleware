/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.input.impl;

import org.universAAL.middleware.input.InputBus;
import org.universAAL.middleware.input.InputEvent;
import org.universAAL.middleware.input.InputPublisher;
import org.universAAL.middleware.input.InputSubscriber;
import org.universAAL.middleware.io.Activator;
import org.universAAL.middleware.output.impl.OutputStrategy;
import org.universAAL.middleware.sodapop.AbstractBus;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageType;
import org.universAAL.middleware.util.Constants;

/**
 * @author mtazari
 * 
 */
public class InputBusImpl extends AbstractBus implements InputBus {

	public InputBusImpl(SodaPop g) {
		super(Constants.uAAL_BUS_NAME_INPUT, new InputStrategy(g), g);
		busStrategy.setBus(this);
	}

	public void abortDialog(OutputStrategy os, String dialogID) {
		if (os != null && dialogID != null)
			((InputStrategy) busStrategy).abortDialog(dialogID);
	}

	public String register(BusMember member) {
		return null;
	}

	public String register(InputPublisher publisher) {
		return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				+ super.register(publisher);
	}

	public String register(InputSubscriber subscriber) {
		return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				+ super.register(subscriber);
	}

	public void sendMessage(String senderID, Message msg) {
	}

	public void sendMessage(String publisherID, InputEvent msg) {
		Activator.assessContentSerialization(msg);
		if (publisherID != null
				&& publisherID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(publisherID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()), new Message(MessageType.event, msg));
	}

	public void unregister(String publisherID, InputPublisher publisher) {
		if (publisherID != null
				&& publisherID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(publisherID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()), publisher);
	}

	public void unregister(String subscriberID, InputSubscriber subscriber) {
		if (subscriberID != null
				&& subscriberID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(subscriberID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()), subscriber);
		((InputStrategy) busStrategy).removeRegParams(subscriber);
	}

	public void unregister(String id, BusMember member) {
	}

	public void addNewRegParams(String subscriberID, String dialogID) {
		if (subscriberID != null
				&& subscriberID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(subscriberID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()));
			if (o instanceof InputSubscriber)
				((InputStrategy) busStrategy).addRegParams((InputSubscriber) o,
						dialogID);
		}
	}

	public void removeMatchingRegParams(String subscriberID, String dialogID) {
		if (subscriberID != null
				&& subscriberID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(subscriberID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()));
			if (o instanceof InputSubscriber)
				((InputStrategy) busStrategy).removeMatchingRegParams(
						(InputSubscriber) o, dialogID);
		}
	}
}
