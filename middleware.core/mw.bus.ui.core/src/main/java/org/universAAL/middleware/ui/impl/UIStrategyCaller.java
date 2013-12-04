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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.ui.IDialogManager;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.impl.generic.CallMessage;
import org.universAAL.middleware.ui.impl.generic.EventMessage;
import org.universAAL.middleware.ui.rdf.Form;

/**
 * @author amedrano
 * 
 */
public class UIStrategyCaller extends UIStrategyHandler {
    
    public class UIRequestCall extends CallMessage<UIStrategyCaller> implements IUIStrategyMessageSharedProps{

	private static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE + "UIRequestCall";
	private static final String PROP_uAAL_UI_RESPONSE = Resource.uAAL_VOCABULARY_NAMESPACE + "uiResponse";;

	public UIRequestCall(){
	    super();
	}
	
	public UIRequestCall(UIRequest req){
	    addType(MY_URI, true);
	    setProperty(PROP_uAAL_UI_CALL, req);
	}
	
	public UIRequestCall(UIResponse resp){
	    addType(MY_URI, true);
	    setProperty(PROP_uAAL_UI_RESPONSE, resp);
	}
	/** {@ inheritDoc}	 */
	@Override
	protected void onRequest(UIStrategyCaller strategy, BusMessage m,
		String senderID) {
	    if (iAmCoordinator()) {
		handleUIRequest(m, senderID);
	    } else {
		//Forward to Coord.
		m.setReceiver(getCoordinator());
		send(m);
	    }
	}

	/** {@ inheritDoc}	 */
	@Override
	protected void onResponse(UIStrategyCaller strategy, BusMessage m,
		String senderID) {
	    UIResponse resp = (UIResponse)getProperty(PROP_uAAL_UI_RESPONSE);
	    if (resp != null) {
		notifyCallerDialogSubmitted(resp);
	    } else {
		LogUtils.logError(busModule, getClass(), "onResponse", "Received UIResponse is null!");
	    }
	}

	/**
	 * @return
	 */
	public UIRequest getRequest() {
	    return (UIRequest) getProperty(PROP_uAAL_UI_CALL);
	}
	
    }

    private class ResumeDialogMessage extends Resource implements IUIStrategyMessageSharedProps,EventMessage<UIStrategyCaller> {
	    public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
		    + "ResumeDialog";
	    public ResumeDialogMessage() {
		super();
	    }
	    
	    public ResumeDialogMessage(String dialogID, Resource updatedData){
		addType(MY_URI, true);
		setProperty(PROP_uAAL_DIALOG_ID, dialogID);
		setProperty(PROP_uAAL_UI_UPDATED_DATA, updatedData);
	    }

	    /** {@ inheritDoc}	 */
	    public void onReceived(UIStrategyCaller strategy, BusMessage m, String senderID) {
		resumeDialog(
			(String) getProperty(PROP_uAAL_DIALOG_ID), 
			(Resource) getProperty(PROP_uAAL_UI_UPDATED_DATA));
	    }
	}

    private class SuspendDialogMessage extends Resource implements IUIStrategyMessageSharedProps,EventMessage<UIStrategyCaller> {
	    public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
		    + "SuspendDialog";
	    public SuspendDialogMessage() {
		super();
	    }
	    
	    public SuspendDialogMessage(String dialogID){
		addType(MY_URI, true);
		setProperty(PROP_uAAL_DIALOG_ID, dialogID);
	    }

	    /** {@ inheritDoc}	 */
	    public void onReceived(UIStrategyCaller strategy, BusMessage m, String senderID) {
		strategy.suspendDialog(
			(String) getProperty(PROP_uAAL_DIALOG_ID));
	    }
	}
    
    private class AbortCall extends CallMessage<UIStrategyCaller> implements IUIStrategyMessageSharedProps {

	    /**
	     * Type for aborting.
	     */
	    public static final String MY_URI = Resource.uAAL_VOCABULARY_NAMESPACE
		    + "Abort";
	    
	    /**
	     * Constructor for deserializer.
	     */
	    public AbortCall() {
		super();
	    }

	    public AbortCall(String dialogID, String callerID){
		super();
		addType(MY_URI, true);
		setProperty(PROP_uAAL_DIALOG_ID, dialogID);
		setProperty(PROP_uAAL_UI_CALLER_ID, callerID);
	    }
	    
	    public AbortCall(String dialogID, String callerID, Resource data){
		super();
		addType(MY_URI, true);
		setProperty(PROP_uAAL_DIALOG_ID, dialogID);
		setProperty(PROP_uAAL_UI_CALLER_ID, callerID);
		setProperty(PROP_uAAL_UI_UPDATED_DATA,data);
	    }

