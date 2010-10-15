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
package org.persona.middleware.output.impl;

import org.persona.middleware.Activator;
import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.PResource;
import org.persona.middleware.dialog.SubdialogTrigger;
import org.persona.middleware.dialog.Submit;
import org.persona.middleware.input.impl.InputStrategy;
import org.persona.middleware.output.DialogManager;
import org.persona.middleware.output.OutputBus;
import org.persona.middleware.output.OutputEvent;
import org.persona.middleware.output.OutputEventPattern;
import org.persona.middleware.output.OutputPublisher;
import org.persona.middleware.output.OutputSubscriber;

import de.fhg.igd.ima.sodapop.AbstractBus;
import de.fhg.igd.ima.sodapop.BusMember;
import de.fhg.igd.ima.sodapop.SodaPop;
import de.fhg.igd.ima.sodapop.msg.Message;
import de.fhg.igd.ima.sodapop.msg.MessageType;

/**
 * @author mtazari
 * 
 */
public class OutputBusImpl extends AbstractBus implements OutputBus {

	public OutputBusImpl(SodaPop g) {
		super(MiddlewareConstants.PERSONA_BUS_NAME_OUTPUT,
				new OutputStrategy(g), g);
		busStrategy.setBus(this);
	}

	public void abortDialog(String publisherID, String dialogID) {
		if (publisherID != null  &&  publisherID.startsWith(
				MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			String localID = publisherID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length());
			Object o = registry.get(localID);
			if (o instanceof OutputPublisher)
				((OutputStrategy) busStrategy).abortDialog(
						localID, dialogID);
		}
	}
	
	public void abortDialog(InputStrategy is, String dialogID) {
		if (is != null)
			((OutputStrategy) busStrategy).abortDialog(dialogID);
	}

	public void adaptationParametersChanged(DialogManager dm, OutputEvent oe, String changedProp) {
		((OutputStrategy) busStrategy).adaptationParametersChanged(dm, oe, changedProp);
	}

	public void addNewRegParams(String subscriberID,
			OutputEventPattern newSubscription) {
		if (subscriberID != null
				&& subscriberID.startsWith(
						MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(subscriberID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof OutputSubscriber)
				((OutputStrategy) busStrategy).addRegParams(
						subscriberID, newSubscription);
		}
	}

	public void dialogFinished(String subscriberID, Submit submission, boolean poppedMessage) {
		if (subscriberID != null
				&& subscriberID.startsWith(
						MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(subscriberID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof OutputSubscriber)
				if (submission instanceof SubdialogTrigger)
					((OutputStrategy) busStrategy).suspendDialog(submission.getDialogID());
				else if (submission != null)
					((OutputStrategy) busStrategy).dialogFinished(
							subscriberID, submission.getDialogID(), poppedMessage);
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
		return MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + id;
	}

	public String register(OutputSubscriber subscriber,
			OutputEventPattern initialSubscription) {
		String id = MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX
				+ super.register(subscriber);
		if (initialSubscription != null)
			((OutputStrategy) busStrategy)
					.addRegParams(id, initialSubscription);
		return id;
	}

	public void removeMatchingRegParams(String subscriberID,
			OutputEventPattern oldSubscription) {
		if (subscriberID != null
				&& subscriberID.startsWith(
						MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(
					subscriberID.substring(
							MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof OutputSubscriber)
				((OutputStrategy) busStrategy).removeMatchingRegParams(
						subscriberID, oldSubscription);
		}
	}
	
	public void resumeDialog(String publisherID, String dialogID, PResource dialogData) {
		if (publisherID != null  &&  publisherID.startsWith(
				MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			String localID = publisherID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length());
			Object o = registry.get(localID);
			if (o instanceof DialogManager  &&  dialogData instanceof OutputEvent)
				((OutputStrategy) busStrategy).adaptationParametersChanged(
						(DialogManager) o, (OutputEvent) dialogData, null); 
			else if (o instanceof OutputPublisher)
				((OutputStrategy) busStrategy).resumeDialog(dialogID, dialogData);
		}
	}

	public void sendMessage(String senderID, Message msg) {
	}

	public void sendMessage(String publisherID, OutputEvent msg) {
		Activator.assessContentSerialization(msg);
		if (publisherID != null  &&  publisherID.startsWith(
				MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(
					publisherID.substring(
							MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					new Message(MessageType.event, msg));
	}

	public void unregister(String id, BusMember member) {
	}

	public void unregister(String publisherID, OutputPublisher publisher) {
		if (publisherID != null  &&  publisherID.startsWith(
				MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.unregister(
					publisherID.substring(
							MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					publisher);
	}

	public void unregister(String subscriberID, OutputSubscriber subscriber) {
		if (subscriberID != null  &&  subscriberID.startsWith(
				MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			Object o = registry.get(
					subscriberID.substring(
							MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o == subscriber) {
				super.unregister(
						subscriberID.substring(
								MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
						subscriber);
				((OutputStrategy) busStrategy).removeRegParams(subscriberID);
			}
		}
	}
}
