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
package org.universAAL.middleware.ui.impl;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.AbstractBus;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.sodapop.BusStrategy;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;
import org.universAAL.middleware.sodapop.msg.MessageType;
import org.universAAL.middleware.ui.DialogManager;
import org.universAAL.middleware.ui.UIBus;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.owl.UIBusOntology;
import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.util.ResourceComparator;

/**
 * @author mtazari
 * 
 * @see org.universAAL.middleware.ui.UIBus
 * @author Carsten Stockloew
 */
public class UIBusImpl extends AbstractBus implements UIBus {
    public static final String uAAL_BUS_NAME_UI = "uAAL.bus.ui";
    public static MessageContentSerializer contentSerializer = null;
    public static Container container;
    public static ModuleContext moduleContext;
    public static Object[] contentSerializerParams;
    public static Object[] busFetchParams;
    public static Object[] busShareParams;
    public static Object[] sodapopFetchParams;
    private static UIBusOntology uiBusOntology = new UIBusOntology();

    public static synchronized void assessContentSerialization(Resource content) {
	if (org.universAAL.middleware.util.Constants.debugMode()) {
	    if (contentSerializer == null) {
		contentSerializer = (MessageContentSerializer) moduleContext
			.getContainer().fetchSharedObject(moduleContext,
				contentSerializerParams);
		if (contentSerializer == null)
		    return;
	    }

	    LogUtils
		    .logDebug(
			    moduleContext,
			    UIBusImpl.class,
			    "assessContentSerialization",
			    new Object[] { "Assessing message content serialization:" },
			    null);

	    String str = contentSerializer.serialize(content);
	    LogUtils
		    .logDebug(
			    moduleContext,
			    UIBusImpl.class,
			    "assessContentSerialization",
			    new Object[] { "\n      1. serialization dump\n",
				    str,
				    "\n      2. deserialize & compare with the original resource\n" },
			    null);
	    new ResourceComparator().printDiffs(content,
		    (Resource) contentSerializer.deserialize(str));
	}
    }

    public static void startModule() {
	OntologyManagement.getInstance().register(uiBusOntology);
	container.shareObject(moduleContext, new UIBusImpl((SodaPop) container
		.fetchSharedObject(moduleContext, sodapopFetchParams)),
		busShareParams);
    }

    public static void stopModule() {
	OntologyManagement.getInstance().unregister(uiBusOntology);
    }

    /*
     * public static void loadExportedClasses() throws ClassNotFoundException {
     * Class.forName("org.universAAL.middleware.ui.UIResponse");
     * Class.forName("org.universAAL.middleware.ui.owl.AccessImpairment");
     * Class.forName("org.universAAL.middleware.ui.owl.DialogType");
     * Class.forName("org.universAAL.middleware.ui.owl.Gender");
     * Class.forName("org.universAAL.middleware.ui.owl.Modality");
     * Class.forName("org.universAAL.middleware.ui.owl.PrivacyLevel");
     * Class.forName("org.universAAL.middleware.ui.rdf.ChoiceItem");
     * Class.forName("org.universAAL.middleware.ui.rdf.ChoiceList");
     * Class.forName("org.universAAL.middleware.ui.rdf.Form");
     * Class.forName("org.universAAL.middleware.ui.rdf.Group");
     * Class.forName("org.universAAL.middleware.ui.rdf.InputField");
     * Class.forName("org.universAAL.middleware.ui.rdf.Label");
     * Class.forName("org.universAAL.middleware.ui.rdf.MediaObject");
     * Class.forName("org.universAAL.middleware.ui.rdf.Range");
     * Class.forName("org.universAAL.middleware.ui.rdf.Repeat");
     * Class.forName("org.universAAL.middleware.ui.rdf.Select");
     * Class.forName("org.universAAL.middleware.ui.rdf.Select1");
     * Class.forName("org.universAAL.middleware.ui.rdf.SimpleOutput");
     * Class.forName("org.universAAL.middleware.ui.rdf.SubdialogTrigger");
     * Class.forName("org.universAAL.middleware.ui.rdf.Submit");
     * Class.forName("org.universAAL.middleware.ui.rdf.TextArea");
     * Class.forName("org.universAAL.middleware.ui.UIRequest");
     * Class.forName("org.universAAL.middleware.ui.UIHandlerProfile"); }
     */
    /**
     * Create an instance of the UIBus.
     * 
     * @param g
     *            Pointer to the local instance of the SodaPop bus-system
     */
    public UIBusImpl(SodaPop g) {
	super(uAAL_BUS_NAME_UI, g);
	busStrategy.setBus(this);
    }

    protected BusStrategy createBusStrategy(SodaPop sodapop) {
		return new UIStrategy(sodapop);
	}
    
    /**
     * Closes a running dialog
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
	    Object o = registry.getBusMemberByID(localID);
	    if (o instanceof UICaller)
		((UIStrategy) busStrategy).abortDialog(localID, dialogID);
	}
    }

    /**
     * @param dm
     *            The responsible Dialogmanager
     * @param oe
     *            New/Changed output
     * @param changedProp
     *            Property that has been changed since last time
     */
    public void adaptationParametersChanged(DialogManager dm, UIRequest oe,
	    String changedProp) {
	((UIStrategy) busStrategy).adaptationParametersChanged(dm, oe,
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
	    UIHandlerProfile newSubscription) {
	if (subscriberID != null
		&& subscriberID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
	    Object o = registry.getBusMemberByID(subscriberID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()));
	    if (o instanceof UIHandler)
		((UIStrategy) busStrategy).addRegParams(subscriberID,
			newSubscription);
	}
    }

