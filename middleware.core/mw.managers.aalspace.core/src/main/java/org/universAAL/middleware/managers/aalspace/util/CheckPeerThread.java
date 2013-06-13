/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

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
package org.universAAL.middleware.managers.aalspace.util;

import java.util.List;
import java.util.Map;

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceManager;

/**
 * Checks the peers joining to the AALSpace
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class CheckPeerThread implements Runnable {

	ModuleContext moduleContext;
	ControlBroker controlBrolker;
	AALSpaceEventHandler aalSpaceEventHandler;
	AALSpaceManager aalSpaceManager;

	public CheckPeerThread(ModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}

	public void run() {
		Object controlBrokerRef = moduleContext.getContainer()
				.fetchSharedObject(moduleContext,
						new Object[] { ControlBroker.class.getName() });
		Object aalSpaceManagerRef = moduleContext.getContainer()
				.fetchSharedObject(moduleContext,
						new Object[] { AALSpaceManager.class.getName() });
		Object aalSpaceEventHandlerRef = moduleContext.getContainer()
				.fetchSharedObject(moduleContext,
						new Object[] { AALSpaceEventHandler.class.getName() });

		if (controlBrokerRef != null && aalSpaceManagerRef != null
				&& aalSpaceEventHandlerRef != null) {
			try {
				controlBrolker = (ControlBroker) controlBrokerRef;
				aalSpaceManager = (AALSpaceManager) aalSpaceManagerRef;
				aalSpaceEventHandler = (AALSpaceEventHandler) aalSpaceEventHandlerRef;

				// get the list of peers only if the current MW instance joins a
				// AAL Space
				if (aalSpaceManager.getAALSpaceDescriptor() != null) {
					// first get the list of peer address
					List<String> peersAddress = controlBrolker
							.getPeersAddress();

					// second for each new peer address ask for the PeerCard
					Map<String, PeerCard> peers = aalSpaceManager.getPeers();
					for (String address : peersAddress) {
						if (!peers.containsKey(address)) {
							controlBrolker.requestPeerCard(address);
						}
					}
					// third for each old peer address, check if it still is
					// present
					for (String oldPeer : peers.keySet()) {
						if (!peersAddress.contains(oldPeer)) {
							// check if the peer lost is the coordinator. This
							// happens if the coordinator crashed without
							// sending any Request_to_leave
							if (aalSpaceManager.getAALSpaceDescriptor()
									.getSpaceCard().getCoordinatorID()
									.equals(oldPeer)) {
								// than force the leave
								aalSpaceManager.leaveAALSpace(aalSpaceManager
										.getAALSpaceDescriptor());
							}
							controlBrolker.peerLost(peers.get(oldPeer));
						}
					}
				}
			} catch (Exception e) {
				LogUtils.logError(
						moduleContext,
						CheckPeerThread.class,
						"CheckPeerThread",
						new Object[] { "Error during Peer Thread Check: "
								+ e.toString() }, null);
			}
		} else {
			LogUtils.logWarn(
					moduleContext,
					CheckPeerThread.class,
					"CheckPeerThread",
					new Object[] { "Cannnot update the list of peers, some of the service are not available" },
					null);
		}

	}
}
