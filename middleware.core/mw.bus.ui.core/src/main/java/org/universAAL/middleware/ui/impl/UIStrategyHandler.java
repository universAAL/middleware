/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.ui.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.ui.IDialogManager;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.impl.generic.CallMessage;
import org.universAAL.middleware.ui.impl.generic.EventMessage;
import org.universAAL.middleware.ui.rdf.Form;

/**
 * This part of the UIStrategy Stack deals only with communications between the {@link UIHandler}s
 * and the {@link IDialogManager}.
 *  <center> <img style="background-color:white;" src="doc-files/UIStrategyHandler.png"
 * alt="UIStrategy messages" width="70%"/> </center>
 * <br>
 * the messages exchaged are:
 * <ol>
 * <li> userLogOn: notifies the {@link IDialogManager} when a user has logOn at a handler.
 * <li> NotifyHandler: the {@link IDialogManager} is sending a new {@link UIRequest} or updating an
 * existing one.
 * <li> FinishDialog: the {@link UIHandler} notifies the {@link IDialogManager} a user has finishied a dialog, the {@link UIResponse}
 * goes in this message.
 * <li> CutCall: this is a synchronous call, the bus is telling the {@link UIHandler} to derenderize a certain dialog, and it is expecting
 * (and waiting) for the {@link Form#PROP_DIALOG_DATA_ROOT dataRoot} of the {@link Form}.
 * </ol>
 * @author amedrano
 * 
 */
public abstract class UIStrategyHandler extends UIStrategyCoordinatorMng {    

    /**
     * Last used handler weight is same as the dialog privacy's one.
     */
    public static final int LAST_USED_HANDLER_MATCH_LEVEL_ADDITION = UIHandlerProfile.MATCH_DIALOG_PRIVACY;

    private class UserLogOnMessage extends Resource implements IUIStrategyMessageSharedProps,EventMessage<UIStrategyCaller> {

	public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
		+ "LogIn";

	public static final String PROP_LOCATION = Resource.uAAL_VOCABULARY_NAMESPACE
		+ "logOnLocation";
	/**
	 * 
	 */
	public UserLogOnMessage() {
	    super();
	}

	public UserLogOnMessage(String handlerID, Resource user, AbsLocation location){
	    addType(MY_URI, true);
	    setProperty(PROP_uAAL_INVOLVED_HUMAN_USER, user);
	    setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    if (location != null) {
		setProperty(PROP_LOCATION, location);
	    }
	}


	/** {@ inheritDoc}	 */
	public void onReceived(UIStrategyCaller strategy, BusMessage m, String senderID) {
	    String handerID = (String) getProperty(PROP_uAAL_UI_HANDLER_ID);
	    Resource usr = (Resource) getProperty(PROP_uAAL_INVOLVED_HUMAN_USER);
	    AbsLocation loc = (AbsLocation) getProperty(PROP_LOCATION);
	    userLoggedIn(handerID, usr, loc);
	}
    }
    
    private class FinishDialogMessage extends Resource implements
    EventMessage<UIStrategyCaller>, IUIStrategyMessageSharedProps {
	/**
	 * Type for finishing dialog.
	 */
	public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
		+ "FinishDialog";
	/**
	 * 
	 */
	public FinishDialogMessage() {
	    super();
	}

	public FinishDialogMessage(String handlerID, UIResponse input) {
	    addType(MY_URI, true);
	    setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    setProperty(PROP_uAAL_UI_USER_INPUT, input);
	}
	/** {@ inheritDoc}	 */
	public void onReceived(UIStrategyCaller strategy, BusMessage m,
		String senderID) {
	    dialogFinished(
		    (String) getProperty(PROP_uAAL_UI_HANDLER_ID),
		    (UIResponse) getProperty(PROP_uAAL_UI_USER_INPUT)
		    );
	}
    }
    
