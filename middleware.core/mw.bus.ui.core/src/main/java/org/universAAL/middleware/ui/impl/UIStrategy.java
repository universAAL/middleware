/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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
package org.universAAL.middleware.ui.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.IDialogManager;
import org.universAAL.middleware.ui.IUIBus;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.SubdialogTrigger;
import org.universAAL.middleware.ui.rdf.Submit;

/**
 * The strategy of a {@link IUIBus} is responsible to handle all messages
 * passing the local instance. The central method is
 * "handle(Message msg, String senderID)" that is called for every message given
 * to the bus.
 * 
 * It provides mechanism to match user preferences, abilities and some other
 * important information with {@link UIHandlerProfile} that describes
 * {@link UIHandler} capabilities. Based upon this matching the appropriate
 * {@link UIHandler} to communicate with the user is selected. <center>
 * <table align="left" border="1" cellpadding="1" cellspacing="1" style="width: 70%;">
 * <caption>UI Bus Messages summary</caption> <thead>
 * <tr>
 * <th scope="col">Message</th>
 * <th scope="col">Event MsgType</th>
 * <th scope="col">Parameters</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>request</td>
 * <td>Request (Not event)</td>
 * <td>UIRequest (not as parameter, as the message resource)</td>
 * </tr>
 * <tr>
 * <td>abort</td>
 * <td>bus_Notification</td>
 * <td>Dialog_ID</td>
 * </tr>
 * <tr>
 * <td>cut</td>
 * <td>bus_Notification</td>
 * <td>Dialog_ID; Handler_ID</td>
 * </tr>
 * <tr>
 * <td>newHandlerProfile</td>
 * <td>bus_Subscription</td>
 * <td>Handler_ID; Subscription</td>
 * </tr>
 * <tr>
 * <td>finishDialog</td>
 * <td>bus_Notification</td>
 * <td>Handler_ID; Dialog_ID</td>
 * </tr>
 * <tr>
 * <td>notifyUserInput</td>
 * <td>bus_Notification</td>
 * <td>User_Input</td>
 * </tr>
 * <tr>
 * <td>removeMatchingProfile</td>
 * <td>bus_Subscription</td>
 * <td>Handler_ID; Subscription; Remove_Subscription=true</td>
 * </tr>
 * <tr>
 * <td>removeProfile</td>
 * <td>bus_Subscription</td>
 * <td>Handler_ID;&nbsp;Remove_Subscription=true</td>
 * </tr>
 * <tr>
 * <td>resumeDialog</td>
 * <td>bus_Notification</td>
 * <td>Dialog_ID; Updated_Data</td>
 * </tr>
 * <tr>
 * <td>suspendDialog</td>
 * <td>bus_Notification</td>
 * <td>Dialog_ID</td>
 * </tr>
 * <tr>
 * <td>userLogOn</td>
 * <td>main_Menu</td>
 * <td>Involved_User; [Location]</td>
 * </tr>
 * <tr>
 * <td>notifyHandler</td>
 * <td>bus_Notification</td>
 * <td>Handler_ID; UI_Request; Is_New_Request</td>
 * </tr>
 * <tr>
 * <td>notifyCoordinator</td>
 * <td>UI_Bus_Coordinator</td>
 * <td>&nbsp;</td>
 * </tr>
 * </tbody>
 * </table>
 * </center> <center> <img src="doc-files/UIStrategy.png"
 * alt="UIStrategy messages" width="70%"/> </center>
 * 
 * @author mtazari
 * @author eandgrg
 * 
 */
public class UIStrategy extends BusStrategy {

    /**
     * 
     * A subscription is the combination of a uiHandlerProfileFilter in form of
     * an UIHandlerProfile and the ID of the subscriber.
     * 
     * @author amarinc
     * 
     */
    private class Subscription {
	/**
	 * {@link UIHandler} id
	 */
	String subscriberID;

	/**
	 * {@link UIHandlerProfile}
	 */
	UIHandlerProfile uiHandlerProfileFilter;

