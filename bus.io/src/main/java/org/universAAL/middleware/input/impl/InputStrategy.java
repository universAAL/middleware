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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.universAAL.middleware.input.InputEvent;
import org.universAAL.middleware.input.InputSubscriber;
import org.universAAL.middleware.io.SharedResources;
import org.universAAL.middleware.output.impl.OutputBusImpl;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.AbstractBus;
import org.universAAL.middleware.sodapop.BusStrategy;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageType;
import org.universAAL.middleware.util.Constants;

/**
 * @author mtazari
 * 
 *         The strategy of a bus is responsible to handle all messages passing
 *         the local instance. The central method is
 *         "handle(Message msg, String senderID)" that is called for every
 *         message given to the bus. InputBus is responsible
 */
public class InputStrategy extends BusStrategy {
    private static final String PROP_ABORT_DIALOG_ID = InputEvent.uAAL_INPUT_NAMESPACE
	    + "abortDialogID";
    private static final String TYPE_ABORT_DIALOG_CONTENT_TYPE = InputEvent.uAAL_INPUT_NAMESPACE
	    + "AbortDialog";

    private Hashtable dialogInputSubscribers; // dialogs that are subscribed for
    // input
    private InputSubscriber dialogManager = null;

    /**
     * Creates a new instance of the InputStrategy
     * 
     * @param sodapop
     *            SodaPop network instance
     */
    public InputStrategy(SodaPop sodapop) {
	super(sodapop);
	dialogInputSubscribers = new Hashtable();
    }

    /**
     * Aborts the dialog with the given ID
     * 
     * @param dialogID
     *            ID of the dialog to abort
     */
    void abortDialog(String dialogID) {
	if (dialogID != null) {
	    int num = 0;
	    InputSubscriber subscriber = (InputSubscriber) dialogInputSubscribers
		    .remove(dialogID);
	    if (dialogManager != null && dialogManager != subscriber) {
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

	    // id subscriber is not the dialogmanager propagate the massage
	    if (num < 2) {
		Resource pr = new Resource();
		pr.addType(TYPE_ABORT_DIALOG_CONTENT_TYPE, true);
		pr.setProperty(PROP_ABORT_DIALOG_ID, new Resource(dialogID));
		SharedResources.assessContentSerialization(pr);
		Message m = new Message(MessageType.p2p_event, pr);
		sodapop.propagateMessage(bus, m);
	    }
	}
    }

    /**
     * 
     * Add a subscriber for the input from the given dialog
     * 
     * @param subscriber
     *            Subscriber to add
     * @param dialogID
     *            ID of the dialog the subscription is about
     */
    void addRegParams(InputSubscriber subscriber, String dialogID) {
	if (subscriber == null)
	    return;

	// If the event is a uAAL_MAIN_MENU_REQUEST it must com from the
	// dialog-manager and therefore we can safe this for later use
	if (dialogID == null
		|| InputEvent.uAAL_MAIN_MENU_REQUEST.equals(dialogID))
	    if (dialogManager == null)
		dialogManager = subscriber;
	    else {
		// only one component per bus instance is allowed to play the
		// role of the dialog manager
		// TODO: at least a log entry
	    }
	else if (dialogInputSubscribers.get(dialogID) == null)
	    dialogInputSubscribers.put(dialogID, subscriber);
    }

    /**
     * Handle all incoming messages. Every call of this method take place in his
     * own thread.
     * 
     * @param msg
     *            Message to handle
     * @param senderID
     *            ID of the sender of the message
     */
    public void handle(Message msg, String senderID) {
	// if the message is not a valid InputEvent simply ignore it
	if (msg.getType() != MessageType.event
		|| !(msg.getContent() instanceof InputEvent))
	    return;

	// If the message is a local one propagate it on the bus
	if (!msg.isRemote())
	    sodapop.propagateMessage(bus, msg);
	else {
	    Object o = msg.getContent();
	    // if the input is a result from a dialog give it to the
	    // dialogManager and the output-bus
	    if (o instanceof Resource
		    && TYPE_ABORT_DIALOG_CONTENT_TYPE.equals(((Resource) o)
			    .getType())) {
		o = ((Resource) o).getProperty(PROP_ABORT_DIALOG_ID);
		if (o instanceof String) {
		    InputSubscriber subscriber = (InputSubscriber) dialogInputSubscribers
			    .remove(o);
		    if (dialogManager != null && dialogManager != subscriber)
			dialogManager.dialogAborted((String) o);
		    if (subscriber != null) {
			AbstractBus ib = getLocalBusByName(Constants.uAAL_BUS_NAME_OUTPUT);
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
	    InputSubscriber is = (InputSubscriber) dialogInputSubscribers
		    .remove(event.getDialogID());
	    if (is != null) {
		// only one instance of the input bus should get here
		if (!event.isSubdialogCall()) {
		    AbstractBus ib = getLocalBusByName(Constants.uAAL_BUS_NAME_OUTPUT);
		    if (ib instanceof OutputBusImpl)
			((OutputBusImpl) ib).abortDialog(this, event
				.getDialogID());
		}
		is.handleEvent(msg);
	    }
	} else if (event.isServiceSearch()
		|| InputEvent.uAAL_MAIN_MENU_REQUEST
			.equals(event.getDialogID())) {
	    if (dialogManager != null)
		// only one instance of the input bus should get here
		dialogManager.handleEvent(msg);
	} else {
	    // event is not well-formed
	    // TODO: at least a log entry
	}
    }

    /**
     * Remove the subscription from a dialog
     * 
     * @param subscriber
     *            Instance of the subscriber to be removed
     * @param dialogID
     *            Dialog the subscriber is removed from
     */
    void removeMatchingRegParams(InputSubscriber subscriber, String dialogID) {
	if (subscriber == null || dialogID == null)
	    return;

	synchronized (dialogInputSubscribers) {
	    InputSubscriber is = (InputSubscriber) dialogInputSubscribers
		    .remove(dialogID);
	    if (is != subscriber && is != null)
		dialogInputSubscribers.put(dialogID, is);
	}
    }

    /**
     * Removes all subscriptions from the given subscriber
     * 
     * @param subscriber
     *            Instance of the subscriber that need to be unsubscribed
     */
    void removeRegParams(InputSubscriber subscriber) {
	if (subscriber == null)
	    return;

	synchronized (dialogInputSubscribers) {
	    Vector ids = new Vector();
	    for (Iterator i = dialogInputSubscribers.keySet().iterator(); i
		    .hasNext();) {
		Object key = i.next();
		if (subscriber == dialogInputSubscribers.get(key))
		    ids.add(key);
	    }
	    for (Iterator i = ids.iterator(); i.hasNext();)
		dialogInputSubscribers.remove(i.next());
	}
    }

}
