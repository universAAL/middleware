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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.persona.middleware.Activator;
import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.PResource;
import org.persona.middleware.input.InputEvent;
import org.persona.middleware.input.impl.InputBusImpl;
import org.persona.middleware.output.DialogManager;
import org.persona.middleware.output.OutputEvent;
import org.persona.middleware.output.OutputEventPattern;
import org.persona.middleware.output.OutputPublisher;
import org.persona.middleware.output.OutputSubscriber;

import de.fhg.igd.ima.sodapop.AbstractBus;
import de.fhg.igd.ima.sodapop.BusMember;
import de.fhg.igd.ima.sodapop.BusStrategy;
import de.fhg.igd.ima.sodapop.SodaPop;
import de.fhg.igd.ima.sodapop.msg.Message;
import de.fhg.igd.ima.sodapop.msg.MessageType;

/**
 * @author mtazari
 * 
 */
public class OutputStrategy extends BusStrategy {

	public static final String PROP_PERSONA_CHANGED_PROPERTY = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "changedProperty";
	public static final String PROP_PERSONA_DIALOG_ID = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "dialogID";
	public static final String PROP_PERSONA_OUTPUT_EVENT = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "theOutputEvent";
	public static final String PROP_PERSONA_OUTPUT_HANDLER_ID = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "theSubscriber";
	public static final String PROP_PERSONA_OUTPUT_IS_NEW_EVENT = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "isNewEvent";
	public static final String PROP_PERSONA_OUTPUT_IS_POPPED_MESSAGE = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "isPoppedMessage";
	public static final String PROP_PERSONA_OUTPUT_REMOVE_SUBSCRIPTION = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "removeSubscription";
	public static final String PROP_PERSONA_OUTPUT_SUBSCRIPTION = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "theSubscription";
	public static final String PROP_PERSONA_OUTPUT_UPDATED_DATA = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "updatedData";
	public static final String TYPE_PERSONA_OUTPUT_BUS_COORDINATOR =
		PResource.PERSONA_VOCABULARY_NAMESPACE + "Coordinator";
	public static final String TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "SubscriberNotification";
	public static final String TYPE_PERSONA_OUTPUT_BUS_SUBSCRIPTION = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "Subscription";
	public static final String TYPE_PERSONA_SUSPEND_DIALOG = 
		PResource.PERSONA_VOCABULARY_NAMESPACE + "SuspendDialog";
	
	private class Subscription {
		String subscriberID;
		OutputEventPattern filter;
		
		Subscription(String subscriberID, OutputEventPattern filter) {
			this.subscriberID = subscriberID;
			this.filter = filter;
		}
	}

	private DialogManager dialogManager = null;
	private Vector globalSubscriptions = null;
	private Hashtable runningDialogs = null;
	private String[] theCoordinator = null;
	private Hashtable waitingForCut = null;
	private Hashtable pendingEvents = new Hashtable();

	public OutputStrategy(SodaPop sodapop) {
		super(sodapop);
	}

	void abortDialog(String dialogID) {
		if (dialogID != null)
			pendingEvents.remove(dialogID);
	}

	void abortDialog(String requester, String dialogID) {
		BusMember bm = getBusMember(requester);
		if (bm instanceof OutputPublisher  &&  dialogID != null) {
			String publisher = (String) pendingEvents.remove(dialogID);
			if (bm == dialogManager
					|| requester.equals(publisher)) {
				AbstractBus ib = getLocalBusByName(
						MiddlewareConstants.PERSONA_BUS_NAME_INPUT);
				if (ib instanceof InputBusImpl)
					((InputBusImpl) ib).abortDialog(this, dialogID);
				notifyHandler_abortDialog(dialogID);
			} else if (publisher != null)
				pendingEvents.put(dialogID, publisher);
		}
	}

