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
package org.universAAL.middleware.managers.space.util;

import java.util.List;
import java.util.Map;

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.api.SpaceEventHandler;
import org.universAAL.middleware.managers.api.SpaceManager;

/**
 * Checks the peers joining to the Space
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class CheckPeerThread implements Runnable {

	ModuleContext moduleContext;
	ControlBroker controlBrolker;
	SpaceEventHandler spaceEventHandler;
	SpaceManager spaceManager;

	public CheckPeerThread(ModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}

	public void run() {
		Object controlBrokerRef = moduleContext.getContainer().fetchSharedObject(moduleContext,
				new Object[] { ControlBroker.class.getName() });
		Object spaceManagerRef = moduleContext.getContainer().fetchSharedObject(moduleContext,
				new Object[] { SpaceManager.class.getName() });
		Object spaceEventHandlerRef = moduleContext.getContainer().fetchSharedObject(moduleContext,
				new Object[] { SpaceEventHandler.class.getName() });

		if (controlBrokerRef != null && spaceManagerRef != null && spaceEventHandlerRef != null) {
			try {
				controlBrolker = (ControlBroker) controlBrokerRef;
				spaceManager = (SpaceManager) spaceManagerRef;
				spaceEventHandler = (SpaceEventHandler) spaceEventHandlerRef;

				// get the list of peers only if the current MW instance joins a Space
				if (spaceManager.getSpaceDescriptor() != null) {
					// first get the list of peer address
					List<String> peersAddress = controlBrolker.getPeersAddress();

					// second for each new peer address ask for the PeerCard
					Map<String, PeerCard> peers = spaceManager.getPeers();
					for (String address : peersAddress) {
						if (!peers.containsKey(address)) {
							controlBrolker.requestPeerCard(address);
						}
					}
					// third for each old peer address, check if it still is present
					for (String oldPeer : peers.keySet()) {
						if (!peersAddress.contains(oldPeer)) {
							// check if the peer lost is the coordinator. This
							// happens if the coordinator crashed without
							// sending any Request_to_leave
							if (spaceManager.getSpaceDescriptor().getSpaceCard().getCoordinatorID()
									.equals(oldPeer)) {
								// than force the leave
								spaceManager.leaveSpace(spaceManager.getSpaceDescriptor());
							}
							controlBrolker.peerLost(peers.get(oldPeer));
						}
					}
				}
			} catch (Exception e) {
				LogUtils.logError(moduleContext, CheckPeerThread.class, "CheckPeerThread",
						new Object[] { "Error during Peer Thread Check: " + e.toString() }, null);
			}
		} else {
			LogUtils.logWarn(moduleContext, CheckPeerThread.class, "CheckPeerThread",
					new Object[] { "Cannnot update the list of peers, some of the service are not available" }, null);
		}

	}
}
