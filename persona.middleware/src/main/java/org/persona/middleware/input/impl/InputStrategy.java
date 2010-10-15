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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.persona.middleware.Activator;
import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.PResource;
import org.persona.middleware.input.InputEvent;
import org.persona.middleware.input.InputSubscriber;
import org.persona.middleware.output.impl.OutputBusImpl;

import de.fhg.igd.ima.sodapop.AbstractBus;
import de.fhg.igd.ima.sodapop.BusStrategy;
import de.fhg.igd.ima.sodapop.SodaPop;
import de.fhg.igd.ima.sodapop.msg.Message;
import de.fhg.igd.ima.sodapop.msg.MessageType;

/**
 * @author mtazari
 * 
 */
public class InputStrategy extends BusStrategy {
	private static final String PROP_ABORT_DIALOG_ID =
		InputEvent.PERSONA_INPUT_NAMESPACE + "abortDialogID";
	private static final String TYPE_ABORT_DIALOG_CONTENT_TYPE =
		InputEvent.PERSONA_INPUT_NAMESPACE + "AbortDialog";
    
	private Hashtable dialogInputSubscribers;
	private InputSubscriber dialogManager = null;

	public InputStrategy(SodaPop sodapop) {
		super(sodapop);
		dialogInputSubscribers = new Hashtable();
	}
	
	void abortDialog(String dialogID) {
		if (dialogID != null) {
			int num = 0;
			InputSubscriber subscriber = (InputSubscriber) dialogInputSubscribers.remove(dialogID);
			if (dialogManager != null  &&  dialogManager != subscriber) {
				dialogManager.dialogAborted(dialogID);
				num = 1;
			}
			if (subscriber != null) {
				subscriber.dialogAborted(dialogID);
				if (dialogManager == subscriber)
					num = 2;
				else
					num++;
			}
			
			if (num < 2) {
				PResource pr = new PResource();
				pr.addType(TYPE_ABORT_DIALOG_CONTENT_TYPE, true);
				pr.setProperty(PROP_ABORT_DIALOG_ID, new PResource(dialogID));
				Activator.assessContentSerialization(pr);
				Message m = new Message(MessageType.p2p_event, pr);
				sodapop.propagateMessage(bus, m);
			}
		}
	}

	void addRegParams(InputSubscriber subscriber, String dialogID) {
		if (subscriber == null)
			return;

		if (dialogID == null
				||  InputEvent.PERSONA_MAIN_MENU_REQUEST.equals(dialogID))
			if (dialogManager == null)
				dialogManager = subscriber;
			else {
				// only one component per bus instance is allowed to play the role of the dialog manager
				// TODO: at least a log entry 
			}
		else if (dialogInputSubscribers.get(dialogID) == null)
			dialogInputSubscribers.put(dialogID, subscriber);
	}

	/**
	 * @see de.fhg.igd.ima.sodapop.BusStrategy#handle(de.fhg.igd.ima.sodapop.msg.Message, String)
	 */
	public void handle(Message msg, String senderID) {
		if (msg.getType() != MessageType.event
				|| !(msg.getContent() instanceof InputEvent))
			return;

		if (!msg.isRemote())
			sodapop.propagateMessage(bus, msg);
		else {
			Object o = msg.getContent();
			if (o instanceof PResource
					&&  TYPE_ABORT_DIALOG_CONTENT_TYPE.equals(((PResource) o).getType())) {
				o = ((PResource) o).getProperty(PROP_ABORT_DIALOG_ID);
				if (o instanceof String) {
					InputSubscriber subscriber = (InputSubscriber) dialogInputSubscribers.remove(o);
					if (dialogManager != null  &&  dialogManager != subscriber)
						dialogManager.dialogAborted((String) o);
					if (subscriber != null) {
						AbstractBus ib = getLocalBusByName(
								MiddlewareConstants.PERSONA_BUS_NAME_OUTPUT);
						if (ib instanceof OutputBusImpl)
							((OutputBusImpl) ib).abortDialog(this, (String) o);
						subscriber.dialogAborted((String) o);
					}
					return;
				}
			}
			if (!(o instanceof InputEvent))
				// TODO: add a log entry
				return;
		}

		InputEvent event = (InputEvent) msg.getContent();
		if (event.hasDialogInput()) {
			// automatically un-subscribe by removing from the hash-table
			InputSubscriber is = (InputSubscriber) dialogInputSubscribers.remove(event.getDialogID());
			if (is != null) {
				// only one instance of the input bus should get here
				if (!event.isSubdialogCall()) {
					AbstractBus ib = getLocalBusByName(
							MiddlewareConstants.PERSONA_BUS_NAME_OUTPUT);
					if (ib instanceof OutputBusImpl)
						((OutputBusImpl) ib).abortDialog(this, event.getDialogID());
				}
				is.handleInputEvent(event);
			}
		} else if (event.isServiceSearch()
				||  InputEvent.PERSONA_MAIN_MENU_REQUEST.equals(event.getDialogID())) {
			if (dialogManager != null)
				// only one instance of the input bus should get here
				dialogManager.handleInputEvent(event);
		} else {
			// event is not well-formed
			// TODO: at least a log entry 
		}
	}

	void removeMatchingRegParams(InputSubscriber subscriber, String dialogID) {
		if (subscriber == null  ||  dialogID == null)
			return;

		synchronized (dialogInputSubscribers) {
			InputSubscriber is = (InputSubscriber) dialogInputSubscribers.remove(dialogID);
			if (is != subscriber  &&  is != null)
				dialogInputSubscribers.put(dialogID, is);
		}
	}

	void removeRegParams(InputSubscriber subscriber) {
		if (subscriber == null)
			return;

		synchronized (dialogInputSubscribers) {
			Vector ids = new Vector();
			for (Iterator i=dialogInputSubscribers.keySet().iterator(); i.hasNext();) {
				Object key = i.next();
				if (subscriber == dialogInputSubscribers.get(key))
					ids.add(key);
			}
			for (Iterator i=ids.iterator(); i.hasNext();)
				dialogInputSubscribers.remove(i.next());
		}
	}

}