	    /** {@ inheritDoc}	 */
	    @Override
	    protected void onResponse(UIStrategyCaller strategy, BusMessage m,
		    String senderID) {
		notifyAbort(
				(String) getProperty(PROP_uAAL_DIALOG_ID), 
				(String) getProperty(PROP_uAAL_UI_CALLER_ID),
				(Resource) getProperty(PROP_uAAL_UI_UPDATED_DATA));
		
	    }

	    /** {@ inheritDoc}	 */
	    @Override
	    protected void onRequest(UIStrategyCaller strategy, BusMessage m,
		    String senderID) {
		abortDialogRequest(
			(String) getProperty(PROP_uAAL_DIALOG_ID),
			(String) getProperty(PROP_uAAL_UI_CALLER_ID));
	    }
	}
    
    private class OntFact implements ResourceFactory{

	/** {@ inheritDoc}	 */
	public Resource createInstance(String classURI, String instanceURI,
		int factoryIndex) {
	    switch (factoryIndex) {
	    case 0:
		return new UIRequestCall();
	    case 1:
		return new ResumeDialogMessage();
	    case 2:
		return new SuspendDialogMessage();
	    case 3:
		return new AbortCall();
	    default:
		break;
	    }
	    return null;
	}
    }
    
    private class MessageOntology extends Ontology{

	private ResourceFactory fac = new OntFact();
	
	/**
	 * @param ontURI
	 */
	public MessageOntology(String ontURI) {
	    super(ontURI);
	}

	/** {@ inheritDoc}	 */
	@Override
	public void create() {
	    createNewRDFClassInfo(UIRequestCall.MY_URI, fac, 0);
	    createNewRDFClassInfo(ResumeDialogMessage.MY_URI, fac, 1);
	    createNewRDFClassInfo(SuspendDialogMessage.MY_URI, fac, 2);
	    createNewRDFClassInfo(AbortCall.MY_URI, fac, 3);
	}
    }
    
    /**
     * The local pending requests.
     */
    private Map<String, UICaller> pendingRequests;
    
    /**
     * A {@link Map} from DialogID to CallerId to keep track of global requests.
     */
    private Map<String, String> globalRequest;

    private Ontology ont;
    /**
     * @param commModule
     * @param name
     */
    public UIStrategyCaller(CommunicationModule commModule, String name) {
	super(commModule, name);
    }

    /** {@ inheritDoc}	 */
    public synchronized void start() {
	super.start();
	ont = new MessageOntology(Resource.uAAL_NAMESPACE_PREFIX + "UIStrategyCallerMesageOntology");
	OntologyManagement.getInstance().register(busModule, ont);
	pendingRequests = new Hashtable<String, UICaller>();
    }

    /**
     * @param commModule
     */
    public UIStrategyCaller(CommunicationModule commModule) {
	super(commModule);
    }

    final boolean setDialogManager(IDialogManager dm) {
	if (dm != null ){
	    globalRequest = new Hashtable<String, String>();
	}else {
	    globalRequest = null;
	}
	return super.setDialogManager(dm);
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
	if (iAmCoordinator()) {
	    UIRequest uiRequest = dialogManager.getSuspendedDialog(dialogID);
	    if (uiRequest != null) {
		uiRequest.setCollectedInput(dialogData);
		adaptationParametersChanged(dialogManager, uiRequest,
			(String) null);
	    } else {
		// trust the dialog manager: either the dialog was aborted
		// previously
		// or it has less priority than the running one
	    }
	} else {
	    sendEventToRemoteBusMember(getCoordinator(), new ResumeDialogMessage(dialogID, dialogData));
	}
    }

