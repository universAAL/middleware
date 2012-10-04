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

import java.security.acl.Group;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.sodapop.BusStrategy;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageType;
import org.universAAL.middleware.ui.DialogManager;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.SubdialogTrigger;
import org.universAAL.middleware.ui.rdf.Submit;
import org.universAAL.middleware.util.Constants;

/**
 * @author mtazari
 * 
 *         The strategy of a bus is responsible to handle all messages passing
 *         the local instance. The central method is
 *         "handle(Message msg, String senderID)" that is called for every
 *         message given to the bus.
 */
/**
 * @author eandgrg
 * 
 */
public class UIStrategy extends BusStrategy {

    /**
     * 
     * A subscription is the combination of a filter in form of an
     * UIHandlerProfile and the ID of the subscriber.
     * 
     * @author amarinc
     * 
     */
    private class Subscription {
	String subscriberID;
	UIHandlerProfile filter;

	Subscription(String subscriberID, UIHandlerProfile filter) {
	    this.subscriberID = subscriberID;
	    this.filter = filter;
	}
    }

    public static final String PROP_uAAL_CHANGED_PROPERTY = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "changedProperty";
    public static final String PROP_uAAL_DIALOG_ID = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "dialogID";
    public static final String PROP_uAAL_UI_CALL = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theUICall";
    public static final String PROP_uAAL_UI_HANDLER_ID = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theUIHandler";
    public static final String PROP_uAAL_UI_IS_NEW_REQUEST = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "isNewRequest";
    public static final String PROP_uAAL_UI_USER_INPUT = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "userInput";
    public static final String PROP_uAAL_UI_REMOVE_SUBSCRIPTION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "removeSubscription";
    public static final String PROP_uAAL_UI_SUBSCRIPTION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theSubscription";
    public static final String PROP_uAAL_UI_UPDATED_DATA = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "updatedData";
    public static final String TYPE_uAAL_UI_BUS_COORDINATOR = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "Coordinator";
    public static final String TYPE_uAAL_UI_BUS_NOTIFICATION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "SubscriberNotification";
    public static final String TYPE_uAAL_UI_BUS_SUBSCRIPTION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "Subscription";
    public static final String TYPE_uAAL_UI_MAIN_MENU = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "GetMainMenu";
    public static final String TYPE_uAAL_SUSPEND_DIALOG = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "SuspendDialog";

    private DialogManager dialogManager = null;
    private Vector globalSubscriptions = null;
    private Hashtable runningDialogs = null;
    private String[] theCoordinator = null;
    private Hashtable waitingForCut = null;
    private Hashtable pendingRequests = new Hashtable();

    /**
     * Creates a new instance of the UIStrategy
     * 
     * @param sodapop
     *            SodaPop network instance
     */
    public UIStrategy(SodaPop sodapop) {
	super(sodapop);
    }

    /**
     * 
     * Aborts the dialog with the given ID. If the requester is the
     * dialogManager and/or the publisher (caller) of the dialog the request
     * will be handled directly. Otherwise the request is given to the
     * request-queue.
     * 
     * @param requester
     *            ID of the subscriber that want to abort the dialog
     * @param dialogID
     *            ID of the dialog to abort
     */
    void abortDialog(String requester, String dialogID) {
	BusMember bm = getBusMember(requester);
	if (bm instanceof UICaller && dialogID != null) {
	    UICaller publisher = (UICaller) pendingRequests.remove(dialogID);
	    // only dialog manager & the original publisher are allowed to ask
	    // for abortion of dialogs
	    if (bm == dialogManager || bm == publisher)
		notifyHandler_abortDialog(dialogID, publisher);
	    else if (publisher != null)
		pendingRequests.put(dialogID, publisher);
	}
    }

