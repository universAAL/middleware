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
import java.util.List;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.IDialogManager;
import org.universAAL.middleware.ui.IUIBus;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.impl.UIStrategyCaller.UIRequestCall;
import org.universAAL.middleware.ui.owl.Modality;
import org.universAAL.middleware.ui.owl.UIBusOntology;
import org.universAAL.middleware.util.ResourceComparator;

/**
 *
 * Implementation of {@link IUIBus} interface
 *
 * @author mtazari
 * @author Carsten Stockloew
 * @author eandgrg
 *
 */
public class UIBusImpl extends AbstractBus implements IUIBus {
	private static Object[] busFetchParams;
	private static UIBusImpl theUIBus = null;
	private static UIBusOntology uiBusOntology = null;
	private static ModuleContext mc;

	public static Object[] getUIBusFetchParams() {
		return busFetchParams.clone();
	}

	public synchronized void assessContentSerialization(Resource content) {
		if (org.universAAL.middleware.util.Constants.debugMode()) {
			LogUtils.logDebug(context, UIBusImpl.class, "assessContentSerialization",
					new Object[] { "Assessing message content serialization:" }, null);

			String str = BusMessage.trySerializationAsContent(content);
			LogUtils.logDebug(context, UIBusImpl.class, "assessContentSerialization",
					new Object[] { "\n      1. serialization dump\n", str,
							"\n      2. deserialize & compare with the original resource\n" },
					null);
			new ResourceComparator().printDiffs(content, (Resource) BusMessage.deserializeAsContent(str));
		}
	}

	public static synchronized void startModule(Container c, ModuleContext mc, Object[] uiBusShareParams,
			Object[] uiBusFetchParams) {
		if (theUIBus == null) {
			UIBusImpl.mc = mc;
			uiBusOntology = new UIBusOntology();
			OntologyManagement.getInstance().register(mc, uiBusOntology);
			theUIBus = new UIBusImpl(mc);
			busFetchParams = uiBusFetchParams;
			c.shareObject(mc, theUIBus, uiBusShareParams);
		}
	}

	public static void stopModule() {
		// new Thread(new Runnable() {
		// public void run() {
		if (theUIBus != null) {
			((UIStrategyCaller) theUIBus.busStrategy).close();
			OntologyManagement.getInstance().unregister(mc, uiBusOntology);
			uiBusOntology = null;
			theUIBus.dispose();
			theUIBus = null;
		}
		// }
		// }, "UIBusStoppingThread").start();
	}

	/**
	 * Create an instance of the {@link IUIBus}.
	 *
	 * @param mc
	 *            {@link ModuleContext}
	 */
	private UIBusImpl(ModuleContext mc) {
		super(mc, "mw.bus.ui.osgi");
		busStrategy.setBus(this);
	}

	/**
	 * @return {@link ModuleContext}
	 */
	public static ModuleContext getModuleContext() {
		return mc;
	}

	protected BusStrategy createBusStrategy(CommunicationModule commModule) {
		// return new UIStrategyCaller(commModule);
		return new UIStrategyCaller(commModule, "UIBusStrategy");
	}