    void dialogSuspended(IDialogManager dm, String dialogID){
	if (dm == null || dialogID == null || dm != dialogManager)
	    return;
	suspendDialog(dialogID);
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

	if (iAmCoordinator()) {
	    dialogManager.suspendDialog(dialogID);
	} else {
	    //Send Message
	    sendEventToRemoteBusMember(getCoordinator(), new SuspendDialogMessage(dialogID));
	}
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
		abortDialogRequest(dialogID, uiCaller.getMyID());
	    } 
	}
    }

    private void handleUIRequest(BusMessage message, String senderID) {
	UIRequestCall call = (UIRequestCall) message.getContent();
	UIRequest request = call.getRequest();
	if (!message.senderResidesOnDifferentPeer()) {
	    Form form = request.getDialogForm();
	    if (form != null) {
		BusMember receiver = getBusMember(senderID);
		if (!form.isMessage() && (receiver instanceof UICaller)) {
		    pendingRequests
			    .put(form.getDialogID(), (UICaller) receiver);
		}
		if (!iAmCoordinator()) {
		    message.setReceiver(getCoordinator());
		    send(message);
		    return;
		}
	    } else {
		LogUtils.logError(
			busModule,
			getClass(),
			"handleUIRequest",
			new Object[] { "Dialog form of the UIRequest could not be determined! Not allowed!" },
			null);
		return;
	    }
	}
	if (iAmCoordinator()) {
	    globalRequest.put( request.getDialogID(), senderID);
	    if (dialogManager.checkNewDialog(request)) {

		// keepOriginalRequestForLogging(message);
		// we call adaptationParametersChanged() because the
		// matchmaking logic is the same; we needed only to add
		// an 'if' there
		adaptationParametersChanged(dialogManager,
			request, null);
		// removeTemporaryProperty(resource);
	    } else {
		LogUtils.logDebug(
			busModule,
			getClass(),
			"askDialogManagerForPresentation",
			new Object[] { "The UI Bus ignores the request because it " +
					"trusts that the Dialog Manager will keep " +
					"the request in a queue of suspended dialogs" +
					" and will re-activate it whenever appropriate." },
			null);
	    }
	} else {
	    LogUtils.logError(
		    busModule,
		    getClass(),
		    "handleUIRequest",
		    new Object[] { "combination non-coordinator + remote. We shouldn't get here!!" },
		    null);
	}
    }

    
    private void abortDialogRequest(String dialogID, String callerID) {
	if (iAmCoordinator()) {
	    String handlerID = runningDialogs.getHandler(dialogID);
	    Resource data = cutDialog(handlerID, dialogID);
	    ((UICaller) dialogManager).dialogAborted(dialogID, data);
	    notifyAbort(callerID, dialogID, data);
	    runningDialogs.removeDialogId(dialogID);
	    globalRequest.remove(dialogID);
	} else {
	    placeAsynchronousRequest(getCoordinator(), new AbortCall(dialogID, callerID));
	}
    }
    
    private void abortDialogWiouthCallback(String dialogID, String callerID){
	if (iAmCoordinator()) {
	    String handlerID = runningDialogs.getHandler(dialogID);
	    Resource data = cutDialog(handlerID, dialogID);
	    ((UICaller) dialogManager).dialogAborted(dialogID, data);
	    runningDialogs.removeDialogId(dialogID);
	    globalRequest.remove(dialogID);
	}
    }
    
    /**
     * @param callerID
     * @param dialogID
     * @param abortLocalDialog
     */
    private void notifyAbort(String callerID, String dialogID,
	    Resource data) {
	if (callerID == null || dialogID == null)
	    return;
	BusMember bm = getBusMember(callerID);
	if (bm instanceof UICaller){
	    ((UICaller)bm).dialogAborted(dialogID, data);
	}else {
	    sendAsynchronousResponse(callerID, new AbortCall(dialogID, callerID, data));
	}
    }
    
    /** {@ inheritDoc}	 */
    protected void notifyCallerDialogSubmitted(UIResponse response) {
	UICaller caller = pendingRequests.remove(response.getDialogID());
	if (caller != null){
	    caller.handleUIResponse(response);
	    if (iAmCoordinator()) {
		globalRequest.remove(response.getDialogID());
	    }
	}
	else {
	    if (iAmCoordinator()) {
		String callerId = globalRequest.remove(response.getDialogID());
		sendAsynchronousResponse(callerId, new UIRequestCall(response));
	    }
	}
    }

    /** {@ inheritDoc}	 */
    @Override
    public void close() {
	super.close();
	OntologyManagement.getInstance().unregister(busModule, ont);
	globalRequest = null;
	pendingRequests = null;
    }

    /**
     * A caller is unregistering, and it will be no more reachable, all of its
     * pending requests must be aborted. 
     * @param caller
     */
    public void abortAllPendingRequestsFor(UICaller caller) {
	for (Entry<String, UICaller> entry : pendingRequests.entrySet()) {
	    if (entry.getValue().equals(caller)){
		abortDialogWiouthCallback(entry.getKey(), caller.getMyID());
	    }
	}
    }

    
    /** Coordinator will abort all pending request of the lost peer */
    public void peerLost(PeerCard peer) {
	super.peerLost(peer);
	
	if(iAmCoordinator()){
	    // abort all pending requests from callers in this peer.
	    for (Entry<String, String> entry : globalRequest.entrySet()) {
		if (AbstractBus.getPeerFromBusResourceURI(entry.getValue()).equals(peer)){
		    abortDialogWiouthCallback(entry.getKey(), entry.getValue());
		}
	    }
	}
    }

    /** 
     * Peer will notify that communication channel is broken 
     * and then notify an abort on all pending requests, for all local {@link UICaller}s
     */
    protected void lostCoordinator() {
	super.lostCoordinator();
	Set<UICaller> localCallers = new HashSet<UICaller>();
	Set<Entry<String, UICaller>> entries = pendingRequests.entrySet();
	for (Entry<String, UICaller> entry : entries) {
	    localCallers.add(entry.getValue());
	}
	pendingRequests.clear();
	for (UICaller c : localCallers) {
	    c.communicationChannelBroken();
	}
	for (Entry<String, UICaller> entry : entries) {
	    entry.getValue().dialogAborted(entry.getKey(), null);
	}
    }
    
}
