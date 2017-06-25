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
package org.universAAL.middleware.managers.api;

import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.interfaces.space.SpaceStatus;

/**
 * SpaceEventHandler interface. These methods are called by the lower layer
 * in order to manage events.
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public interface SpaceEventHandler {
	/**
	 * The method is called asynchronously from the bottom layer of the MW after
	 * a previous join(...) request.
	 *
	 * @param descriptor
	 *            all the information about the joined Space
	 */
	public void spaceJoined(SpaceDescriptor descriptor);

	/**
	 * This method allows the SpaceManager to manage a join request from a
	 * remote peer. In this case this SpaceManager is the Space
	 * coordinator
	 *
	 * @param spaceCard
	 * @param sender
	 */
	public void joinRequest(SpaceCard spaceCard, PeerCard sender);

	/**
	 * This method forces the peer to leave a Space
	 *
	 * @param spaceCard
	 */
	public void leaveRequest(SpaceDescriptor spaceCard);

	/**
	 * This method notifies to the coordinator that a peer wants to leave to the
	 * Space
	 *
	 * @param sender
	 */
	public void peerLost(PeerCard peer);

	/**
	 * Method called when a peer joins the Space managed by the
	 * SpaceManager
	 *
	 * @param peer
	 */
	public void peerFound(PeerCard peer);

	/**
	 * A new Space has been found
	 *
	 * @param spaceCard
	 */
	public void newSpacesFound(Set<SpaceCard> spaceCards);

	public void spaceEvent(SpaceStatus newStatus);

	/**
	 * This method allows to configure the set of peers that actually join the
	 * Space managed or joined by this SpaceManager
	 *
	 * @param peer
	 *            Map of peers
	 */
	public void setListOfPeers(Map<String, PeerCard> peer);

	/**
	 * Called in order to alert the Space about the installation of a new MPA
	 */
	public void mpaInstalling(SpaceDescriptor spaceDescriptor);

	/**
	 * Called in order to alert the Space about the installation of a new MPA
	 */
	public void mpaInstalled(SpaceDescriptor spaceDescriptor);

}