    private class NotifyHandlerMessage extends Resource implements
    IUIStrategyMessageSharedProps,EventMessage<UIStrategyCaller> {

	public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
		+ "HandlerNotify";

	/**
	 * 
	 */
	public NotifyHandlerMessage() {
	    super();
	}

	public NotifyHandlerMessage(String handlerID, UIRequest req){
	    addType(MY_URI, true);
	    setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	    setProperty(PROP_uAAL_UI_CALL, req);
	}

	public NotifyHandlerMessage(String handlerID, UIRequest req, String propChange){
	    this(handlerID,req);
	    setProperty(PROP_uAAL_CHANGED_PROPERTY, propChange);
	}

	/** {@ inheritDoc}	 */
	public void onReceived(UIStrategyCaller strategy, BusMessage m, String senderID) {
	    String handlerID = (String) getProperty(PROP_uAAL_UI_HANDLER_ID);
	    UIRequest uiReq = (UIRequest) getProperty(PROP_uAAL_UI_CALL);
	    if (!hasProperty(PROP_uAAL_CHANGED_PROPERTY)) {
		notifyHandler_handle(handlerID, uiReq);
	    } else {
		notifyHandler_apChanged(handlerID, uiReq,
			(String) getProperty(PROP_uAAL_CHANGED_PROPERTY));
	    }
	}
    }
    
    private class CutCallMessage extends CallMessage<UIStrategyHandler> implements IUIStrategyMessageSharedProps {

	/**
	 * Type for Cutting.
	 */
	public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
		+ "Cut";
	
	/**
	 * Type only for the case there is a {@link Resource} withou properties, that will not be serialized.
	 */
	private static final String TYPE_DUMMY_TYPE = Resource.uAAL_VOCABULARY_NAMESPACE + "DummyType";

	public CutCallMessage(){
	    super();
	}
	/**
	 * 
	 */
	public CutCallMessage(String dialogID, String handlerID){
	    addType(MY_URI, true);
	    setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    setProperty(PROP_uAAL_UI_HANDLER_ID, handlerID);
	}

	/** {@ inheritDoc}	 */
	protected
	void onRequest(UIStrategyHandler strategy, BusMessage m, String senderID) {
	    Resource data = strategy.cutDialog(
		    (String) getProperty(PROP_uAAL_UI_HANDLER_ID),
		    (String) getProperty(PROP_uAAL_DIALOG_ID));
	    if (data == null
		    || data.numberOfProperties() == 0){
		data = new Resource(data==null? null:data.getURI());
		data.addType(TYPE_DUMMY_TYPE, true);
	    }
	    strategy.sendSynchronousResponse(m, data);
	}

	/** {@ inheritDoc}	 */
	protected
	void onResponse(UIStrategyHandler strategy, BusMessage m, String senderID) {
	    //NOTHING a synchronous call always.
	}
    }
    
    private class OntFactory implements ResourceFactory{

	/** {@ inheritDoc}	 */
	public Resource createInstance(String classURI, String instanceURI,
		int factoryIndex) {
	    switch (factoryIndex) {
	    case 0:
		return new UserLogOnMessage();
	    case 1:
		return new FinishDialogMessage();
	    case 2:
		return new NotifyHandlerMessage();
	    case 3:
		return new CutCallMessage();
	    default:
		break;
	    }
	    return null;
	}
	
    }
    
    private class POntology extends Ontology{

	private ResourceFactory fatory = new OntFactory();
	/**
	 * @param ontURI
	 */
	public POntology(String ontURI) {
	    super(ontURI);
	}
	
	/** {@ inheritDoc}	 */
	public void create() {
	    createNewRDFClassInfo(UserLogOnMessage.MY_URI, fatory, 0);
	    createNewRDFClassInfo(FinishDialogMessage.MY_URI, fatory, 1);
	    createNewRDFClassInfo(NotifyHandlerMessage.MY_URI, fatory, 2);
	    createNewRDFClassInfo(CutCallMessage.MY_URI, fatory, 3);
	}
	
    }
    