	void adaptationParametersChanged(DialogManager dm, OutputEvent event, String changedProp) {
		if (dm != null  &&  dm == dialogManager) {
			int aux, numInMod = 0, matchResult = OutputEventPattern.MATCH_LEVEL_FAILED;
			synchronized(globalSubscriptions) {
				String selectedHandler = null,
						currentHandler = (String) runningDialogs.get(event.getDialogID());
				if (changedProp == null) {
					// this is a new dialog published to the bus
					if (currentHandler != null) {
						// strange situation: duplication dialog ID??!!
						// TODO: a log entry!
						System.out.println("??!! strange situation: duplicate dialog ID??!!");
					}
				} else if (currentHandler == null) {
					// dialog manager data is inconsistent with my data
					// TODO: a log entry!
					System.out.println("??!! dialog manager data is inconsistent with my data??!!");
				}
				for (Iterator i=globalSubscriptions.iterator(); i.hasNext();) {
					Subscription s = (Subscription) i.next();
					aux = s.filter.matches(event);
					if (aux > OutputEventPattern.MATCH_LEVEL_FAILED) {
						if (s.subscriberID.equals(currentHandler)) {
							notifyHandler_apChanged(currentHandler, event, changedProp);
							return;
						}
						int n = s.filter.getNumberOfSupportedInputModalities();
						if (aux > matchResult
								||  n > numInMod) {
							numInMod = n;
							matchResult = aux;
							selectedHandler = s.subscriberID;
						}
					}
				}
				if (selectedHandler == null) {
					// TODO: what to do here? At least a log entry
					System.out.println("!!!! no handler could be selected!!!!");
					return;
				}
				if (currentHandler != null) {
					PResource collectedData = notifyHandler_cutDialog(currentHandler, event.getDialogID());
					if (collectedData != null)
						event.setCollectedInput(collectedData);
					runningDialogs.remove(event.getDialogID());
				}
				runningDialogs.put(event.getDialogID(), selectedHandler);
				notifyHandler_handle(selectedHandler, event);
			}
		}
	}

