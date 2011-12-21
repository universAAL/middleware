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
import org.universAAL.middleware.io.SharedResources;
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
 * @see org.universAAL.middleware.input.OutputBus
 * 
 */
public class OutputBusImpl extends AbstractBus implements OutputBus {
    public static Object[] busFetchParams;

    /**
     * Create an instance of the OutputBus using the provided strategy.
     * 
     * @param g
     *            Pointer to the lokal instance of the SodaPop bus-system
     */
    public OutputBusImpl(SodaPop g) {
	super(Constants.uAAL_BUS_NAME_OUTPUT, new OutputStrategy(g), g);
	busStrategy.setBus(this);
    }

    /**
     * Closes an opened dialog
     * 
     * @param publisherID
     *            ID of the publisher of the Dialog
     * @param dialogID
     *            ID of the dialog to delete
     */
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

    /**
     * 
     * @param is
     *            Calling InputStrategy
     * @param dialogID
     *            ID of the dialog to delete
     */
    public void abortDialog(InputStrategy is, String dialogID) {
	if (is != null)
	    ((OutputStrategy) busStrategy).abortDialog(dialogID);
    }

    /**
     * @param dm
     *            The responsible Dialogmanager
     * @parem oe New/Changed output
     * @param changedProp
     *            Property that has been changed since last time
     */
    public void adaptationParametersChanged(DialogManager dm, OutputEvent oe,
	    String changedProp) {
	((OutputStrategy) busStrategy).adaptationParametersChanged(dm, oe,
		changedProp);
    }

    /**
     * 
     * Adds a new subscription to the bus
     * 
     * @param subscriberID
     *            ID of the subscriber like given by register
     * @param newSubscription
     *            Description of the subscription
     */
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

    /**
     * 
     * Denotes a regular suspended or closed dialog. ??? I do not understand the
     * parameters of this method ???
     * 
     * @param subscriberID
     * @param submission
     * @param poppedMessage
     * 
     */
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

    /**
     * 
     * Can only be called by the DialogManager. Suspend the given dialog
     * 
     * @param dm
     *            Instance of the DialogManager
     * @param dialogID
     *            ID of the dialog to suspend
     */
    public void dialogSuspended(DialogManager dm, String dialogID) {
	((OutputStrategy) busStrategy).dialogSuspended(dm, dialogID);
    }

    /**
     * Standard implementation of AbstractBus will not be used here and always
     * return null.
     */
    public String register(BusMember member) {
	return null;
    }

    /**
     * Method to register an OutputPublisher at the bus
     * 
     * @param publisher
     *            Instance of the Publisher to register
     * 
     * @return ID of the publisher
     */
    public String register(OutputPublisher publisher) {
	String id = super.register(publisher);
	if (publisher instanceof DialogManager)
	    ((OutputStrategy) busStrategy)
		    .setDialogManager((DialogManager) publisher);
	return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + id;
    }

    /**
     * Method to register an OutputSubscriber at the bus
     * 
     * @param subscriber
     *            Instance of a Subscriber to register
     * @param initialSubscription
     *            Initial description of the Outputevents the subscriber is
     *            asking for
     * 
     * @return ID of the subscriber
     */
    public String register(OutputSubscriber subscriber,
	    OutputEventPattern initialSubscription) {
	String id = Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		+ super.register(subscriber);
	if (initialSubscription != null)
	    ((OutputStrategy) busStrategy)
		    .addRegParams(id, initialSubscription);
	return id;
    }

    /**
     * Removes a subscription from the bus
     * 
     * @param subscriberID
     *            ID from the owner of the subscription
     * @param oldSubscription
     *            Subscription to remove
     */
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

    /**
     * 
     * Depending on the type of the publisher this method either update
     * (publisherID is an event) or reopen the given dialog (publisherID is a
     * Publisher-Object)
     * 
     * @param publisherID
     * @param dialogID
     */
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

    /**
     * Standard implementation of sendMessage from AbstractBus will not be used
     * here and simply do nothing.
     */
    public void sendMessage(String senderID, Message msg) {
    }

    /**
     * 
     * Publish the given OutputEvent on the bus
     * 
     * @param publisherID
     *            Publisher of the event
     * @param msg
     *            Message to be sent
     * 
     */
    public void sendMessage(String publisherID, OutputEvent msg) {
	SharedResources.assessContentSerialization(msg);
	if (publisherID != null
		&& publisherID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    super.sendMessage(publisherID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()), new Message(MessageType.event, msg));
    }

    /**
     * Standard implementation of unregister from AbstractBus will not be used
     * here and simply do nothing.
     */
    public void unregister(String id, BusMember member) {

    }

    /**
     * @see org.universAAL.middleware.output.OutputBus#unregister(java.lang.String,
     *      org.universAAL.middleware.output.OutputPublisher)
     */
    public void unregister(String publisherID, OutputPublisher publisher) {
	if (publisherID != null
		&& publisherID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    super.unregister(publisherID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()), publisher);
    }

    /**
     * @see org.universAAL.middleware.output.OutputBus#unregister(java.lang.String,
     *      org.universAAL.middleware.output.OutputSubscriber)
     */
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
