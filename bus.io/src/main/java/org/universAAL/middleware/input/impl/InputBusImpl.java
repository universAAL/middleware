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
 * @see org.universAAL.middleware.input.InputBus
 * 
 */
public class InputBusImpl extends AbstractBus implements InputBus {

	/**
	 * Create an instance of the InputBus using the provided strategy.
	 * 
	 * @param g
	 *            Pointer to the lokal instance of the SodaPop bus-system
	 */
	public InputBusImpl(SodaPop g) {
		super(Constants.uAAL_BUS_NAME_INPUT, new InputStrategy(g), g);
		busStrategy.setBus(this);
	}

	/**
	 * Closes the dialog with dialogID
	 * 
	 * @param os
	 *            Maybe to make sure that this method is called only by
	 *            OutputStrategy ??? But the constructor of OutputStrategy is
	 *            public ???
	 * @param dialogID
	 *            ID of the dialog to abort
	 */
	public void abortDialog(OutputStrategy os, String dialogID) {
		if (os != null && dialogID != null)
			((InputStrategy) busStrategy).abortDialog(dialogID);
	}

	/**
	 * We do not want to use the standard implementation of "register" from
	 * AbstractBus to make sure that the member is neither an InputPublisher or
	 * an InputSubscriber. So this method always return null.
	 */
	public String register(BusMember member) {
		return null;
	}

	/**
	 * Method to register an InputPublisher at the bus
	 * 
	 * @return ID of the publisher
	 */
	public String register(InputPublisher publisher) {
		return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				+ super.register(publisher);
	}

	/**
	 * Method to register an InputSubscriber at the bus
	 * 
	 * @return ID of the subscriber
	 */
	public String register(InputSubscriber subscriber) {
		return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				+ super.register(subscriber);
	}

	/**
	 * Again we do not support the standard implementation of sendMessage from
	 * AbstractBus and therefore this method simply do nothing.
	 */
	public void sendMessage(String senderID, Message msg) {
	}

	/**
	 * Allows to send Input-Event on the bus
	 * 
	 * @param publisherID
	 *            ID from the publisher of the event
	 * @param msg
	 *            Content of the event
	 */
	public void sendMessage(String publisherID, InputEvent msg) {
		Activator.assessContentSerialization(msg);
		if (publisherID != null
				&& publisherID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(publisherID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()), new Message(MessageType.event, msg));
	}

	/**
	 * Allows to unregister publisher of input-events from the bus
	 * 
	 * @param publisherID
	 *            ID of the publisher
	 * @param publisher
	 *            Concrete instance of the publisher ??? Why this ??? Is part of
	 *            the publisher
	 */
	public void unregister(String publisherID, InputPublisher publisher) {
		if (publisherID != null
				&& publisherID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(publisherID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()), publisher);
	}

	/**
	 * Allows to unregister subscriber of input-events from the bus
	 * 
	 * @param subscriberID
	 *            ID of the subscriber
	 * @param subscriber
	 *            Concrete instance of the subscriber ??? Why this ??? Is part
	 *            of the subscriber
	 */
	public void unregister(String subscriberID, InputSubscriber subscriber) {
		if (subscriberID != null
				&& subscriberID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(subscriberID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()), subscriber);
		((InputStrategy) busStrategy).removeRegParams(subscriber);
	}

	/**
	 * Standard implementation for unregister from AbstractBus. Is simply do
	 * nothing here. Use Methods for InputSubscriber/InputPublisher instead.
	 */
	public void unregister(String id, BusMember member) {
	}

	/**
	 * Method to register an input-subscriber for a dialog
	 * 
	 * @param subscriberID ID of the subscriber
	 * @param dialogID ID of the dialog
	 */
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
	
	/**
	 * Method to unregister an input-subscriber from a dialog
	 * 
	 * @param subscriberID ID of the subscriber
	 * @param dialogID ID of the dialog
	 */
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