    /**
     * To keep track of the last used handler per user.
     */
    protected Map<String, String> lastUsedHandler;

    /**
     * The Dialogs Manager keeps track of which dialog is where
     * and what dialog is handling each handler.
     */
    protected RunningDialogsManager runningDialogs;

    
    private Ontology ont;
    /**
     * @param commModule
     * @param name
     */
    public UIStrategyHandler(CommunicationModule commModule, String name) {
	super(commModule, name);
    }

    /** {@ inheritDoc}	 */
    public synchronized void start() {
	super.start();
	ont = new POntology(Resource.uAAL_NAMESPACE_PREFIX + "UIStrategyHandlerMessageOntology");
	OntologyManagement.getInstance().register(busModule, ont);
    }

    /**
     * @param commModule
     */
    public UIStrategyHandler(CommunicationModule commModule) {
	super(commModule);
    }

    /** {@ inheritDoc} */
    boolean setDialogManager(IDialogManager dm) {
	if (dm != null) {
	    lastUsedHandler = new Hashtable<String, String>();
	    runningDialogs = new RunningDialogsManager();
	}else{
	    lastUsedHandler = null;
	    runningDialogs = null;
	}
	return super.setDialogManager(dm);
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
	    //operation only done in Coordinator
	    String selectedHandler = null;
	    String currentHandler = runningDialogs.getHandler(uiRequest
		    .getDialogID());
	    if (changedProp == null) {
		// this is a new dialog published to the bus
		// Or a dialog is being resumed by the DM.
		// if (pendingRequests.get(uiRequest.getDialogID()) == null) {
		// // if req is not in the pendingRequests (in dm queue)
		// // add it there
		// pendingRequests.put(uiRequest.getDialogID(), (UICaller) dm);
		// LogUtils.logDebug(
		// busModule,
		// getClass(),
		// "adaptationParametersChanged",
		// new Object[] {
		// "ui.dm has published new dialog on the ui bus !" },
		// null);
		// }
		if (currentHandler != null) {
		    LogUtils.logWarn(
			    busModule,
			    getClass(),
			    "adaptationParametersChanged",
			    new Object[] {
				    "strange situation: duplication dialog ID?\n",
				    uiRequest }, null);
		}
	    } else if (currentHandler == null) {
		LogUtils.logError(
			busModule,
			getClass(),
			"adaptationParametersChanged",
			new Object[] { "Current UI Handler could not be determined from running dialogs." +
					" Inconsistent data between ui.dm data and UIStrategy data!" },
			null);
	    }

	    selectedHandler = selectHandler(uiRequest);
	    if (selectedHandler == null) {
		// No UI Handler can be selected so put dialog to suspended
		// dialogues queue. DM is repeatedly trying to
		// show something and if it there is nothing else to show it
		// will check pending dialogues also (at that time new
		// UIHandler can be added or existing one can change reg
		// parameters or adaptation parameters can change)
		dm.suspendDialog(uiRequest.getDialogID());
		LogUtils.logInfo(
			busModule,
			getClass(),
			"adaptationParametersChanged",
			new Object[] {
				"No UI Handler could be selected so dialog is suspended for now.",
				uiRequest }, null);
		return;
	    }

	    if (selectedHandler.equals(currentHandler)) {
		// notify UIHandler that currently "has" this
		// request. Notify only if there is prop that
		// changed.
		if (changedProp != null) {
		    notifyHandler_apChanged(currentHandler, uiRequest,
			    changedProp);
		    return;
		}
	    }

	    if (currentHandler != null) {
		    /*
		     *  the dialog has to move from the currentHandler to the
		     *  selectedHandler
		     */
		    // Retrieve data from currentHandler
		    Resource collectedData = cutDialog(currentHandler, uiRequest.getDialogID());
		    if (collectedData != null) {
			//update the data
			uiRequest.setCollectedInput(collectedData);
			//XXX send data to DM?
		    }
		    // remove the dialog ID from asigned handler.
		    runningDialogs.removeDialogId(uiRequest.getDialogID());

	    }
	    runningDialogs.add(selectedHandler, uiRequest.getDialogID());
	    notifyHandler_handle(selectedHandler, uiRequest);
	}
    }
    
    protected Resource cutDialog(String handlerID, String dialogID){
	if (handlerID == null ||dialogID == null)
	    return null;
	BusMember bm = getBusMember(handlerID);
	    if (bm instanceof UIHandler) {
		// I have the handler => i can handle it
		if (bm instanceof UIHandler) {
		    Resource data = ((UIHandler) bm).cutDialog(dialogID);
		    return data;
		}
	    } 
	    else if (AbstractBus.getPeerFromBusResourceURI(handlerID).equals(bus.getPeerCard())){
		// handler should be in this instance but it is not responding to previous if
		return null;
	    }
	    else {
		// send request to remote peer
		try {
		    Resource data = (Resource) placeSynchronousRequest(handlerID, new CutCallMessage(dialogID, handlerID));
		    if (data != null && data.getType().equals(CutCallMessage.TYPE_DUMMY_TYPE)){
			data = new Resource(data.getURI());
		    }
		    return data;
		} catch (InterruptedException e) {
			LogUtils.logError(busModule, getClass(),
				"CutDialog",
				"Cut Call to move dialog was aborted.");
		}
	    }
	    return null;
    }
        
    void dialogFinished(final String handlerID, final UIResponse input) {
        if (input == null) {
            LogUtils.logWarn(
        	    busModule,
        	    getClass(),
        	    "dialogFinished",
        	    new Object[] { "Dialog is finished by the user but UI Handler sent empty UI Response!" },
        	    null);
            return;
        }
        // first handle the bus internal handling of this request
        if (iAmCoordinator()) {
            // do it in a new thread to make sure that no deadlock will happen
            new Thread(new DialogFinishedTask(
        	    handlerID, input),
        	    "UI Bus Strategy - Handling dialog finished").start();
        } else {
            //send message
            sendEventToRemoteBusMember(getCoordinator(), new FinishDialogMessage(handlerID, input));
        }
    }

    /**
     * 
     * @param user
     *            {@link User} that logged (request main menu)
     * @param loginLocation
     *            location of the handler from which the user logged in
     */
    void userLoggedIn(String handlerID, Resource user, AbsLocation loginLocation) {
	if (iAmCoordinator()) {
	    dialogManager.userLogIn(user, loginLocation);
	    lastUsedHandler.put(user.getURI(), handlerID);
	} else {
	    sendEventToRemoteBusMember(getCoordinator(), new UserLogOnMessage(handlerID,user, loginLocation));
	}
    }
    
    /**
     * @return
     */
    private String selectHandler(UIRequest uiRequest) {
	String selectedHandler = null;
	int maxMatchDegree = UIHandlerProfile.MATCH_LEVEL_FAILED;
	// iterate through all UIHandlers
	for (Iterator<Matchable> it = registryIterator(); it.hasNext();) {
	    UIHandlerProfile prof = (UIHandlerProfile) it.next();
	    String profId = getRegistryID(prof);
	    int tempMatchingDegree = prof.getMatchingDegree(uiRequest);
	    LogUtils.logDebug(
		    busModule,
		    getClass(),
		    "selectHandler",
		    new Object[] { "\n START+++++++++++++++++++++++++++++++++++++++++++++++\n Handler with subscription id: "
			    + profId
			    + "\nhas matching degree: "
			    + tempMatchingDegree
			    + " \n in binary is: " + Integer.toBinaryString(tempMatchingDegree)
			    + " \n [Usr, Mod, AltMod, Loc, Impair, Priv, Lang, Form]"
			    + "\n 2+++++++++++++++++++++++++++++++++++++++++++++++"
			    + "\n UIHandler profile:\n "
			    + prof.toStringRecursive()
			    + "\n 3+++++++++++++++++++++++++++++++++++++++++++++++"
			    + "\n uiRequest that is getting matched.\n Addressed user: "
			    + uiRequest.getAddressedUser().getURI()
			    + "\n Modality: "
			    + uiRequest
				    .getProperty(UIRequest.PROP_PRESENTATION_MODALITY)
			    + "\n Presentation location: "
			    + uiRequest.getPresentationLocation()
			    + "\n STOP+++++++++++++++++++++++++++++++++++++++++++++++\n " },
		    null);

	    if (tempMatchingDegree > UIHandlerProfile.MATCH_LEVEL_FAILED) {

		if (profId.equals(lastUsedHandler.get(uiRequest
			.getAddressedUser().getURI()))) {
		    // if currently observed handler also the one last
		    // used by the user then increase his matching
		    // degree a bit
		    tempMatchingDegree += LAST_USED_HANDLER_MATCH_LEVEL_ADDITION;
		    LogUtils.logDebug(
			    busModule,
			    getClass(),
			    "selectHandler", "last profile has been bonified for being last handler used by user.");
		}
		if (tempMatchingDegree > maxMatchDegree) {
		    maxMatchDegree = tempMatchingDegree;
		    selectedHandler = profId;
		}
	    }
	}
	LogUtils.logDebug(
		busModule,
		getClass(),
		"selectHandler",
		new Object[] { "Handler with id: "
			+ selectedHandler
			+ ", and matching degree: "
			+ maxMatchDegree
			+ " was selected as best. Note: last used handler additional weight= "
			+ LAST_USED_HANDLER_MATCH_LEVEL_ADDITION }, null);
	return selectedHandler;
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
	// if handler is at local node perform the adaption
	Object o = getBusMember(handlerID);
	if (o instanceof UIHandler) {
	    LogUtils.logInfo(busModule, getClass(),
		    "notifyHandler_apChanged", new Object[] {
			    "Notified handler ", handlerID, ":\n", request },
		    null);
	    if (changedProp != null) {
		((UIHandler) o).adaptationParametersChanged(
			request.getDialogID(), changedProp,
			request.getProperty(changedProp));
	    }
	} else if (iAmCoordinator()) {
	    // if handler is not the local instance but it is the coordinator
	    // forward the notification to the appropriate node
	    sendEventToRemoteBusMember(handlerID, new NotifyHandlerMessage(handlerID, request, changedProp));
	} 
	else
	// else should not happen
	LogUtils.logWarn(
		busModule,
		getClass(),
		"notifyHandler_apChanged",
		new Object[] { "Unpredicted situation happened while handling a dialogChanged-Notification!" },
		null);
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
	if (handlerID == null || request == null)
	    return;
	// if the handler is the local node handle the output
	Object o = getBusMember(handlerID);
	if (o instanceof UIHandler) {
	    // I have the handler => i can handle it
	    LogUtils.logInfo(busModule, getClass(),
		    "notifyHandler_handle", new Object[] {
			    "Notified handler id: ", handlerID }, null);
	    ((UIHandler) o).handleUICall(request);

	} else if (iAmCoordinator()) {
	    // I am the coordinator, but the handler is not here
	    sendEventToRemoteBusMember(handlerID, 
		    new NotifyHandlerMessage(handlerID, request));
	} else
	    LogUtils.logWarn(
		    busModule,
		    getClass(),
		    "notifyHandler_handle",
		    new Object[] {
			    "Unpredicted situation happened while handling a uiRequest-notification!",
			    o.getClass().getName() }, null);
    }


    private class DialogFinishedTask implements Runnable {
	    
        private UIResponse response;
        private String handlerId;
    
        /**
         * @param user
         * @param handlerId
         * @param dialogID
         */
        public DialogFinishedTask( String handlerId,
        	UIResponse resp) {
            super();
            this.handlerId = handlerId;
            this.response = resp;
        }
    
        /** {@ inheritDoc} */
        public void run() {
            synchronized (runningDialogs) {
        	// remember last used handler for the user (important when
        	// selecting the
        	// Handler)
        	lastUsedHandler.put(response.getUser().getURI(), handlerId);
        	
        	if (handlerId.equals(runningDialogs.getHandler(response.getDialogID()))) {
        	    runningDialogs.removeDialogId(response.getDialogID());
        	    if (response.isForDialogManagerCall()){
        		((UICaller)dialogManager).handleUIResponse(response);
        	    }
        	    else {
        		notifyCallerDialogSubmitted(response);
        		if (response.isSubdialogCall()){
        		    dialogManager.suspendDialog(response.getDialogID());
        		}
        		else {
        		    dialogManager.dialogFinished(response.getDialogID());
        		}
        	    }
        	} 
        	else if (response.isForDialogManagerCall()){
        	    ((UICaller)dialogManager).handleUIResponse(response);
        	}
            }
        }
    }
    
    /**
     * check sender and if local send it if not send message.
     * @param response
     */
    protected abstract void notifyCallerDialogSubmitted(UIResponse response);

    /** {@ inheritDoc}	 */
    public void close() {
	super.close();
	abortAll();
	OntologyManagement.getInstance().unregister(busModule, ont);
    }
    
    /** {@ inheritDoc}	 */
    public void peerLost(PeerCard peer) {
	super.peerLost(peer);
	if (iAmCoordinator()){
	    //remove all Handler profiles form that peer
	    List<String> tbr = new ArrayList<String>();
	    Iterator<String> it = registryIdIterator();
	    while (it.hasNext()) {
		String handlerId = (String) it.next();
		if (AbstractBus.getPeerFromBusResourceURI(handlerId).equals(peer)){
		    tbr.add(handlerId);
		}
	    }
	    for (String hID : tbr) {
		removeAllRegistries(hID);
	    }
	    //reschedule running dialogs of the falling peer
	    Set<String> reschedule = new HashSet<String>();
	    for (String hID : tbr) {
		reschedule.addAll(runningDialogs.getDialogs(hID));
		runningDialogs.removeHandlerId(hID);
	    }
	    for (String dID : reschedule) {
		adaptationParametersChanged(dialogManager, dialogManager.getSuspendedDialog(dID), null);
	    }
	}
    }


    /**
     * This task is launched when the coordinator peer is lost,
     * which means all registrations are now invalid. This task
     * schedules the re-registration of all implemented {@link UIHandlerProfile}s
     * by all the {@link UIHandler}s registered in the local node.
     * @author amedrano
     *
     */
    private class ResendRegisstrationTask implements Runnable{

	/** {@ inheritDoc}	 */
	public void run() {
	    String[] id = bus.getBusMembersByID();
	    for (int i = 0; i < id.length; i++) {
		BusMember bm = getBusMember(id[i]);
		if (bm instanceof UIHandler){
		    UIHandler h = (UIHandler) bm;
		    List<UIHandlerProfile> profs = h.getRealizedHandlerProfiles();
		    for (UIHandlerProfile hp : profs) {
			addRegistration(id[i], hp);
			//the first one will be locked until there is a coordinator.
		    }
		}
	    }
	}
    }
    
    /**
     * on Coordination lost: reschedule reRegistration.
     */
    protected void lostCoordinator() { 
	//cut all displaying handlers
	BusMember[] bm = bus.getBusMembers();
	for (int i = 0; i < bm.length; i++) {
	    if (bm[i] instanceof UIHandler){
		((UIHandler)bm[i]).communicationChannelBroken();
	    }
	}
	//resend Registration
	new Thread(new ResendRegisstrationTask(), "UIStrategyResendRegistrationsTask").start();
    }
    
    //TODO override newRegistration() and try to reallocate pending (and not running) dialogs
}
