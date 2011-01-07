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
package org.universAAL.middleware.output.impl;

import org.universAAL.middleware.input.impl.InputStrategy;
import org.universAAL.middleware.io.Activator;
import org.universAAL.middleware.io.rdf.SubdialogTrigger;
import org.universAAL.middleware.io.rdf.Submit;
import org.universAAL.middleware.output.DialogManager;
import org.universAAL.middleware.output.OutputBus;
import org.universAAL.middleware.output.OutputEvent;
import org.universAAL.middleware.output.OutputEventPattern;
import org.universAAL.middleware.output.OutputPublisher;
import org.universAAL.middleware.output.OutputSubscriber;
import org.universAAL.middleware.rdf.Resource;
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
public class OutputBusImpl extends AbstractBus implements OutputBus {

	public OutputBusImpl(SodaPop g) {
		super(Constants.uAAL_BUS_NAME_OUTPUT, new OutputStrategy(g), g);
		busStrategy.setBus(this);
	}

	public void abortDialog(String publisherID, String dialogID) {
		if (publisherID != null
				&& publisherID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			String localID = publisherID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length());
			Object o = registry.get(localID);
			if (o instanceof OutputPublisher)
				((OutputStrategy) busStrategy).abortDialog(localID, dialogID);
		}
	}

	public void abortDialog(InputStrategy is, String dialogID) {
		if (is != null)
			((OutputStrategy) busStrategy).abortDialog(dialogID);
	}

	public void adaptationParametersChanged(DialogManager dm, OutputEvent oe,
			String changedProp) {
		((OutputStrategy) busStrategy).adaptationParametersChanged(dm, oe,
				changedProp);
	}

	public void addNewRegParams(String subscriberID,
			OutputEventPattern newSubscription) {
		if (subscriberID != null
				&& subscriberID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(subscriberID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()));
			if (o instanceof OutputSubscriber)
				((OutputStrategy) busStrategy).addRegParams(subscriberID,
						newSubscription);
		}
	}

	public void dialogFinished(String subscriberID, Submit submission,
			boolean poppedMessage) {
		if (subscriberID != null
				&& subscriberID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(subscriberID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()));
			if (o instanceof OutputSubscriber)
				if (submission instanceof SubdialogTrigger)
					((OutputStrategy) busStrategy).suspendDialog(submission
							.getDialogID());
				else if (submission != null)
					((OutputStrategy) busStrategy).dialogFinished(subscriberID,
							submission.getDialogID(), poppedMessage);
		}
	}

	public void dialogSuspended(DialogManager dm, String dialogID) {
		((OutputStrategy) busStrategy).dialogSuspended(dm, dialogID);
	}

	public String register(BusMember member) {
		return null;
	}

	public String register(OutputPublisher publisher) {
		String id = super.register(publisher);
		if (publisher instanceof DialogManager)
			((OutputStrategy) busStrategy)
					.setDialogManager((DialogManager) publisher);
		return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + id;
	}

	public String register(OutputSubscriber subscriber,
			OutputEventPattern initialSubscription) {
		String id = Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				+ super.register(subscriber);
		if (initialSubscription != null)
			((OutputStrategy) busStrategy)
					.addRegParams(id, initialSubscription);
		return id;
	}

	public void removeMatchingRegParams(String subscriberID,
			OutputEventPattern oldSubscription) {
		if (subscriberID != null
				&& subscriberID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(subscriberID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()));
			if (o instanceof OutputSubscriber)
				((OutputStrategy) busStrategy).removeMatchingRegParams(
						subscriberID, oldSubscription);
		}
	}

	public void resumeDialog(String publisherID, String dialogID,
			Resource dialogData) {
		if (publisherID != null
				&& publisherID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			String localID = publisherID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length());
			Object o = registry.get(localID);
			if (o instanceof DialogManager && dialogData instanceof OutputEvent)
				((OutputStrategy) busStrategy).adaptationParametersChanged(
						(DialogManager) o, (OutputEvent) dialogData, null);
			else if (o instanceof OutputPublisher)
				((OutputStrategy) busStrategy).resumeDialog(dialogID,
						dialogData);
		}
	}

	public void sendMessage(String senderID, Message msg) {
	}

	public void sendMessage(String publisherID, OutputEvent msg) {
		Activator.assessContentSerialization(msg);
		if (publisherID != null
				&& publisherID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(publisherID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()), new Message(MessageType.event, msg));
	}

	public void unregister(String id, BusMember member) {
	}

	public void unregister(String publisherID, OutputPublisher publisher) {
		if (publisherID != null
				&& publisherID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(publisherID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()), publisher);
	}

	public void unregister(String subscriberID, OutputSubscriber subscriber) {
		if (subscriberID != null
				&& subscriberID
						.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(subscriberID
					.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
							.length()));
			if (o == subscriber) {
				super.unregister(subscriberID
						.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
								.length()), subscriber);
				((OutputStrategy) busStrategy).removeRegParams(subscriberID);
			}
		}
	}
}