    /**
     * 
     * This method is responsible to adapt existing dialogs to new environmental
     * conditions.
     * 
     * @param dm
     *            Instance of the DialogManager
     * @param request
     *            The request containing the new content
     * @param changedProp
     *            Changed property from the request
     */
    void adaptationParametersChanged(DialogManager dm, UIRequest request,
	    String changedProp) {
	if (dm != null && dm == dialogManager) {
	    int aux, numInMod = 0, matchResult = UIHandlerProfile.MATCH_LEVEL_FAILED;
	    synchronized (globalSubscriptions) {
		String selectedHandler = null, currentHandler = (String) runningDialogs
			.get(request.getDialogID());
		if (changedProp == null) {
		    // this is a new dialog published to the bus
		    if (pendingRequests.get(request.getDialogID()) == null)
			// the UICaller is the dialog manager
			pendingRequests.put(request.getDialogID(), dm);
		    if (currentHandler != null) {
			// strange situation: duplication dialog ID??!!
			// TODO: a log entry!
			System.out
				.println("??!! strange situation: duplicate dialog ID??!!");
		    }
		} else if (currentHandler == null) {
		    // dialog manager data is inconsistent with my data
		    // TODO: a log entry!
		    System.out
			    .println("??!! dialog manager data is inconsistent with my data??!!");
		}
		for (Iterator i = globalSubscriptions.iterator(); i.hasNext();) {
		    Subscription s = (Subscription) i.next();
		    aux = s.filter.matches(request);
		    if (aux > UIHandlerProfile.MATCH_LEVEL_FAILED) {
			if (s.subscriberID.equals(currentHandler)) {
			    notifyHandler_apChanged(currentHandler, request,
				    changedProp);
			    return;
			}
			int n = s.filter.getNumberOfSupportedInputModalities();
			if (aux > matchResult || n > numInMod) {
			    numInMod = n;
			    matchResult = aux;
			    selectedHandler = s.subscriberID;
			}
		    }
		}
		if (selectedHandler == null) {
		    // TODO: what to do here? At least a log entry
		    LogUtils
			    .logDebug(
				    UIBusImpl.moduleContext,
				    UIStrategy.class,
				    "adaptationParametersChanged",
				    new Object[] { "!!!! no handler could be selected!!!!" },
				    null);
		    return;
		}
		if (currentHandler != null) {
		    Resource collectedData = notifyHandler_cutDialog(
			    currentHandler, request.getDialogID());
		    if (collectedData != null)
			request.setCollectedInput(collectedData);
		    runningDialogs.remove(request.getDialogID());
		}
		runningDialogs.put(request.getDialogID(), selectedHandler);
		notifyHandler_handle(selectedHandler, request);
	    }
	}
    }