	/**
	 * @param dm
	 *            {@link IDialogManager}
	 */
	public void setDialogManager(IDialogManager dm) {
		if (!((UIStrategyCaller) busStrategy).setDialogManager(dm))
			throw new RuntimeException("Could not set DialogManager");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#abortDialog(java.lang.String,
	 * java.lang.String)
	 */
	public void abortDialog(String callerID, String dialogID) {
		BusMember bm = getBusMember(callerID);
		if (bm instanceof UICaller) {
			((UIStrategyCaller) busStrategy).abortDialog(callerID, dialogID);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#adaptationParametersChanged(org.
	 * universAAL .middleware.ui.DialogManager,
	 * org.universAAL.middleware.ui.UIRequest, java.lang.String)
	 */
	public void adaptationParametersChanged(IDialogManager dm, UIRequest uiRequest, String changedProp) {
		((UIStrategyCaller) busStrategy).adaptationParametersChanged(dm, uiRequest, changedProp);
	}

	// public UIHandlerProfile getMatchingUiHandler(){
	// return ((UIStrategyCaller) busStrategy).get
	// }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#addNewProfile(java.lang.String,
	 * org.universAAL.middleware.ui.UIHandlerProfile)
	 */
	public void addNewProfile(String handlerID, UIHandlerProfile newProfile) {
		Object o = registry.getBusMemberByID(handlerID);
		if (o instanceof UIHandler) {
			((UIStrategyCaller) busStrategy).addRegistration(handlerID, newProfile);
			registry.addRegParams(handlerID, new UIHandlerProfile[] { newProfile });
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#dialogFinished(java.lang.String,
	 * org.universAAL.middleware.ui.UIResponse)
	 */
	public void dialogFinished(String handlerID, UIResponse response) {
		if (response != null) {
			((UIStrategyCaller) busStrategy).dialogFinished(handlerID, response);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#dialogSuspended(org.universAAL.
	 * middleware .ui.DialogManager, java.lang.String)
	 */
	public void dialogSuspended(IDialogManager dm, String dialogID) {
		((UIStrategyCaller) busStrategy).dialogSuspended(dm, dialogID);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#removeMatchingProfile(java.lang.
	 * String , org.universAAL.middleware.ui.UIHandlerProfile)
	 */
	public void removeMatchingProfile(String handlerID, UIHandlerProfile oldProfile) {
		Object o = registry.getBusMemberByID(handlerID);
		if (o instanceof UIHandler) {
			((UIStrategyCaller) busStrategy).removeMatchingRegistries(handlerID, oldProfile);
			registry.removeRegParams(handlerID, new UIHandlerProfile[] { oldProfile });
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#resumeDialog(java.lang.String,
	 * java.lang.String, org.universAAL.middleware.rdf.Resource)
	 */
	public void resumeDialog(String callerID, String dialogID, Resource dialogData) {
		BusMember bm = getBusMember(callerID);
		if (bm instanceof IDialogManager && dialogData instanceof UIRequest) {
			((UIStrategyCaller) busStrategy).adaptationParametersChanged((IDialogManager) bm, (UIRequest) dialogData,
					null);
		} else if (bm instanceof UICaller) {
			((UIStrategyCaller) busStrategy).resumeDialog(dialogID, dialogData);
		}
	}

	/**
	 *
	 * Asks the bus to find an appropriate UI handler and forward the request to
	 * it for handling
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#brokerUIRequest(java.lang.String,
	 *      org.universAAL.middleware.ui.UIRequest)
	 *
	 * @param callerID
	 *            the ID of the UICaller that is asking the bus
	 * @param req
	 *            the request to be forwarded to a UI handler
	 *
	 */
	public void brokerUIRequest(String callerID, UIRequest req) {
		BusMember bm = getBusMember(callerID);
		if (bm instanceof UICaller) {
			UIRequestCall call = ((UIStrategyCaller) busStrategy).new UIRequestCall(req, callerID);
			assessContentSerialization(call);
			brokerMessage(callerID, new BusMessage(MessageType.request, call, this));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#unregister(java.lang.String,
	 * org.universAAL.middleware.ui.UICaller)
	 */
	public void unregister(String callerID, UICaller caller) {
		((UIStrategyCaller) busStrategy).abortAllPendingRequestsFor(caller);
		if (caller instanceof IDialogManager) {
			setDialogManager(null);
		}
		super.unregister(callerID, caller);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#unregister(java.lang.String,
	 * org.universAAL.middleware.ui.UIHandler)
	 */
	public void unregister(String handlerID, UIHandler handler) {
		Object o = registry.getBusMemberByID(handlerID);
		if (o != null && o == handler) {
			super.unregister(handlerID, handler);
			((UIStrategyCaller) busStrategy).removeAllRegistries(handlerID);
		}
	}

	@Override
	public void unregister(String memberID, BusMember m) {
		if (m instanceof UICaller)
			unregister(memberID, (UICaller) m);
		else if (m instanceof UIHandler)
			unregister(memberID, (UIHandler) m);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.ui.IUIBus#userLoggedIn(java.lang.String,
	 * org.universAAL.middleware.rdf.Resource,
	 * org.universAAL.middleware.owl.supply.AbsLocation)
	 */
	public void userLoggedIn(String handlerID, Resource user, AbsLocation loginLocation) {
		Object o = registry.getBusMemberByID(handlerID);
		if (o instanceof UIHandler && user != null) {
			((UIStrategyCaller) busStrategy).userLoggedIn(handlerID, user, loginLocation);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.modules.listener.MessageListener#
	 * handleSendError
	 * (org.universAAL.middleware.connectors.util.ChannelMessage,
	 * org.universAAL.
	 * middleware.connectors.exception.CommunicationConnectorException)
	 */
	public void handleSendError(ChannelMessage message, CommunicationConnectorException e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns list of {@link UIHandlerProfile}s that match the given expression
	 */
	public UIHandlerProfile[] getMatchingProfiles(String modalityRegex) {
		String lowerCaseModality = modalityRegex.toLowerCase();
		List<UIHandlerProfile> matchedProfiles = new ArrayList<UIHandlerProfile>();
		BusMember[] members = registry.getAllBusMembers();
		for (BusMember member : members) {
			if (member instanceof UIHandler) {
				UIHandler handler = (UIHandler) member;
				List<UIHandlerProfile> realizedProfiles = (List<UIHandlerProfile>) handler.getRealizedHandlerProfiles();
				for (UIHandlerProfile profile : realizedProfiles) {
					Modality[] supportedModalities = profile.getSupportedInputModalities();
					for (Modality m : supportedModalities) {
						if (m.toString().toLowerCase().matches(lowerCaseModality)) {
							matchedProfiles.add(profile);
							break;
						}
					}
				}
			}
		}
		return matchedProfiles.toArray(new UIHandlerProfile[0]);
	}
}