	void addRegParams(String subscriberID, OutputEventPattern newSubscription) {
		if (newSubscription == null)
			return;

		if (isCoordinator())
			globalSubscriptions.add(new Subscription(subscriberID, newSubscription));
		else {
			PResource pr = new PResource();
			pr.addType(TYPE_PERSONA_OUTPUT_BUS_SUBSCRIPTION, true);
			pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, subscriberID);
			pr.setProperty(PROP_PERSONA_OUTPUT_SUBSCRIPTION, newSubscription);
			Activator.assessContentSerialization(pr);
			Message m = new Message(MessageType.p2p_event, pr);
			m.setReceivers(theCoordinator);
			sodapop.propagateMessage(bus, m);
		}
	}

	void dialogFinished(final String subscriberID, final String dialogID, final boolean poppedMessage) {
		if (isCoordinator()) {
			// do it in a new thread to make sure that no deadlock will happen
			new Thread() {
				public void run() {
					synchronized(globalSubscriptions) {
						if (subscriberID.equals(runningDialogs.get(dialogID))) {
							runningDialogs.remove(dialogID);
							if (!poppedMessage)
								dialogManager.dialogFinished(dialogID);
						}
					}
				}
			}.start();
		} else {
			PResource pr = new PResource();
			pr.addType(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION, true);
			pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, subscriberID);
			pr.setProperty(PROP_PERSONA_DIALOG_ID, dialogID);
			if (poppedMessage)
				pr.setProperty(PROP_PERSONA_OUTPUT_IS_POPPED_MESSAGE, Boolean.TRUE);
			Activator.assessContentSerialization(pr);
			Message m = new Message(MessageType.p2p_event, pr);
			m.setReceivers(theCoordinator);
			sodapop.propagateMessage(bus, m);
		}
	}
	
	void dialogSuspended(DialogManager dm, String dialogID) {
		if (dialogID != null  &&  dm != null  &&  dm == dialogManager) {
			// most probably does not need 'synchronized(globalSubscriptions)'
			runningDialogs.remove(dialogID);
		}
	}

	/**
	 * @see de.fhg.igd.ima.sodapop.BusStrategy#handle(de.fhg.igd.ima.sodapop.msg.Message, String)
	 */
	public void handle(Message msg, String senderID) {
		if (msg == null
				|| !(msg.getContent() instanceof PResource))
			return;

		PResource res = (PResource) msg.getContent();
		switch (msg.getType().ord()) {
		case MessageType.EVENT:
			if (res.getType().equals(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION)) {
				String handlerID = (String) res.getProperty(PROP_PERSONA_OUTPUT_HANDLER_ID);
				OutputEvent oe = (OutputEvent) res.getProperty(PROP_PERSONA_OUTPUT_EVENT);
				Boolean isNew = (Boolean) res.getProperty(PROP_PERSONA_OUTPUT_IS_NEW_EVENT);
				if (handlerID == null  ||  oe == null  ||  isNew == null) {
					// TODO: a log entry!
					return;
				} else if (isNew.booleanValue())
					notifyHandler_handle(handlerID, oe);
				else
					notifyHandler_apChanged(handlerID, oe, res.getProperty(PROP_PERSONA_CHANGED_PROPERTY).toString());
			} else if (res instanceof OutputEvent) {
				if (!msg.isRemote())
					pendingEvents.put(
							((OutputEvent) res).getDialogForm().getDialogID(),
							senderID);
				if (isCoordinator()) {
					if (dialogManager.checkNewDialog((OutputEvent) res))
						adaptationParametersChanged(dialogManager, (OutputEvent) res, null);
				} else {
					msg.setReceivers(theCoordinator);
					sodapop.propagateMessage(bus, msg);
				}
			}
			break;
		case MessageType.P2P_EVENT:
			if (res.getType().equals(TYPE_PERSONA_OUTPUT_BUS_COORDINATOR)) {
				if (dialogManager == null
						&& theCoordinator == null
						&& res.getURI().startsWith(
									MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
					synchronized(this) {
						theCoordinator = new String[] {
								res.getURI().substring(
										MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length())
						};
						notifyAll();
					}
				}
			} else if (isCoordinator()) {
				if (res.getType().equals(TYPE_PERSONA_SUSPEND_DIALOG))
					suspendDialog((String) res.getProperty(PROP_PERSONA_DIALOG_ID));
				else if (res.getType().equals(TYPE_PERSONA_OUTPUT_BUS_SUBSCRIPTION)) {
					String handler = (String) res.getProperty(
							PROP_PERSONA_OUTPUT_HANDLER_ID);
					OutputEventPattern subscription = (OutputEventPattern) res.getProperty(
							PROP_PERSONA_OUTPUT_SUBSCRIPTION);
					Boolean removes = (Boolean) res.getProperty(
							PROP_PERSONA_OUTPUT_REMOVE_SUBSCRIPTION);
					if (handler != null)
						if (subscription != null)
							if (removes != null  &&  removes.booleanValue())
								removeMatchingRegParams(handler, subscription);
							else
								addRegParams(handler, subscription);
						else if (removes != null  &&  removes.booleanValue())
							removeRegParams(handler);
				} else if (res.getType().equals(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION)) {
					String handler = (String) res.getProperty(
							PROP_PERSONA_OUTPUT_HANDLER_ID);
					String dialogID = (String) res.getProperty(
							PROP_PERSONA_DIALOG_ID);
					PResource data = (PResource) res.getProperty(
							PROP_PERSONA_OUTPUT_UPDATED_DATA);
					if (dialogID != null)
						if (handler == null)
							if (data == null)
								notifyHandler_abortDialog(dialogID);
							else
								resumeDialog(dialogID, data);
						else if (data == null)
							dialogFinished(handler, dialogID,
									Boolean.TRUE.equals(res.getProperty(PROP_PERSONA_OUTPUT_IS_POPPED_MESSAGE)));
				}
			} else if (res.getType().equals(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION)) {
				String handlerID = (String) res.getProperty(PROP_PERSONA_OUTPUT_HANDLER_ID);
				String dialogID = (String) res.getProperty(PROP_PERSONA_DIALOG_ID);
				if (handlerID == null  ||  dialogID == null)
					// TODO: a log entry!
					return;
				if (handlerID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
					handlerID = handlerID.substring(
							MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length());
				Object o = getBusMember(handlerID);
				if (o instanceof OutputSubscriber)
					((OutputSubscriber) o).cutDialog(dialogID);
			}
			break;
		case MessageType.P2P_REPLY:
			if (res.getType().equals(TYPE_PERSONA_OUTPUT_BUS_COORDINATOR)) {
				if (theCoordinator == null  &&  res.getURI().startsWith(
						MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
					synchronized(this) {
						theCoordinator = new String[] {
								res.getURI().substring(
										MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length())
						};
						notifyAll();
					}
				}
			}
			break;
		case MessageType.P2P_REQUEST:
			if (isCoordinator()
					&&  res.getType().equals(TYPE_PERSONA_OUTPUT_BUS_COORDINATOR)) {
				res = new PResource(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX
						+ sodapop.getID());
				res.addType(TYPE_PERSONA_OUTPUT_BUS_COORDINATOR, true);
				Activator.assessContentSerialization(res);
				sodapop.propagateMessage(bus, msg.createReply(res));
			}
			break;
		case MessageType.REPLY:
			if (isCoordinator()  &&  res instanceof InputEvent
					&& waitingForCut.get(((InputEvent) res).getDialogID()) instanceof String) {
				synchronized (waitingForCut) {
					waitingForCut.put(((InputEvent) res).getDialogID(), res);
					notifyAll();
				}
			}
			break;
		case MessageType.REQUEST:
			if (!isCoordinator()
					&& res.getType().equals(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION)) {
				String handler = (String) res.getProperty(PROP_PERSONA_OUTPUT_HANDLER_ID);
				String dialogID = (String) res.getProperty(PROP_PERSONA_DIALOG_ID);
				if (handler != null  &&  dialogID != null
						&&  handler.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
					Object o = getBusMember(handler.substring(
							MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
					if (o instanceof OutputSubscriber) {
						InputEvent ie = new InputEvent();
						ie.setProperty(InputEvent.PROP_DIALOG_ID, dialogID);
						PResource userInput = ((OutputSubscriber) o).cutDialog(dialogID);
						if (userInput != null)
							ie.setProperty(InputEvent.PROP_DIALOG_DATA, userInput);
						Activator.assessContentSerialization(ie);
						sodapop.propagateMessage(bus, msg.createReply(ie));
					}
				}
			}
			break;
		}
	}
	
	private boolean isCoordinator() {
		if (dialogManager == null  &&  theCoordinator == null) {
			PResource r = new PResource();
			r.addType(TYPE_PERSONA_OUTPUT_BUS_COORDINATOR, true);
			Activator.assessContentSerialization(r);
			Message m = new Message(MessageType.p2p_request, r);
			synchronized(this) {
				while (dialogManager == null  &&  theCoordinator == null) {
					sodapop.propagateMessage(bus, m);
					try { wait(); } catch (Exception e) {}
				}
			}
		}
		// only the coordinator has an adaptation engine
		return dialogManager != null;
	}
	
	private void notifyHandler_abortDialog(String dialogID) {
		if (isCoordinator()) {
			String handlerID = (String) runningDialogs.remove(dialogID);
			if (handlerID == null)
				return;
			String peerID = MiddlewareConstants.extractPeerID(handlerID);
			if (sodapop.getID().equals(peerID)) {
				Object o = getBusMember(handlerID.substring(
						MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
				if (o instanceof OutputSubscriber) {
					((OutputSubscriber) o).cutDialog(dialogID);
				}
			} else {
				PResource pr = new PResource();
				pr.addType(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION, true);
				pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, handlerID);
				pr.setProperty(PROP_PERSONA_DIALOG_ID, dialogID);
				Activator.assessContentSerialization(pr);
				Message m = new Message(MessageType.p2p_event, pr);
				m.setReceivers(new String[]{peerID});
				sodapop.propagateMessage(bus, m);
			}
		} else {
			PResource pr = new PResource();
			pr.addType(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION, true);
			pr.setProperty(PROP_PERSONA_DIALOG_ID, dialogID);
			Activator.assessContentSerialization(pr);
			Message m = new Message(MessageType.p2p_event, pr);
			m.setReceivers(theCoordinator);
			sodapop.propagateMessage(bus, m);
		}
	}
	
	private void notifyHandler_apChanged(String handlerID, OutputEvent event, String changedProp) {
		String peerID = MiddlewareConstants.extractPeerID(handlerID);
		if (sodapop.getID().equals(peerID)) {
			Object o = getBusMember(handlerID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof OutputSubscriber)
				((OutputSubscriber) o).adaptationParametersChanged(event.getDialogID(), changedProp, event.getProperty(changedProp));
		} else if (isCoordinator()) {
			PResource pr = new PResource();
			pr.addType(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION, true);
			pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, handlerID);
			pr.setProperty(PROP_PERSONA_OUTPUT_EVENT, event);
			pr.setProperty(PROP_PERSONA_CHANGED_PROPERTY, changedProp);
			pr.setProperty(PROP_PERSONA_OUTPUT_IS_NEW_EVENT, Boolean.FALSE);
			Activator.assessContentSerialization(pr);
			Message m = new Message(MessageType.event, pr);
			m.setReceivers(new String[] {peerID});
			sodapop.propagateMessage(bus, m);
		} // else
			// TODO: a log entry
	}
	
	private PResource notifyHandler_cutDialog(String handlerID, String dialogID) {
		String peerID = MiddlewareConstants.extractPeerID(handlerID);
		if (sodapop.getID().equals(peerID)) {
			Object o = getBusMember(handlerID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof OutputSubscriber)
				return ((OutputSubscriber) o).cutDialog(dialogID);
		} else if (isCoordinator()) {
			PResource pr = new PResource();
			pr.addType(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION, true);
			pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, handlerID);
			pr.setProperty(PROP_PERSONA_DIALOG_ID, dialogID);
			Activator.assessContentSerialization(pr);
			Message m = new Message(MessageType.request, pr);
			m.setReceivers(new String[] {peerID});
			synchronized (waitingForCut) {
				waitingForCut.put(dialogID, handlerID);
				sodapop.propagateMessage(bus, m);
				while (!(waitingForCut.get(handlerID) instanceof InputEvent)) {
					try { wait(); } catch (Exception e) {}
				}
				InputEvent ie = (InputEvent) waitingForCut.remove(handlerID);
				return (PResource) ie.getProperty(InputEvent.PROP_DIALOG_DATA);
			}
		} // else
			// TODO: a log entry
		return null;
	}
	
	private void notifyHandler_handle(String handlerID, OutputEvent event) {
		String peerID = MiddlewareConstants.extractPeerID(handlerID);
		if (sodapop.getID().equals(peerID)) {
			Object o = getBusMember(handlerID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
			if (o instanceof OutputSubscriber)
				((OutputSubscriber) o).handleOutputEvent(event);
		} else if (isCoordinator()) {
			PResource pr = new PResource();
			pr.addType(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION, true);
			pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, handlerID);
			pr.setProperty(PROP_PERSONA_OUTPUT_EVENT, event);
			pr.setProperty(PROP_PERSONA_OUTPUT_IS_NEW_EVENT, Boolean.TRUE);
			Activator.assessContentSerialization(pr);
			Message m = new Message(MessageType.event, pr);
			m.setReceivers(new String[] {peerID});
			sodapop.propagateMessage(bus, m);
		} // else
			// TODO: a log entry
	}

	void removeMatchingRegParams(String subscriberID, OutputEventPattern oldSubscription) {
		if (subscriberID == null  ||  oldSubscription == null)
			return;

		if (isCoordinator()) {
			synchronized (globalSubscriptions) {
				for (Iterator i=globalSubscriptions.iterator(); i.hasNext();) {
					Subscription s = (Subscription) i.next();
					if (s.subscriberID.equals(subscriberID)  &&  oldSubscription.matches(s.filter))
						i.remove();
				}
			}
		} else {
			PResource pr = new PResource();
			pr.addType(TYPE_PERSONA_OUTPUT_BUS_SUBSCRIPTION, true);
			pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, subscriberID);
			pr.setProperty(PROP_PERSONA_OUTPUT_SUBSCRIPTION, oldSubscription);
			pr.setProperty(PROP_PERSONA_OUTPUT_REMOVE_SUBSCRIPTION, Boolean.TRUE);
			Activator.assessContentSerialization(pr);
			Message m = new Message(MessageType.p2p_event, pr);
			m.setReceivers(theCoordinator);
			sodapop.propagateMessage(bus, m);
		}
	}

	void removeRegParams(String subscriberID) {
		if (subscriberID == null)
			return;

		if (isCoordinator()) {
			synchronized (globalSubscriptions) {
				for (Iterator i=globalSubscriptions.iterator(); i.hasNext();) {
					Subscription s = (Subscription) i.next();
					if (s.subscriberID.equals(subscriberID))
						i.remove();
				}
			}
		} else {
			PResource pr = new PResource();
			pr.addType(TYPE_PERSONA_OUTPUT_BUS_SUBSCRIPTION, true);
			pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, subscriberID);
			pr.setProperty(PROP_PERSONA_OUTPUT_REMOVE_SUBSCRIPTION, Boolean.TRUE);
			Activator.assessContentSerialization(pr);
			Message m = new Message(MessageType.p2p_event, pr);
			m.setReceivers(theCoordinator);
			sodapop.propagateMessage(bus, m);
		}
	}
	
	/**
	 * Called only when an application is finished with a subdialog and wants to resume
	 * the original dialog passing the changed dialog data.
	 */
	void resumeDialog(String dialogID, PResource dialogData) {
		if (isCoordinator()) {
			OutputEvent oe = dialogManager.getSuspendedDialog(dialogID);
			if (oe != null) {
				oe.setCollectedInput(dialogData);
				adaptationParametersChanged(dialogManager, oe, null);
			} else {
				// trust the dialog manager: either the dialog was aborted previously
				//                           or it has less priority than the running one
			}
		} else {
			PResource res = new PResource();
			res.addType(TYPE_PERSONA_OUTPUT_BUS_NOTIFICATION, true);
			res.setProperty(PROP_PERSONA_DIALOG_ID, dialogID);
			res.setProperty(PROP_PERSONA_OUTPUT_UPDATED_DATA, dialogData);
			Activator.assessContentSerialization(res);
			Message m = new Message(MessageType.p2p_event, res);
			m.setReceivers(theCoordinator);
			sodapop.propagateMessage(bus, m);
		}
	}

	void setDialogManager(DialogManager dm) {
		if (dm == null  ||  dialogManager != null  ||  theCoordinator != null)
			// TODO: a log entry!?!
			return;
		
		globalSubscriptions = new Vector();
		runningDialogs = new Hashtable();
		waitingForCut = new Hashtable(2);
		
		PResource res = new PResource(
				MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX
				+ sodapop.getID());
		res.addType(TYPE_PERSONA_OUTPUT_BUS_COORDINATOR, true);

		synchronized(this) {
			dialogManager = dm;
			Activator.assessContentSerialization(res);
			sodapop.propagateMessage(bus, new Message(MessageType.p2p_event, res));
			notifyAll();
		}
	}
	
	/**
	 * Called only when an I/O handler has called dialogFinished with an instance of
	 * {@link org.persona.middleware.dialog.SubdialogTrigger} so that we must only
	 * notify to suspend this dialog until the original publisher calls 'resume'.
	 */
	void suspendDialog(String dialogID) {
		if (dialogID == null)
			return;
		
		if (isCoordinator()) {
			dialogManager.suspendDialog(dialogID);
//			synchronized(globalSubscriptions) {
//				String currentHandler = (String) runningDialogs.remove(dialogID);
//				if (currentHandler != null) {
//					String peerID = MiddlewareConstants.extractPeerID(currentHandler);
//					if (sodapop.getID().equals(peerID)) {
//						Object o = getBusMember(currentHandler.substring(
//								MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()));
//						if (o instanceof OutputSubscriber)
//							((OutputSubscriber) o).cutDialog(dialogID);
//					} else {
//						PResource pr = new PResource();
//						pr.addType(TYPE_PERSONA_SUSPEND_DIALOG, true);
//						pr.setProperty(PROP_PERSONA_OUTPUT_HANDLER_ID, currentHandler);
//						pr.setProperty(PROP_PERSONA_DIALOG_ID, dialogID);
//						Message m = new Message(MessageType.p2p_event, pr);
//						m.setReceivers(new String[] {peerID});
//						sodapop.propagateMessage(bus, m);
//					}
//				}
//			}
		} else {
			PResource res = new PResource();
			res.addType(TYPE_PERSONA_SUSPEND_DIALOG, true);
			res.setProperty(PROP_PERSONA_DIALOG_ID, dialogID);
			Activator.assessContentSerialization(res);
			Message m = new Message(MessageType.p2p_event, res);
			m.setReceivers(theCoordinator);
			sodapop.propagateMessage(bus, m);
		}
	}
}