    /**
     * 
     * Add new subscriptions to the bus
     * 
     * @param subscriberID
     *            ID of the subscriber
     * @param newSubscription
     *            UIHandlerProfile thats contains the details about the
     *            subscription
     */
    void addRegParams(String subscriberID, UIHandlerProfile newSubscription) {
	if (newSubscription == null)
	    return;

	// only the coordinator saves all the subscriptions
	if (isCoordinator())
	    globalSubscriptions.add(new Subscription(subscriberID,
		    newSubscription));
	// so if not is the coordinator publish the pattern to the bus
	else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_SUBSCRIPTION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, subscriberID);
	    pr.setProperty(PROP_uAAL_UI_SUBSCRIPTION, newSubscription);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.p2p_event, pr);
	    m.setReceivers(theCoordinator);
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * 
     * Indicates that dialog has been finished by the user. Handling of this is
     * done locally in a new thread if the local host is the coordinator.
     * Otherwise the message is send to the coordinator using SodaPop.
     * 
     * @param subscriberID
     *            ID of the subscriber that handles the dialog
     * @param input
     *            the response to send.
     */
    void dialogFinished(final String subscriberID, final UIResponse input) {
	final String dialogID = input.getDialogID();
	// first handle the bus internal handling of this request
	if (isCoordinator()) {
	    // do it in a new thread to make sure that no deadlock will happen
	    new Thread() {
		public void run() {
		    synchronized (globalSubscriptions) {
			if (subscriberID.equals(runningDialogs.get(dialogID))) {
			    runningDialogs.remove(dialogID);
			    dialogManager.dialogFinished(dialogID);
			}
		    }
		}
	    }.start();
	} else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, subscriberID);
	    pr.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.p2p_event, pr);
	    m.setReceivers(theCoordinator);
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * Send a UI Response to the application. Call this message after a
     * {@link Submit} or {@link SubdialogTrigger} was pressed.
     * 
     * @param input
     *            the response to send.
     */
    void notifyUserInput(final UIResponse input) {
	final String dialogID = input.getDialogID();
	// inform the application that user input is ready
	if (input.isForDialogManagerCall()){
		notifyUICaller((UICaller)dialogManager, input);	    
	}
	else {
	    UICaller caller = (UICaller) pendingRequests.get(dialogID);
	    notifyUICaller(caller, input);
	}
    }
    
    /**
     * Notify user input to a specific {@link UICaller}
     * @param caller the UICaller to be notified
     * @param input the UIResponse to send.
     */
    private void notifyUICaller(final UICaller caller, final UIResponse input ){
	if (caller == null) {
	    // some other node is hosting the application
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_USER_INPUT, input);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.p2p_event, pr);
	    sodapop.propagateMessage(bus, m);
	} else
	    caller.handleUIResponse(input);
    }

    /**
     * Only called by the DialogManager. Simply removes dialogs from the active
     * list.
     * 
     * @param dm
     * @param dialogID
     */
    void dialogSuspended(DialogManager dm, String dialogID) {
	if (dialogID != null && dm != null && dm == dialogManager) {
	    // most probably does not need 'synchronized(globalSubscriptions)'
	    runningDialogs.remove(dialogID);
	}
    }

    /**
     * Handle all incoming messages. Every call of this method take place in its
     * own thread.
     * 
     * @param msg
     *            Message to handle
     * @param senderID
     *            ID of the sender of the message
     */
    public void handle(Message msg, String senderID) {
	if (msg == null || !(msg.getContent() instanceof Resource))
	    return;

	// Get the content of the message
	Resource res = (Resource) msg.getContent();
	// Decide from the type of the message how to handle it
	switch (msg.getType().ord()) {
	// If it is of type event this can either be a notification or an
	// UI request
	case MessageType.EVENT:
	    // pass notifications to the according methods
	    if (res.getType().equals(TYPE_uAAL_UI_BUS_NOTIFICATION)) {
		String handlerID = (String) res
			.getProperty(PROP_uAAL_UI_HANDLER_ID);
		UIRequest oe = (UIRequest) res.getProperty(PROP_uAAL_UI_CALL);
		Boolean isNew = (Boolean) res
			.getProperty(PROP_uAAL_UI_IS_NEW_REQUEST);
		if (handlerID == null || oe == null || isNew == null) {
		    // TODO: a log entry!
		    return;
		} else if (isNew.booleanValue()) {
		    oe.setProperty(PROP_uAAL_UI_CALL, Message
			    .trySerializationAsContent(oe));
		    notifyHandler_handle(handlerID, oe);
		    oe.changeProperty(PROP_uAAL_UI_CALL, null);
		} else
		    notifyHandler_apChanged(handlerID, oe, res.getProperty(
			    PROP_uAAL_CHANGED_PROPERTY).toString());
		// handle UI requests
	    }
	    break;
	// handle P2P events
	case MessageType.P2P_EVENT:
	    boolean parametrizedNotification = res.getType().equals(
		    TYPE_uAAL_UI_BUS_NOTIFICATION);
	    if (parametrizedNotification) {
		UIResponse input = (UIResponse) res
			.getProperty(PROP_uAAL_UI_USER_INPUT);
		if (input != null) {
		    UICaller caller = (UICaller) pendingRequests.get(input
			    .getDialogID());
		    if (caller != null) {
			caller.handleUIResponse(input);
			return;
		    }
		}
	    }
	    // handle the bus message that indicates the bus-coordinator
	    if (res.getType().equals(TYPE_uAAL_UI_BUS_COORDINATOR)) {
		if (dialogManager == null
			&& theCoordinator == null
			&& res.getURI().startsWith(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
		    synchronized (this) {
			theCoordinator = new String[] { res.getURI().substring(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
					.length()) };
			notifyAll();
		    }
		}
		// if the local instance is the coordinator it really need to
		// perform the indicated tasks
	    } else if (isCoordinator()) {
		if (res.getType().equals(TYPE_uAAL_UI_MAIN_MENU)) {
		    Resource user = (Resource) res
			    .getProperty(Resource.PROP_uAAL_INVOLVED_HUMAN_USER);
		    AbsLocation loginLocation = (AbsLocation) res
			    .getProperty(UIResponse.PROP_SUBMISSION_LOCATION);
		    dialogManager.getMainMenu(user, loginLocation);
		    // suspend a dialog
		} else if (res.getType().equals(TYPE_uAAL_SUSPEND_DIALOG))
		    suspendDialog((String) res.getProperty(PROP_uAAL_DIALOG_ID));
		// handle subscription messages
		else if (res.getType().equals(TYPE_uAAL_UI_BUS_SUBSCRIPTION)) {
		    String handler = (String) res
			    .getProperty(PROP_uAAL_UI_HANDLER_ID);
		    UIHandlerProfile subscription = (UIHandlerProfile) res
			    .getProperty(PROP_uAAL_UI_SUBSCRIPTION);
		    Boolean removes = (Boolean) res
			    .getProperty(PROP_uAAL_UI_REMOVE_SUBSCRIPTION);
		    // if subscription is null or the removes flag true remove
		    // the subscriptions of the handler or the given one
		    if (handler != null)
			if (subscription != null)
			    if (removes != null && removes.booleanValue())
				removeMatchingRegParams(handler, subscription);
			    else
				addRegParams(handler, subscription);
			else if (removes != null && removes.booleanValue())
			    removeRegParams(handler);
		    // handle P2P notification
		} else if (parametrizedNotification) {
		    final String handler = (String) res
			    .getProperty(PROP_uAAL_UI_HANDLER_ID);
		    final String dialogID = (String) res
			    .getProperty(PROP_uAAL_DIALOG_ID);
		    Resource data = (Resource) res
			    .getProperty(PROP_uAAL_UI_UPDATED_DATA);
		    // Without a valid dialogID we can't do anything
		    if (dialogID != null)
			if (handler == null)
			    if (data == null)
				// here, we are in the case where the original
				// publisher of a dialog has requested to abort
				// the dialog
				notifyHandler_abortDialog(dialogID,
					(UICaller) pendingRequests
						.remove(dialogID));
			    else
				// if we have data but no handler we resume to
				// the given dialog
				resumeDialog(dialogID, data);
			// Otherwise if we got on handler but not data we want
			// to close the dialog
			else if (data == null) {
			    // do it in a new thread to make sure that no
			    // deadlock will happen
			    new Thread() {
				public void run() {
				    synchronized (globalSubscriptions) {
					if (handler.equals(runningDialogs
						.get(dialogID))) {
					    runningDialogs.remove(dialogID);
					    dialogManager
						    .dialogFinished(dialogID);
					}
				    }
				}
			    }.start();
			}
		}
		// This is in case we are not the coordinator
	    } else if (parametrizedNotification) {
		String handlerID = (String) res
			.getProperty(PROP_uAAL_UI_HANDLER_ID);
		String dialogID = (String) res.getProperty(PROP_uAAL_DIALOG_ID);
		if (dialogID == null || handlerID == null)
		    // TODO: a log entry!
		    return;
		// here, we are in the case where the coordinator asks for
		// dialog abort => if the handler and 7 or the original
		// publisher are on this node, then perform accordingly
		UICaller publisher = (UICaller) pendingRequests.get(dialogID);
		if (publisher != null)
		    publisher.dialogAborted(dialogID);
		if (handlerID
			.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
		    handlerID = handlerID
			    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				    .length());
		Object o = getBusMember(handlerID);
		if (o instanceof UIHandler)
		    ((UIHandler) o).cutDialog(dialogID);
	    }
	    break;
	// handle P2P replies (currently only the response for the search of the
	// coordinator
	case MessageType.P2P_REPLY:
	    if (res.getType().equals(TYPE_uAAL_UI_BUS_COORDINATOR)) {
		if (theCoordinator == null
			&& res.getURI().startsWith(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
		    synchronized (this) {
			theCoordinator = new String[] { res.getURI().substring(
				Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
					.length()) };
			notifyAll();
		    }
		}
	    }
	    break;
	// Handle P2P-Requests (currently only if the local instance is the
	// coordinator)
	case MessageType.P2P_REQUEST:
	    if (isCoordinator()
		    && res.getType().equals(TYPE_uAAL_UI_BUS_COORDINATOR)) {
		res = new Resource(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			+ sodapop.getID());
		res.addType(TYPE_uAAL_UI_BUS_COORDINATOR, true);
		UIBusImpl.assessContentSerialization(res);
		sodapop.propagateMessage(bus, msg.createReply(res));
	    }
	    break;
	// Add an user input the pool
	case MessageType.REPLY:
	    if (isCoordinator()
		    && res instanceof UIResponse
		    && waitingForCut.get(((UIResponse) res).getDialogID()) instanceof String) {
		synchronized (waitingForCut) {
		    waitingForCut.put(((UIResponse) res).getDialogID(), res);
		    notifyAll();
		}
	    }
	    break;
	// This request is meant in terms of the user. As response to this
	// request the result of a cut-dialog is propagated as user input
	case MessageType.REQUEST:
	    if (res instanceof UIRequest) {
		if (!msg.isRemote()) {
		    Form f = ((UIRequest) res).getDialogForm();
		    if (f == null) {
			// TODO: ERROR... add a log entry... not allowed!
			return;
		    }
		    if (!f.isMessage()) {
			// remember whom to notify once the response is received
			BusMember sender = getBusMember(senderID);
			if (sender instanceof UICaller)
			    pendingRequests.put(f.getDialogID(), sender);
			else {
			    // TODO: log entry... we shouldn't get here!
			}
		    }
		    if (!isCoordinator()) {
			// just forward it to the coordinator
			msg.setReceivers(theCoordinator);
			sodapop.propagateMessage(bus, msg);
			return;
		    }
		}
		if (isCoordinator()) {
		    // if the coordinator is receiving this messages (no matter
		    // if from a local bus member or forwarded by a peer) we ask
		    // the dialog manager to check if this dialog should be
		    // presented to the user immediately or the DM will queue it
		    // for later;
		    if (dialogManager.checkNewDialog((UIRequest) res)) {
			// as a side effect of checkNewDialog(), the request
			// should have been enriched by the current adaptation
			// parameters

			// keep the original request in its serialized form for
			// possible log entries because the coordinator might
			// locally add other info to the request during
			// matchmaking
			res.setProperty(PROP_uAAL_UI_CALL, msg
				.getContentAsString());
			// we call adaptationParametersChanged() because the
			// matchmaking logic is the same; we needed only to add
			// an 'if' there
			adaptationParametersChanged(dialogManager,
				(UIRequest) res, null);
			// remove the temporary prop not needed
			res.changeProperty(PROP_uAAL_UI_CALL, null);
		    }
		} else {
		    // this is the combination non-coordinator + remote because
		    // non-coordinator + local returns immediately
		    // TODO: we shouldn't get here
		}
	    } else if (!isCoordinator()
		    && res.getType().equals(TYPE_uAAL_UI_BUS_NOTIFICATION)) {
		String handler = (String) res
			.getProperty(PROP_uAAL_UI_HANDLER_ID);
		String dialogID = (String) res.getProperty(PROP_uAAL_DIALOG_ID);
		if (handler != null
			&& dialogID != null
			&& handler
				.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
		    Object o = getBusMember(handler
			    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				    .length()));
		    if (o instanceof UIHandler) {
			UIResponse ie = new UIResponse();
			ie.setProperty(UIResponse.PROP_DIALOG_ID, dialogID);
			Resource userInput = ((UIHandler) o)
				.cutDialog(dialogID);
			if (userInput != null)
			    ie.setProperty(UIResponse.PROP_DIALOG_DATA,
				    userInput);
			UIBusImpl.assessContentSerialization(ie);
			sodapop.propagateMessage(bus, msg.createReply(ie));
		    }
		}
	    }
	    break;
	}
    }

    /**
     * 
     * @return True if the instance of the strategy is the coordinator, false
     *         otherwise
     */
    private boolean isCoordinator() {
	// If dialogManager and the coordinator is not set we need to search for
	// them. Therefore we push messages until we get answer and both
	// variables are set be the handle-method.
	if (dialogManager == null && theCoordinator == null) {
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_UI_BUS_COORDINATOR, true);
	    UIBusImpl.assessContentSerialization(r);
	    Message m = new Message(MessageType.p2p_request, r);
	    synchronized (this) {
		while (dialogManager == null && theCoordinator == null) {
		    sodapop.propagateMessage(bus, m);
		    try {
			wait();
		    } catch (Exception e) {
		    }
		}
	    }
	}
	// If we found the coordinator but the DialogManager is not a part of
	// the local instance, it is not the coordinator
	return dialogManager != null;
    }

    /**
     * 
     * Handle an abortDialog-Notification
     * 
     * @param dialogID
     *            ID of the dialog the notification is about
     */
    private void notifyHandler_abortDialog(String dialogID, UICaller publisher) {
	if (isCoordinator()) {
	    ((UICaller) dialogManager).dialogAborted(dialogID);
	    if (publisher != null && publisher != dialogManager)
		publisher.dialogAborted(dialogID);
	    String handlerID = (String) runningDialogs.remove(dialogID);
	    if (handlerID == null) {
		// TODO: log about inconsistent data!
		return;
	    }
	    String peerID = Constants.extractPeerID(handlerID);
	    if (sodapop.getID().equals(peerID)) {
		Object o = getBusMember(handlerID
			.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
				.length()));
		if (o instanceof UIHandler) {
		    ((UIHandler) o).cutDialog(dialogID);
		}
	    } else {
		Resource pr = new Resource();
		pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
		pr.setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
		pr.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
		UIBusImpl.assessContentSerialization(pr);
		Message m = new Message(MessageType.p2p_event, pr);
		m.setReceivers(new String[] { peerID });
		sodapop.propagateMessage(bus, m);
	    }
	} else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.p2p_event, pr);
	    m.setReceivers(theCoordinator);
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * 
     * Handle a dialogChanged-Notification
     * 
     * @param handlerID
     *            ID of the dialog handler
     * @param request
     *            New content
     * @param changedProp
     *            Property changed in the new content
     */
    private void notifyHandler_apChanged(String handlerID, UIRequest request,
	    String changedProp) {
	String content = (String) request.getProperty(PROP_uAAL_UI_CALL);
	String peerID = Constants.extractPeerID(handlerID);
	// if handler is at local note perform the adaption
	if (sodapop.getID().equals(peerID)) {
	    handlerID = handlerID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length());
	    Object o = getBusMember(handlerID);
	    if (o instanceof UIHandler) {
		LogUtils.logInfo(UIBusImpl.moduleContext, UIStrategy.class,
			"notifyHandler_apChanged",
			new Object[] { "Notified handler ", handlerID, ":\n",
				content }, null);
		if (changedProp != null)
		    ((UIHandler) o).adaptationParametersChanged(request
			    .getDialogID(), changedProp, request
			    .getProperty(changedProp));
	    }
	    // if handler is not the local instance but it is the coordinator
	    // forward the notification to the appropriate node
	} else if (isCoordinator()) {
	    request.changeProperty(PROP_uAAL_UI_CALL, null);
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    pr.setProperty(PROP_uAAL_UI_CALL, request);
	    pr.setProperty(PROP_uAAL_CHANGED_PROPERTY, changedProp);
	    pr.setProperty(PROP_uAAL_UI_IS_NEW_REQUEST, Boolean.FALSE);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.event, pr);
	    m.setReceivers(new String[] { peerID });
	    sodapop.propagateMessage(bus, m);
	} // else
	// TODO: a log entry
    }

    /**
     * 
     * Handle a cutDialog-Notification. In case the local instance is the
     * coordinator it blocks until the dialog has been cut.
     * 
     * @param handlerID
     *            ID of the handler of the dialog
     * @param dialogID
     *            ID of the dialog that need to be cut
     * @return result of the dialog as Resource
     */
    private Resource notifyHandler_cutDialog(String handlerID, String dialogID) {
	String peerID = Constants.extractPeerID(handlerID);
	// if the handler is the local node cut the dialog
	if (sodapop.getID().equals(peerID)) {
	    Object o = getBusMember(handlerID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length()));
	    if (o instanceof UIHandler)
		return ((UIHandler) o).cutDialog(dialogID);
	    // if the handler is not the local node but it is the coordinator
	    // start the cut-dialog and wait for the user-input
	} else if (isCoordinator()) {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    pr.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.request, pr);
	    m.setReceivers(new String[] { peerID });
	    synchronized (waitingForCut) {
		waitingForCut.put(dialogID, handlerID);
		sodapop.propagateMessage(bus, m);
		while (!(waitingForCut.get(handlerID) instanceof UIResponse)) {
		    try {
			wait();
		    } catch (Exception e) {
		    }
		}
		UIResponse ie = (UIResponse) waitingForCut.remove(handlerID);
		return (Resource) ie.getProperty(UIResponse.PROP_DIALOG_DATA);
	    }
	} // else
	// TODO: a log entry
	return null;
    }

    /**
     * 
     * Handle output-notifications
     * 
     * @param handlerID
     *            ID of the handler of the request
     * @param request
     *            Request to handle
     */
    private void notifyHandler_handle(String handlerID, UIRequest request) {
	String content = (String) request.getProperty(PROP_uAAL_UI_CALL);
	String peerID = Constants.extractPeerID(handlerID);
	// if the handler is the local node handle the output
	if (sodapop.getID().equals(peerID)) {
	    handlerID = handlerID
		    .substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			    .length());
	    Object o = getBusMember(handlerID);
	    if (o instanceof UIHandler) {
		LogUtils.logInfo(UIBusImpl.moduleContext, UIStrategy.class,
			"notifyHandler_handle",
			new Object[] { "Notified handler ", handlerID, ":\n",
				content }, null);
		((UIHandler) o).handleUICall(request);
	    }
	    // if is not but the local coordinator forward the message to the
	    // appropriate node
	} else if (isCoordinator()) {
	    request.changeProperty(PROP_uAAL_UI_CALL, null);
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    pr.setProperty(PROP_uAAL_UI_CALL, request);
	    pr.setProperty(PROP_uAAL_UI_IS_NEW_REQUEST, Boolean.TRUE);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.event, pr);
	    m.setReceivers(new String[] { peerID });
	    sodapop.propagateMessage(bus, m);
	} // else
	// TODO: a log entry
    }

    /**
     * 
     * Remove a subscription from the bus
     * 
     * @param subscriberID
     *            ID of the subscriber that want to remove a subscription
     * @param oldSubscription
     *            Subscription to remove
     */
    void removeMatchingRegParams(String subscriberID,
	    UIHandlerProfile oldSubscription) {
	if (subscriberID == null || oldSubscription == null)
	    return;

	// All subscriptions are saved in the coordinator
	if (isCoordinator()) {
	    synchronized (globalSubscriptions) {
		for (Iterator i = globalSubscriptions.iterator(); i.hasNext();) {
		    Subscription s = (Subscription) i.next();
		    if (s.subscriberID.equals(subscriberID)
			    && oldSubscription.matches(s.filter))
			i.remove();
		}
	    }
	    // send the request to the coordinator otherwise
	} else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_SUBSCRIPTION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, subscriberID);
	    pr.setProperty(PROP_uAAL_UI_SUBSCRIPTION, oldSubscription);
	    pr.setProperty(PROP_uAAL_UI_REMOVE_SUBSCRIPTION, Boolean.TRUE);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.p2p_event, pr);
	    m.setReceivers(theCoordinator);
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * 
     * Remove all subscriptions from the given subscriber
     * 
     * @param subscriberID
     *            ID of the subscriber we want to remove all subscriptions
     */
    void removeRegParams(String subscriberID) {
	if (subscriberID == null)
	    return;

	// All subscriptions are saved in the coordinator
	if (isCoordinator()) {
	    synchronized (globalSubscriptions) {
		for (Iterator i = globalSubscriptions.iterator(); i.hasNext();) {
		    Subscription s = (Subscription) i.next();
		    if (s.subscriberID.equals(subscriberID))
			i.remove();
		}
	    }
	    // send the request to the coordinator otherwise
	} else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_SUBSCRIPTION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, subscriberID);
	    pr.setProperty(PROP_uAAL_UI_REMOVE_SUBSCRIPTION, Boolean.TRUE);
	    UIBusImpl.assessContentSerialization(pr);
	    Message m = new Message(MessageType.p2p_event, pr);
	    m.setReceivers(theCoordinator);
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * Called only when an application is finished with a subdialog and wants to
     * resume the original dialog passing the changed dialog data.
     * 
     * @param dialogID
     *            ID of the dialog we want to resume
     * @param dialogData
     *            Original data of the dialog we are going to resume
     */
    void resumeDialog(String dialogID, Resource dialogData) {
	if (isCoordinator()) {
	    UIRequest oe = dialogManager.getSuspendedDialog(dialogID);
	    if (oe != null) {
		oe.setCollectedInput(dialogData);
		adaptationParametersChanged(dialogManager, oe, null);
	    } else {
		// trust the dialog manager: either the dialog was aborted
		// previously
		// or it has less priority than the running one
	    }
	} else {
	    Resource res = new Resource();
	    res.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    res.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    res.setProperty(PROP_uAAL_UI_UPDATED_DATA, dialogData);
	    UIBusImpl.assessContentSerialization(res);
	    Message m = new Message(MessageType.p2p_event, res);
	    m.setReceivers(theCoordinator);
	    sodapop.propagateMessage(bus, m);
	}
    }

    /**
     * 
     * Set the Dialogmanager of the bus. There must be only one instance of this
     * in the whole remote system.
     * 
     * @param dm
     *            Instance of the Dialogmanager
     */
    void setDialogManager(DialogManager dm) {
	if (dm == null || dialogManager != null || theCoordinator != null)
	    // TODO: a log entry!?!
	    return;

	globalSubscriptions = new Vector();
	runningDialogs = new Hashtable();
	waitingForCut = new Hashtable(2);

	Resource res = new Resource(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		+ sodapop.getID());
	res.addType(TYPE_uAAL_UI_BUS_COORDINATOR, true);

	synchronized (this) {
	    dialogManager = dm;
	    UIBusImpl.assessContentSerialization(res);
	    sodapop.propagateMessage(bus, new Message(MessageType.p2p_event,
		    res));
	    notifyAll();
	}
    }

    /**
     * Called only when a UI handler has called dialogFinished with an instance
     * of {@link org.universAAL.middleware.ui.rdf.SubdialogTrigger} so that we
     * must only notify to suspend this dialog until the original publisher
     * calls 'resume'.
     * 
     * @param dialogID
     *            ID of the dialog to suspend
     */
    void suspendDialog(String dialogID) {
	if (dialogID == null)
	    return;

	if (isCoordinator()) {
	    dialogManager.suspendDialog(dialogID);
	    // synchronized(globalSubscriptions) {
	    // String currentHandler = (String) runningDialogs.remove(dialogID);
	    // if (currentHandler != null) {
	    // String peerID = Constants.extractPeerID(currentHandler);
	    // if (sodapop.getID().equals(peerID)) {
	    // Object o = getBusMember(currentHandler.substring(
	    // Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
	    // if (o instanceof UIHandler)
	    // ((UIHandler) o).cutDialog(dialogID);
	    // } else {
	    // Resource pr = new Resource();
	    // pr.addType(TYPE_uAAL_SUSPEND_DIALOG, true);
	    // pr.setProperty(PROP_uAAL_UI_HANDLER_ID, currentHandler);
	    // pr.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    // Message m = new Message(MessageType.p2p_event, pr);
	    // m.setReceivers(new String[] {peerID});
	    // sodapop.propagateMessage(bus, m);
	    // }
	    // }
	    // }
	} else {
	    Resource res = new Resource();
	    res.addType(TYPE_uAAL_SUSPEND_DIALOG, true);
	    res.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    UIBusImpl.assessContentSerialization(res);
	    Message m = new Message(MessageType.p2p_event, res);
	    m.setReceivers(theCoordinator);
	    sodapop.propagateMessage(bus, m);
	}
    }

    void userLoggedIn(Resource user, AbsLocation loginLocation) {
	if (isCoordinator()) {
	    dialogManager.getMainMenu(user, loginLocation);
	    // }
	} else {
	    Resource res = new Resource();
	    res.addType(TYPE_uAAL_UI_MAIN_MENU, true);
	    res.setProperty(Resource.PROP_uAAL_INVOLVED_HUMAN_USER, user);
	    if (loginLocation != null)
		res.setProperty(UIResponse.PROP_SUBMISSION_LOCATION,
			loginLocation);
	    UIBusImpl.assessContentSerialization(res);
	    Message m = new Message(MessageType.p2p_event, res);
	    m.setReceivers(theCoordinator);
	    sodapop.propagateMessage(bus, m);
	}
    }
}