	Subscription(String subscriberID, UIHandlerProfile filter) {
	    this.subscriberID = subscriberID;
	    this.uiHandlerProfileFilter = filter;
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
    /**
     * reference to resource property that is true if it is a new UIRequest and
     * false if it is not a new UIRequest (e.g. adaptation parameters changed)
     */
    public static final String PROP_uAAL_UI_IS_NEW_REQUEST = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "isNewRequest";
    /**
     * input comes from the user
     */
    public static final String PROP_uAAL_UI_USER_INPUT = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "userInput";

    /**
     * References subscription that is {@link UIHandlerProfile} which is to be
     * sent to the coordinator (if this instance is not the coordinator) upon
     * registering a new subscription or removing an old one
     */
    public static final String PROP_uAAL_UI_SUBSCRIPTION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theSubscription";
    /**
     * References changed (updated) dialog data to be passed to coordinator
     */
    public static final String PROP_uAAL_UI_UPDATED_DATA = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "updatedData";
    /**
     * References the property that is true for the coordinator, when it
     * announces its role via p2p_event message.
     */
    public static final String TYPE_uAAL_UI_BUS_COORDINATOR = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "Coordinator";

    /**
     * <table>
     * <tr>
     * <td>References the property that is true when it is ui bus notification
     * or in other words when the message p2p_event has to:</td>
     * </tr>
     * <tr>
     * <td>a) broadcast a message (containing user input) to all instances of
     * the UI bus (to find a matching {@link UICaller}</td>
     * </tr>
     ** 
     * <tr>
     * <td>b) carry dialog finished events to the coordinator (if this is not
     * the coordinator)</td>
     * </tr>
     ** 
     * <tr>
     * <td>c) send abortDialog-Notification</td>
     * </tr>
     ** 
     * <tr>
     * <td>d) notify {@link UIHandler} if it is not on this local (coordinator)
     * instance that adaptation parameters changed or that dialog has to be
     * cut/handled/resumed</td>
     * </tr>
     * </table>
     */
    public static final String TYPE_uAAL_UI_BUS_NOTIFICATION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "SubscriberNotification";
    /**
     * References property set to true only when this instance is not the
     * coordinator and {@link UIHandler} is to be un-subscribed from the
     * {@link IUIBus} whether by only stating ID or both ID and
     * {@link UIHandlerProfile}
     */
    public static final String PROP_uAAL_UI_REMOVE_SUBSCRIPTION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "removeSubscription";

    /**
     * References subscription that is true when the new subscription
     * {@link UIHandlerProfile} is added to the bus or removed from it (and needs to be
     * forwarded to the coordinator). Additional property
     * PROP_uAAL_UI_REMOVE_SUBSCRIPTION says whether it is addition or removal.
     */
    public static final String TYPE_uAAL_UI_BUS_SUBSCRIPTION = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "Subscription";
    /**
     * Sent when {@link User} requested main menu (has logged in) and this is
     * not the coordinator so main menu request has to be forwarded to the
     * coordinator node.
     */
    public static final String TYPE_uAAL_UI_MAIN_MENU = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "GetMainMenu";
    public static final String TYPE_uAAL_SUSPEND_DIALOG = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "SuspendDialog";

    private IDialogManager dialogManager = null;
    /**
     * pairs of (user uri, handler id) where user and it's last used Handler are
     * stored
     */
    private Map<String, String> lastUsedHandler = null;

    /**
     * last used handler weight is same as the dialog privacy's one.
     */
    public static final int LAST_USED_HANDLER_MATCH_LEVEL_ADDITION = UIHandlerProfile.MATCH_DIALOG_PRIVACY;

    /**
     * list of {@link Subscription}s containing (handler id, handler profile)
     * pairs
     */
    private List<Subscription> globalSubscriptions = null;

    /**
     * runningDialogs map contains (dialog id,handler id) pair (Handler is the
     * one that is currently assigned with the UIRequest)
     */
    private Map<String, String> runningDialogs = null;
    private PeerCard theCoordinator = null;
    private Hashtable<String, Object> waitingForCut = null;
    private Map<String, UICaller> pendingRequests = new Hashtable<String, UICaller>();

    /**
     * Creates a new instance of the {@link UIStrategy}
     */
    public UIStrategy(CommunicationModule commModule) {
	super(commModule, "UI Bus Strategy");
    }

    /**
     * 
     * Aborts the dialog with the given ID. If the requester is the
     * {@link IDialogManager} and/or the {@link UICaller} of the dialog the
     * request will be handled directly. Otherwise the request is given to the
     * request-queue. Only {@link IDialogManager} & the original
     * {@link UICaller} are allowed to ask for abortion of dialogs.
     * 
     * @param requester
     *            ID of the {@link UICaller} that wants to abort the dialog
     * @param dialogID
     *            ID of the dialog to abort
     */
    void abortDialog(String requester, String dialogID) {
	BusMember busMember = getBusMember(requester);
	if (busMember instanceof UICaller && dialogID != null) {
	    UICaller uiCaller = pendingRequests.remove(dialogID);
	    // only dialog manager & the original publisher (uicaller) are
	    // allowed to ask for abortion of dialogs
	    if (busMember == dialogManager || busMember == uiCaller) {
		notifyHandler_abortDialog(dialogID, uiCaller);
	    } else if (uiCaller != null) {
		pendingRequests.put(dialogID, uiCaller);
	    }
	}
    }

    /**
     * 
     * This method is responsible for adapting existing dialogs to new
     * environmental conditions.
     * 
     * @param dm
     *            Instance of the {@link IDialogManager}
     * @param uiRequest
     *            The request containing the new content
     * @param changedProp
     *            Changed property from the request
     */
    void adaptationParametersChanged(IDialogManager dm, UIRequest uiRequest,
	    String changedProp) {
	if (dm != null && dm == dialogManager) {
	    int tempMatchingDegree;
	    int maxMatchDegree = UIHandlerProfile.MATCH_LEVEL_FAILED;

	    synchronized (globalSubscriptions) {
		String selectedHandler = null;
		String currentHandler = runningDialogs.get(uiRequest
			.getDialogID());
		if (changedProp == null) {
		    // this is a new dialog published to the bus
		    // Or a dialog is being resumed by the DM.
		    if (pendingRequests.get(uiRequest.getDialogID()) == null) {
			// if req is not in the pendingRequests (in dm queue)
			// add it there
			pendingRequests.put(uiRequest.getDialogID(),
				(UICaller) dm);
			LogUtils
				.logDebug(
					busModule,
					UIStrategy.class,
					"adaptationParametersChanged",
					new Object[] { "ui.dm has published new dialog on the ui bus !" },
					null);
		    }
		    if (currentHandler != null) {
			LogUtils
				.logWarn(
					busModule,
					UIStrategy.class,
					"adaptationParametersChanged",
					new Object[] {
						"strange situation: duplication dialog ID?\n",
						uiRequest }, null);
		    }
		} else if (currentHandler == null) {
		    LogUtils
			    .logError(
				    busModule,
				    UIStrategy.class,
				    "adaptationParametersChanged",
				    new Object[] { "Current UI Handler could not be determined from running dialogs. Inconsistent data between ui.dm data and UIStrategy data!" },
				    null);
		}

		// iterate through all UIHandlers
		for (Subscription subscription : globalSubscriptions) {
		    tempMatchingDegree = subscription.uiHandlerProfileFilter
			    .getMatchingDegree(uiRequest);
		    LogUtils
			    .logDebug(
				    busModule,
				    UIStrategy.class,
				    "adaptationParametersChanged",
				    new Object[] { "\n START+++++++++++++++++++++++++++++++++++++++++++++++\n Handler with subscription id: "
					    + subscription
					    + ", has matching degree: "
					    + tempMatchingDegree
					    + "\n 2+++++++++++++++++++++++++++++++++++++++++++++++"
					    + "\n UIHandler profile:\n "
					    + subscription.uiHandlerProfileFilter
						    .toStringRecursive()
					    + "\n 3+++++++++++++++++++++++++++++++++++++++++++++++"
					    + "\n uiRequest that is getting matched.\n Addressed user: "
					    + uiRequest.getAddressedUser()
						    .getURI()
					    + "\n Modality: "
					    + uiRequest
						    .getProperty(UIRequest.PROP_PRESENTATION_MODALITY)
					    + "\n Presentation location: "
					    + uiRequest
						    .getPresentationLocation()
					    + "\n STOP+++++++++++++++++++++++++++++++++++++++++++++++\n " },
				    null);

		    if (tempMatchingDegree > UIHandlerProfile.MATCH_LEVEL_FAILED) {
			if (subscription.subscriberID.equals(currentHandler)) {
			    // notify UIHandler that currently "has" this
			    // request. Notify only if there is prop that
			    // changed.
			    if (changedProp != null) {
				notifyHandler_apChanged(currentHandler,
					uiRequest, changedProp);
				return;
			    }
			}
			if (subscription.subscriberID.equals(lastUsedHandler
				.get(uiRequest.getAddressedUser().getURI()))) {
			    // if currently observed handler also the one last
			    // used by the user then increase his matching
			    // degree a bit
			    tempMatchingDegree += LAST_USED_HANDLER_MATCH_LEVEL_ADDITION;
			}
			if (tempMatchingDegree > maxMatchDegree) {
			    maxMatchDegree = tempMatchingDegree;
			    selectedHandler = subscription.subscriberID;
			}
		    }
		}
		if (selectedHandler == null) {
		    // No UI Handler can be selected so put dialog to suspended
		    // dialogues queue. DM is repeatedly trying to
		    // show something and if it there is nothing else to show it
		    // will check pending dialogues also (at that time new
		    // UIHandler can be added or existing one can change reg
		    // parameters or adaptation parameters can change)
		    dm.suspendDialog(uiRequest.getDialogID());
		    LogUtils
			    .logWarn(
				    busModule,
				    UIStrategy.class,
				    "adaptationParametersChanged",
				    new Object[] {
					    "No UI Handler could be selected so dialog is suspended for now.\n",
					    uiRequest }, null);
		    return;
		}

		LogUtils
			.logDebug(
				busModule,
				UIStrategy.class,
				"adaptationParametersChanged",
				new Object[] { "Handler with id: "
					+ selectedHandler
					+ ", and matching degree: "
					+ maxMatchDegree
					+ " was selected as best. Note: last used handler additional weight= "
					+ LAST_USED_HANDLER_MATCH_LEVEL_ADDITION },
				null);

		if (currentHandler != null) {
		    Resource collectedData = notifyHandler_cutDialog(
			    currentHandler, uiRequest.getDialogID());
		    if (collectedData != null) {
			uiRequest.setCollectedInput(collectedData);
		    }
		    runningDialogs.remove(uiRequest.getDialogID());
		}
		runningDialogs.put(uiRequest.getDialogID(), selectedHandler);
		notifyHandler_handle(selectedHandler, uiRequest);
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
	if (newSubscription == null) {
	    return;
	}

	// only the coordinator saves all the subscriptions
	if (isCoordinator()) {
	    globalSubscriptions.add(new Subscription(subscriberID,
		    newSubscription));
	} else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_SUBSCRIPTION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, subscriberID);
	    pr.setProperty(PROP_uAAL_UI_SUBSCRIPTION, newSubscription);
	    sendMessageToCoordinator(MessageType.p2p_event, pr);
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
	if (input == null) {
	    LogUtils
		    .logWarn(
			    busModule,
			    UIStrategy.class,
			    "dialogFinished",
			    new Object[] { "Dialog is finished by the user but UI Handler sent empty UI Response!" },
			    null);
	    return;
	}
	// remember last used handler for the user (important when selecting the
	// Handler)
	lastUsedHandler.put(input.getUser().getURI(), subscriberID);
	final String dialogID = input.getDialogID();
	// first handle the bus internal handling of this request
	if (isCoordinator()) {
	    // do it in a new thread to make sure that no deadlock will happen
	    new Thread("UI Bus Strategy - Handling dialog finished") {
		@Override
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
	    sendMessageToCoordinator(MessageType.p2p_event, pr);
	}
    }

    /**
     * Send a {@link UIResponse} to the application. Call this message after a
     * {@link Submit} or {@link SubdialogTrigger} was pressed.
     * 
     * @param input
     *            the response to send.
     */
    void notifyUserInput(final UIResponse input) {
	final String dialogID = input.getDialogID();
	// inform the application that user input is ready
	if (input.isForDialogManagerCall()) {
	    notifyUICaller((UICaller) dialogManager, input);
	} else {
	    UICaller caller = pendingRequests.get(dialogID);
	    notifyUICaller(caller, input);
	}
    }

    /**
     * Notify user input to a specific {@link UICaller}
     * 
     * @param caller
     *            the UICaller to be notified
     * @param input
     *            the UIResponse to send.
     */
    private void notifyUICaller(final UICaller caller, final UIResponse input) {
	if (caller == null) {
	    // some other node is hosting the application that we do not know
	    // => broadcast a message to all instances of the UI bus
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_USER_INPUT, input);
	    ((UIBusImpl) bus).assessContentSerialization(pr);
	    BusMessage m = new BusMessage(MessageType.p2p_event, pr, bus);
	    send(m);
	} else {
	    caller.handleUIResponse(input);
	}
    }

    /**
     * Only called by the {@link IDialogManager}. Simply removes dialogs from
     * the active list.
     * 
     * @param dm
     * @param dialogID
     */
    void dialogSuspended(IDialogManager dm, String dialogID) {
	if (dialogID != null && dm != null && dm == dialogManager) {
	    // most probably does not need 'synchronized(globalSubscriptions)'
	    runningDialogs.remove(dialogID);
	}
    }

    @Override
    protected void handleDeniedMessage(BusMessage message, String senderID) {
	// TODO Auto-generated method stub
    }

    /**
     * Handle all incoming messages. Every call of this method takes place in
     * its own thread.
     * 
     * @param message
     *            Message to handle
     * @param senderID
     *            ID of the sender of the message
     */
    @Override
    public void handle(BusMessage message, String senderID) {
	if (isContentResource(message)) {
	    Resource resource = (Resource) message.getContent();
	    switch (message.getType().ord()) {
	    case MessageType.EVENT:
		handleNotificationOrUIRequest(resource);
		break;
	    case MessageType.P2P_EVENT:
		handleP2PEvent(resource);
		break;
	    case MessageType.P2P_REPLY:
		handleSearchForCoordinator(resource);
		break;
	    case MessageType.P2P_REQUEST:
		handleP2PRequestIfCoordinator(message, resource);
		break;
	    case MessageType.REPLY:
		addUserInputToPool(resource);
		break;
	    case MessageType.REQUEST:
		handleRequestWithCutDialogForUserInput(message, senderID);
		break;
	    }
	} else {
	    LogUtils
		    .logError(
			    busModule,
			    UIStrategy.class,
			    "handle",
			    new Object[] { "Cannot handle message since there is no reference to it or the content of the message is wrong!!" },
			    null);
	}
    }

    private boolean isContentResource(BusMessage message) {
	return message != null && message.getContent() instanceof Resource;
    }

    /**
     * This request is meant in terms of the user. As response to this request
     * the result of a cut-dialog is propagated as user input
     */
    private void handleRequestWithCutDialogForUserInput(BusMessage message,
	    String senderID) {
	Resource resource = (Resource) message.getContent();
	if (resource instanceof UIRequest) {
	    handleUIRequest(message, senderID);
	} else if (!isCoordinator() && isUIBusNotification(resource)) {
	    handleUIBusNotificationWithDifferentCoordinator(message);
	}
    }

    private void handleUIBusNotificationWithDifferentCoordinator(
	    BusMessage message) {
	Resource resource = (Resource) message.getContent();
	String handler = (String) resource.getProperty(PROP_uAAL_UI_HANDLER_ID);
	String dialogID = (String) resource.getProperty(PROP_uAAL_DIALOG_ID);
	if (handler != null && dialogID != null) {
	    Object o = getBusMember(handler);
	    if (o instanceof UIHandler) {
		UIResponse ie = new UIResponse();
		ie.setProperty(UIResponse.PROP_DIALOG_ID, dialogID);
		Resource userInput = ((UIHandler) o).cutDialog(dialogID);
		if (userInput != null) {
		    ie.setProperty(UIResponse.PROP_DIALOG_DATA, userInput);
		}
		((UIBusImpl) bus).assessContentSerialization(ie);
		send(message.createReply(ie));
	    }
	}
    }

    private void handleUIRequest(BusMessage message, String senderID) {
	Resource resource = (Resource) message.getContent();
	if (!message.senderResidesOnDifferentPeer()) {
	    Form form = ((UIRequest) resource).getDialogForm();
	    if (form != null) {
		if (!form.isMessage()) {
		    storeReceiverOfForm(senderID, form);
		}
		if (!isCoordinator()) {
		    forwardMessageToCoordinator(message);
		    return;
		}
	    } else {
		LogUtils
			.logError(
				busModule,
				UIStrategy.class,
				"handleUIRequest",
				new Object[] { "Dialog form of the UIRequest could not be determined! Not allowed!" },
				null);
		return;
	    }
	}
	if (isCoordinator()) {
	    askDialogManagerForPresentation(message);
	} else {
	    LogUtils
		    .logError(
			    busModule,
			    UIStrategy.class,
			    "handleUIRequest",
			    new Object[] { "combination non-coordinator + remote. We shouldn't get here!!" },
			    null);
	}
    }

    private void forwardMessageToCoordinator(BusMessage message) {
	message.setReceiver(theCoordinator);
	send(message);
    }

    /**
     * remember whom to notify once the response is received
     */
    private void storeReceiverOfForm(String receiverID, Form form) {
	BusMember receiver = getBusMember(receiverID);
	if (receiver instanceof UICaller) {
	    pendingRequests.put(form.getDialogID(), (UICaller) receiver);
	} else {
	    LogUtils
		    .logError(
			    busModule,
			    UIStrategy.class,
			    "storeReceiverOfForm",
			    new Object[] { "Method is empty and we shouldn't be here!!" },
			    null);
	}
    }

    /**
     * If the coordinator is receiving this messages (no matter if from a local
     * bus member or forwarded by a peer) we ask the dialog manager to check if
     * this dialog should be presented to the user immediately or the DM will
     * queue it for later;
     * 
     * As a side effect of {@link IDialogManager#checkNewDialog(UIRequest)}, the
     * request should have been enriched by the current adaptation parameters
     */
    private void askDialogManagerForPresentation(BusMessage message) {
	Resource resource = (Resource) message.getContent();
	if (dialogManager.checkNewDialog((UIRequest) resource)) {

	    keepOriginalRequestForLogging(message);
	    // we call adaptationParametersChanged() because the
	    // matchmaking logic is the same; we needed only to add
	    // an 'if' there
	    adaptationParametersChanged(dialogManager, (UIRequest) resource,
		    null);
	    removeTemporaryProperty(resource);
	} else {
	    LogUtils
		    .logDebug(
			    busModule,
			    UIStrategy.class,
			    "askDialogManagerForPresentation",
			    new Object[] { "The UI Bus ignores the request because it trusts that the Dialog Manager will keep the request in a queue of suspended dialogs and will re-activate it whenever appropriate." },
			    null);
	}
    }

    private void removeTemporaryProperty(Resource resource) {
	removeProperty(resource, PROP_uAAL_UI_CALL);
    }

    private void removeProperty(Resource resource, String property) {
	resource.changeProperty(property, null);
    }

    /**
     * 
     * Keep the original request in its serialized form for possible log entries
     * because the coordinator might locally add other info to the request
     * during matchmaking
     * 
     * @param message
     *            {@link BusMessage} to keep in serialized form
     */

    private void keepOriginalRequestForLogging(BusMessage message) {
	((Resource) message.getContent()).setProperty(PROP_uAAL_UI_CALL,
		message.getContentAsString());
    }

    /**
     * @param res
     *            {@link Resource}
     * @return true if the type of the given {@link Resource} is
     *         TYPE_uAAL_UI_BUS_NOTIFICATION
     */
    private boolean isUIBusNotification(Resource res) {
	return res.getType().equals(TYPE_uAAL_UI_BUS_NOTIFICATION);
    }

    private void addUserInputToPool(Resource res) {
	if (isCoordinator()
		&& res instanceof UIResponse
		&& waitingForCut.get(((UIResponse) res).getDialogID()) instanceof String) {
	    synchronized (waitingForCut) {
		waitingForCut.put(((UIResponse) res).getDialogID(), res);
		notifyAll();
	    }
	}
    }

    /**
     * Handle P2P-Requests (currently only if the local instance is the
     * coordinator)
     */
    private void handleP2PRequestIfCoordinator(BusMessage msg, Resource res) {
	if (isCoordinator()
		&& res.getType().equals(TYPE_uAAL_UI_BUS_COORDINATOR)) {
	    res = new Resource(bus.getURI());
	    res.addType(TYPE_uAAL_UI_BUS_COORDINATOR, true);
	    ((UIBusImpl) bus).assessContentSerialization(res);
	    send(msg.createReply(res));
	}
    }

    /**
     * Handle P2P-Replies (currently only the response for the search of the
     * coordinator)
     */
    private void handleSearchForCoordinator(Resource res) {
	if (res.getType().equals(TYPE_uAAL_UI_BUS_COORDINATOR)) {
	    if (theCoordinator == null) {
		synchronized (this) {
		    theCoordinator = AbstractBus.getPeerFromBusResourceURI(res
			    .getURI());
		    notifyAll();
		}
	    }
	}
    }

    private void handleP2PEvent(Resource res) {
	// the highest priority should be given to handling user input
	// this happens when the message content is a "parametrized" one,
	// with the user input as the parameter
	boolean isParametrizedUIBusNotification = isUIBusNotification(res);
	if (isParametrizedUIBusNotification) {
	    UIResponse input = (UIResponse) res
		    .getProperty(PROP_uAAL_UI_USER_INPUT);
	    if (input != null) {
		// the coordinator is telling me there is input for a
		// caller; let's see if the caller is on this node
		UICaller caller = pendingRequests.get(input.getDialogID());
		if (caller != null) {
		    // zes, the caller is a member of this instance, so I
		    // must notify it
		    caller.handleUIResponse(input);
		    return;
		}
	    }
	}

	// the next priority is to wake up all threads waiting for the
	// discovery of the coordinator instance
	// handle the bus message that indicates the bus-coordinator: is it
	// a question or an announcement
	if (res.getType().equals(TYPE_uAAL_UI_BUS_COORDINATOR)) {
	    // this should be a notification from the side of the
	    // coordinator announcing its role
	    // => this is the announcement
	    if (dialogManager == null // I am not the coordinator
		    && theCoordinator == null // I do not know the
	    // coordinator
	    // this check is probably not needed at all
	    // && res.getURI().startsWith(
	    // Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)
	    ) {
		// oh, I didn't know who is the coordinator => store this
		// info
		synchronized (this) {
		    theCoordinator = AbstractBus.getPeerFromBusResourceURI(res
			    .getURI());
		    notifyAll();
		}
	    }
	    // in other cases, ignore this message
	    return;
	}

	if (isCoordinator()) {
	    // the coordinator instance might receive different
	    // notifications from the other instances
	    // => differentiate the case
	    if (res.getType().equals(TYPE_uAAL_UI_MAIN_MENU)) {
		Resource user = (Resource) res
			.getProperty(Resource.PROP_uAAL_INVOLVED_HUMAN_USER);
		AbsLocation loginLocation = (AbsLocation) res
			.getProperty(UIResponse.PROP_SUBMISSION_LOCATION);
		dialogManager.getMainMenu(user, loginLocation);
		// suspend a dialog
	    } else if (res.getType().equals(TYPE_uAAL_SUSPEND_DIALOG)) {
		suspendDialog((String) res.getProperty(PROP_uAAL_DIALOG_ID));
	    } else if (res.getType().equals(TYPE_uAAL_UI_BUS_SUBSCRIPTION)) {
		// new UIHandler has registered/unregistered on another instance
		// so here it has to be processed
		String handler = (String) res
			.getProperty(PROP_uAAL_UI_HANDLER_ID);
		UIHandlerProfile subscription = (UIHandlerProfile) res
			.getProperty(PROP_uAAL_UI_SUBSCRIPTION);
		Boolean removes = (Boolean) res
			.getProperty(PROP_uAAL_UI_REMOVE_SUBSCRIPTION);
		// if subscription is null or the removes flag true remove
		// the subscriptions of the handler or the given one
		if (handler != null) {
		    if (subscription != null) {
			if (removes != null && removes.booleanValue()) {
			    removeMatchingProfile(handler, subscription);
			} else {
			    addRegParams(handler, subscription);
			}
		    } else if (removes != null && removes.booleanValue()) {
			removeRegParams(handler);
			// handle P2P notification
		    }
		}
	    } else if (isParametrizedUIBusNotification) {
		final String handler = (String) res
			.getProperty(PROP_uAAL_UI_HANDLER_ID);
		final String dialogID = (String) res
			.getProperty(PROP_uAAL_DIALOG_ID);
		Resource data = (Resource) res
			.getProperty(PROP_uAAL_UI_UPDATED_DATA);
		// Without a valid dialogID we can't do anything
		if (dialogID != null) {
		    if (handler == null) {
			if (data == null) {
			    // here, we are in the case where the original
			    // publisher of a dialog has requested to abort
			    // the dialog
			    notifyHandler_abortDialog(dialogID, pendingRequests
				    .remove(dialogID));
			} else {
			    // if we have data but no handler we resume to
			    // the given dialog
			    resumeDialog(dialogID, data);
			}
		    } else if (data == null) {
			// do it in a new thread to make sure that no
			// deadlock will happen
			new Thread(
				"UI Bus Strategy - Handling dialog finished from remote") {
			    @Override
			    public void run() {
				synchronized (globalSubscriptions) {
				    if (handler.equals(runningDialogs
					    .get(dialogID))) {
					runningDialogs.remove(dialogID);
					dialogManager.dialogFinished(dialogID);
				    }
				}
			    }
			}.start();
		    }
		}
	    }

	} else if (isParametrizedUIBusNotification) {
	    // This is in case we are not the coordinator & we have a
	    // parametrized notification other than user input that was in
	    // the beginning
	    String handlerID = (String) res
		    .getProperty(PROP_uAAL_UI_HANDLER_ID);
	    String dialogID = (String) res.getProperty(PROP_uAAL_DIALOG_ID);
	    if (dialogID == null || handlerID == null) {
		LogUtils
			.logError(
				busModule,
				UIStrategy.class,
				"handleP2PEvent",
				new Object[] { "While handling incoming message (p2p) either UI Handler was null or dialog id was null!" },
				null);
		return;
	    }
	    // here, we are in the case where the coordinator asks for
	    // dialog abort => if the handler and 7 or the original
	    // publisher are on this node, then perform accordingly
	    UICaller publisher = pendingRequests.get(dialogID);
	    if (publisher != null) {
		publisher.dialogAborted(dialogID);
	    }
	    Object o = getBusMember(handlerID);
	    if (o instanceof UIHandler) {
		((UIHandler) o).cutDialog(dialogID);
	    }
	}
    }

    /**
     * If it is of type event this can either be a notification or an
     * {@link UIRequest}
     * 
     * EVENT type is for resending {@link UIRequest}s and/or adaptation
     * parameter changes pass notifications to the according methods
     */
    private void handleNotificationOrUIRequest(Resource res) {
	if (isUIBusNotification(res)) {
	    String handlerID = (String) res
		    .getProperty(PROP_uAAL_UI_HANDLER_ID);
	    UIRequest uiRequest = (UIRequest) res
		    .getProperty(PROP_uAAL_UI_CALL);
	    Boolean isNewRequest = (Boolean) res
		    .getProperty(PROP_uAAL_UI_IS_NEW_REQUEST);
	    if (handlerID == null || uiRequest == null || isNewRequest == null) {
		LogUtils
			.logError(
				busModule,
				UIStrategy.class,
				"handleNotificationOrUIRequest",
				new Object[] { "While handling incoming message (event) at least one of the following was true: 1) Handler was null, 2) UIRequest was null, 3) it was not new request!" },
				null);
		return;
	    } else if (isNewRequest) {
		uiRequest.setProperty(PROP_uAAL_UI_CALL, BusMessage
			.trySerializationAsContent(uiRequest));
		notifyHandler_handle(handlerID, uiRequest);
		uiRequest.changeProperty(PROP_uAAL_UI_CALL, null);
	    } else {
		notifyHandler_apChanged(handlerID, uiRequest, res.getProperty(
			PROP_uAAL_CHANGED_PROPERTY).toString());
		// handle UI requests
	    }
	}
    }

    /**
     * 
     * @return True if the instance of the strategy is the coordinator, false
     *         otherwise
     */
    private boolean isCoordinator() {
	// the instance to which the dialog manager connects is the coordinator
	// => check if I am the "lucky" instance and if not, check if I know the
	// coordinator
	if (dialogManager == null && theCoordinator == null) {
	    // let's see if any other instance claims to be the coordinator
	    Resource r = new Resource();
	    r.addType(TYPE_uAAL_UI_BUS_COORDINATOR, true);
	    ((UIBusImpl) bus).assessContentSerialization(r);
	    BusMessage m = new BusMessage(MessageType.p2p_request, r, bus);
	    synchronized (this) {
		while (dialogManager == null && theCoordinator == null) {
		    send(m);
		    try {
			// now wait until either the reply comes and notifies me
			// or the dialog manager connects which will also lead
			// to awakening this thread
			wait();
		    } catch (Exception e) {
		    }
		}
	    }
	}
	// either I got into the above 'if' or not, the answer if I am the
	// coordinator depends on if I have the dialog manager
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
	    if (publisher != null && publisher != dialogManager) {
		publisher.dialogAborted(dialogID);
	    }
	    String handlerID = runningDialogs.remove(dialogID);
	    if (handlerID == null) {
		LogUtils
			.logError(
				busModule,
				UIStrategy.class,
				"notifyHandler_abortDialog",
				new Object[] { "UI dialog removed from running dialogs. UI Handler of that dialog could not be determined. Inconsistent data!!" },
				null);
		return;
	    }
	    // check if the handler is a local member of this instance of the
	    // bus
	    Object o = getBusMember(handlerID);
	    if (o instanceof UIHandler) {
		((UIHandler) o).cutDialog(dialogID);
	    } else {
		Resource pr = new Resource();
		pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
		pr.setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
		pr.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
		sendMessageToRemoteBusMember(handlerID, MessageType.p2p_event,
			pr);
	    }
	} else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    sendMessageToCoordinator(MessageType.p2p_event, pr);
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
	// if handler is at local node perform the adaption
	Object o = getBusMember(handlerID);
	if (o instanceof UIHandler) {
	    LogUtils.logInfo(busModule, UIStrategy.class,
		    "notifyHandler_apChanged", new Object[] {
			    "Notified handler ", handlerID, ":\n", content },
		    null);
	    if (changedProp != null) {
		((UIHandler) o).adaptationParametersChanged(request
			.getDialogID(), changedProp, request
			.getProperty(changedProp));
	    }
	} else if (isCoordinator()) {
	    // if handler is not the local instance but it is the coordinator
	    // forward the notification to the appropriate node
	    request.changeProperty(PROP_uAAL_UI_CALL, null);
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    pr.setProperty(PROP_uAAL_UI_CALL, request);
	    pr.setProperty(PROP_uAAL_CHANGED_PROPERTY, changedProp);
	    pr.setProperty(PROP_uAAL_UI_IS_NEW_REQUEST, Boolean.FALSE);
	    sendMessageToRemoteBusMember(handlerID, MessageType.event, pr);
	} // else should not happen
	LogUtils
		.logWarn(
			busModule,
			UIStrategy.class,
			"notifyHandler_apChanged",
			new Object[] { "Unpredicted situation happened while handling a dialogChanged-Notification!" },
			null);
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
	// if the handler is the local node cut the dialog
	Object o = getBusMember(handlerID);
	if (o instanceof UIHandler) {
	    return ((UIHandler) o).cutDialog(dialogID);
	    // if the handler is not the local node but it is the coordinator
	    // start the cut-dialog and wait for the user-input
	} else if (isCoordinator()) {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    pr.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    ((UIBusImpl) bus).assessContentSerialization(pr);
	    BusMessage message = new BusMessage(MessageType.request, pr, bus);
	    message.setReceiver(AbstractBus
		    .getPeerFromBusResourceURI(handlerID));
	    synchronized (waitingForCut) {
		waitingForCut.put(dialogID, handlerID);
		send(message);
		while (!(waitingForCut.get(handlerID) instanceof UIResponse)) {
		    try {
			wait();
		    } catch (Exception e) {
			LogUtils
				.logError(
					busModule,
					UIStrategy.class,
					"notifyHandler_cutDialog",
					new Object[] { "Error while waiting for user input" },
					e);
		    }
		}
		UIResponse ie = (UIResponse) waitingForCut.remove(handlerID);
		return (Resource) ie.getProperty(UIResponse.PROP_DIALOG_DATA);
	    }
	} // else
	LogUtils
		.logWarn(
			busModule,
			UIStrategy.class,
			"notifyHandler_cutDialog",
			new Object[] { "Unpredicted situation happened while handling a cutDialog-Notification!" },
			null);
	return null;
    }

    private void sendMessageToRemoteBusMember(String memberID,
	    MessageType type, Resource content) {
	((UIBusImpl) bus).assessContentSerialization(content);
	BusMessage m = new BusMessage(type, content, bus);
	m.setReceiver(AbstractBus.getPeerFromBusResourceURI(memberID));
	send(m);
    }

    private void sendMessageToCoordinator(MessageType type, Resource content) {
	((UIBusImpl) bus).assessContentSerialization(content);
	BusMessage m = new BusMessage(type, content, bus);
	forwardMessageToCoordinator(m);
    }

    /**
     * 
     * Handle uiRequest-notifications. Look for {@link UIHandler} for relay the
     * {@link UIRequest}.
     * 
     * @param handlerID
     *            ID of the handler of the request
     * @param request
     *            Request to handle
     */
    private void notifyHandler_handle(String handlerID, UIRequest request) {
	String content = (String) request.getProperty(PROP_uAAL_UI_CALL);
	// if the handler is the local node handle the output
	Object o = getBusMember(handlerID);
	if (o instanceof UIHandler) {
	    // I have the handler => i can handle it
	    LogUtils.logInfo(busModule, UIStrategy.class,
		    "notifyHandler_handle", new Object[] {
			    "Notified handler id: ", handlerID }, null);
	    ((UIHandler) o).handleUICall(request);

	} else if (isCoordinator()) {
	    // I am the coordinator, but the handler is not here
	    request.changeProperty(PROP_uAAL_UI_CALL, null);
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_NOTIFICATION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    pr.setProperty(PROP_uAAL_UI_CALL, request);
	    pr.setProperty(PROP_uAAL_UI_IS_NEW_REQUEST, Boolean.TRUE);
	    sendMessageToRemoteBusMember(handlerID, MessageType.event, pr);
	} else
	    LogUtils
		    .logWarn(
			    busModule,
			    UIStrategy.class,
			    "notifyHandler_handle",
			    new Object[] {
				    "Unpredicted situation happened while handling a uiRequest-notification!",
				    o.getClass().getName() }, null);
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
    void removeMatchingProfile(String subscriberID,
	    UIHandlerProfile oldSubscription) {
	if (subscriberID == null || oldSubscription == null) {
	    return;
	}

	if (isCoordinator()) {

	    List<Subscription> remove = new ArrayList<Subscription>();
	    synchronized (globalSubscriptions) {
		for (Subscription s : globalSubscriptions) {
		    if (s.subscriberID.equals(subscriberID)
			    && oldSubscription
				    .matches(s.uiHandlerProfileFilter)) {
			remove.add(s);
		    }
		}
		globalSubscriptions.removeAll(remove);
	    }
	} else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_SUBSCRIPTION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, subscriberID);
	    pr.setProperty(PROP_uAAL_UI_SUBSCRIPTION, oldSubscription);
	    pr.setProperty(PROP_uAAL_UI_REMOVE_SUBSCRIPTION, Boolean.TRUE);
	    sendMessageToCoordinator(MessageType.p2p_event, pr);
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
	if (subscriberID == null) {
	    return;
	}

	if (isCoordinator()) {
	    synchronized (globalSubscriptions) {
		for (Subscription s : globalSubscriptions) {
		    if (s.subscriberID.equals(subscriberID)) {
			globalSubscriptions.remove(s);
		    }
		}
	    }
	} else {
	    Resource pr = new Resource();
	    pr.addType(TYPE_uAAL_UI_BUS_SUBSCRIPTION, true);
	    pr.setProperty(PROP_uAAL_UI_HANDLER_ID, subscriberID);
	    pr.setProperty(PROP_uAAL_UI_REMOVE_SUBSCRIPTION, Boolean.TRUE);
	    sendMessageToCoordinator(MessageType.p2p_event, pr);
	}
    }

    /**
     * Called only when an application is finished with a subdialog and wants to
     * resume the original dialog passing the changed dialog data. Also called
     * by the {@link IDialogManager} when the user has instructed to resume a
     * specific dialog; Or when the {@link IDialogManager} wants to resume a
     * previously suspended dialog because of higher priority dialog
     * interrupted.
     * 
     * @param dialogID
     *            ID of the dialog we want to resume
     * @param dialogData
     *            Original data of the dialog we are going to resume
     */
    void resumeDialog(String dialogID, Resource dialogData) {
	if (isCoordinator()) {
	    UIRequest uiRequest = dialogManager.getSuspendedDialog(dialogID);
	    if (uiRequest != null) {
		uiRequest.setCollectedInput(dialogData);
		adaptationParametersChanged(dialogManager, uiRequest, null);
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
	    sendMessageToCoordinator(MessageType.p2p_event, res);
	}
    }

    /**
     * 
     * Set the {@link IDialogManager} of the bus. There must be only one
     * instance of this in the whole remote system.
     * 
     * @param dm
     *            Instance of the {@link IDialogManager}
     */
    void setDialogManager(IDialogManager dm) {
	if (dm == null || dialogManager != null || theCoordinator != null) {
	    LogUtils
		    .logError(
			    busModule,
			    UIStrategy.class,
			    "setDialogManager",
			    new Object[] { "At least one of the following happened: newly given ui.dm is null, ui.dm already exists, coordinator already exists" },
			    null);
	    return;
	}

	globalSubscriptions = new Vector<Subscription>();
	runningDialogs = new Hashtable<String, String>();
	waitingForCut = new Hashtable<String, Object>(2);
	lastUsedHandler = new Hashtable<String, String>();

	Resource res = new Resource(bus.getURI());
	res.addType(TYPE_uAAL_UI_BUS_COORDINATOR, true);

	synchronized (this) {
	    dialogManager = dm;
	    ((UIBusImpl) bus).assessContentSerialization(res);
	    send(new BusMessage(MessageType.p2p_event, res, bus));
	    notifyAll();
	}
    }

    /**
     * Called only when a {@link UIHandler} has called dialogFinished with an
     * instance of {@link org.universAAL.middleware.ui.rdf.SubdialogTrigger} so
     * that we must only notify to suspend this dialog until the original
     * publisher calls 'resume'.
     * 
     * @param dialogID
     *            ID of the dialog to suspend
     */
    void suspendDialog(String dialogID) {
	if (dialogID == null) {
	    return;
	}

	if (isCoordinator()) {
	    dialogManager.suspendDialog(dialogID);
	} else {
	    Resource res = new Resource();
	    res.addType(TYPE_uAAL_SUSPEND_DIALOG, true);
	    res.setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    sendMessageToCoordinator(MessageType.p2p_event, res);
	}
    }

    /**
     * 
     * @param user
     *            {@link User} that logged (request main menu)
     * @param loginLocation
     *            location of the handler from which the user logged in
     */
    void userLoggedIn(Resource user, AbsLocation loginLocation) {
	if (isCoordinator()) {
	    dialogManager.getMainMenu(user, loginLocation);
	} else {
	    Resource res = new Resource();
	    res.addType(TYPE_uAAL_UI_MAIN_MENU, true);
	    res.setProperty(Resource.PROP_uAAL_INVOLVED_HUMAN_USER, user);
	    if (loginLocation != null) {
		res.setProperty(UIResponse.PROP_SUBMISSION_LOCATION,
			loginLocation);
	    }
	    sendMessageToCoordinator(MessageType.p2p_event, res);
	}
    }

}