    /**
     * 
     * Denotes a regular suspended or closed dialog. ??? I do not understand the
     * parameters of this method ???
     * 
     * @param subscriberID
     */
    public void dialogFinished(String subscriberID, UIResponse input) {
	if (input != null
		&& subscriberID != null
		&& subscriberID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
	    Object o = registry.getBusMemberByID(subscriberID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()));
	    if (o instanceof UIHandler)
		if (input.isSubdialogCall())
		    ((UIStrategy) busStrategy).suspendDialog(input
			    .getDialogID());
		else
		    ((UIStrategy) busStrategy).dialogFinished(subscriberID,
			    input);
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
	((UIStrategy) busStrategy).dialogSuspended(dm, dialogID);
    }

    /**
     * Standard implementation of AbstractBus will not be used here and always
     * return null.
     */
    public String register(BusMember member) {
	return null;
    }

    /**
     * Method to register an UICaller at the bus
     * 
     * @param publisher
     *            Instance of the Publisher to register
     * 
     * @return ID of the publisher
     */
    public String register(UICaller publisher) {
	String id = super.register(publisher);
	if (publisher instanceof DialogManager)
	    ((UIStrategy) busStrategy)
		    .setDialogManager((DialogManager) publisher);
	return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + id;
    }

    /**
     * Method to register an UIHandler at the bus
     * 
     * @param subscriber
     *            Instance of a Subscriber to register
     * @param initialSubscription
     *            Initial description of the Outputevents the subscriber is
     *            asking for
     * 
     * @return ID of the subscriber
     */
    public String register(UIHandler subscriber,
	    UIHandlerProfile initialSubscription) {
	String id = Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		+ super.register(subscriber);
	if (initialSubscription != null)
	    ((UIStrategy) busStrategy).addRegParams(id, initialSubscription);
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
	    UIHandlerProfile oldSubscription) {
	if (subscriberID != null
		&& subscriberID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
	    Object o = registry.getBusMemberByID(subscriberID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()));
	    if (o instanceof UIHandler)
		((UIStrategy) busStrategy).removeMatchingRegParams(
			subscriberID, oldSubscription);
	}
    }

    /**
     * 
     * @see UIBus#resumeDialog(String, String, Resource)
     */
    public void resumeDialog(String publisherID, String dialogID,
	    Resource dialogData) {
	if (publisherID != null
		&& publisherID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
	    String localID = publisherID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length());
	    Object o = registry.getBusMemberByID(localID);
	    if (o instanceof DialogManager && dialogData instanceof UIRequest)
		((UIStrategy) busStrategy).adaptationParametersChanged(
			(DialogManager) o, (UIRequest) dialogData, null);
	    else if (o instanceof UICaller)
		((UIStrategy) busStrategy).resumeDialog(dialogID, dialogData);
	}
    }

    /**
     * Standard implementation of sendMessage from AbstractBus will not be used
     * here and simply do nothing.
     */
    public final void sendMessage(String senderID, Message msg) {
    }

    /**
     * 
     * Publish the given UIRequest on the bus
     * 
     * @param publisherID
     *            Publisher of the event
     * @param msg
     *            Message to be sent
     * 
     */
    public void sendMessage(String publisherID, UIRequest msg) {
	assessContentSerialization(msg);
	if (publisherID != null
		&& publisherID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    super.sendMessage(publisherID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()), new Message(MessageType.request, msg));
    }

    /**
     * Standard implementation of unregister from AbstractBus will not be used
     * here and simply do nothing.
     */
    public void unregister(String id, BusMember member) {

    }

    /**
     * @see org.universAAL.middleware.ui.UIBus#unregister(java.lang.String,
     *      org.universAAL.middleware.ui.UICaller)
     */
    public void unregister(String publisherID, UICaller publisher) {
	if (publisherID != null
		&& publisherID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
	    super.unregister(publisherID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()), publisher);
    }

    /**
     * @see org.universAAL.middleware.ui.UIBus#unregister(java.lang.String,
     *      org.universAAL.middleware.ui.UIHandler)
     */
    public void unregister(String subscriberID, UIHandler subscriber) {
	if (subscriberID != null
		&& subscriberID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
	    Object o = registry.getBusMemberByID(subscriberID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()));
	    if (o == subscriber) {
		super.unregister(subscriberID
			.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				.length()), subscriber);
		((UIStrategy) busStrategy).removeRegParams(subscriberID);
	    }
	}
    }

    /**
     * @see org.universAAL.middleware.ui.UIBus#userLoggedIn(java.lang.String,
     *      org.universAAL.middleware.rdf.Resource,
     *      org.universAAL.middleware.owl.supply.AbsLocation)
     */
    public void userLoggedIn(String handlerID, Resource user,
	    AbsLocation loginLocation) {
	if (handlerID != null
		&& handlerID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
	    Object o = registry.getBusMemberByID(handlerID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()));
	    if (o instanceof UIHandler && user != null)
		((UIStrategy) busStrategy).userLoggedIn(user, loginLocation);
	}

    }
}
