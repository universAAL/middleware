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
package org.universAAL.middleware.modules;

import java.util.Dictionary;
import java.util.List;

import org.universAAL.middleware.brokers.message.space.SpaceMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;

/**
 * The implementations of this interface manage the Space life-cycle:
 * creation, update, destroy.
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public interface SpaceModule extends Module {
	/**
	 * This method fetches a list of Spaces without any filter
	 *
	 * @return List of discovered Spaces
	 */
	public List<SpaceCard> getSpaces();

	/**
	 * This method fetches a list of Spaces according to a filter
	 *
	 * @param filters
	 *            A set of key, value pairs for filtering the Spaces
	 * @return
	 */
	public List<SpaceCard> getSpaces(Dictionary<String, String> filters);

	/**
	 * This method creates a new Space
	 *
	 * @param configurations
	 *            The configuration parameters for the Space
	 * @return
	 */
	public void newSpace(SpaceCard spaceCard);

	/**
	 * This method renews the Space
	 *
	 * @param spaceCard
	 */
	public void renewSpace(SpaceCard spaceCard);

	/**
	 * Destroy a Space
	 *
	 * @param spaceCard
	 *            Space to destroy
	 */
	public void destroySpace(SpaceCard spaceCard);

	/**
	 * This method allows to join to an existing Space
	 *
	 * @param spaceCoordinator
	 *            The PeerCard of the Space coordinator to which to sent the
	 *            request
	 * @param spaceCard
	 *            The SpaceCard of the space the peer aims to join
	 */
	public void joinSpace(PeerCard spaceCoordinator, SpaceCard spaceCard);

	/**
	 * This method allows to leave a Space
	 *
	 * @param spaceCard
	 *            Space Card of the Space to leave
	 */
	public void leaveSpace(PeerCard spaceCoordinator, SpaceCard spaceCard);

	/**
	 * This method announces to all the peers to leave the space
	 *
	 * @param spaceDescriptor
	 */
	public void requestToLeave(SpaceDescriptor spaceDescriptor);

	/**
	 * This method allows to send a request for the PeerCard of the peer with
	 * the specified address
	 *
	 * @param peerAddress
	 *            the address of the peer
	 * @param spaceDescriptor
	 *            the Space descriptor
	 */
	public void requestPeerCard(SpaceDescriptor spaceDescriptor, String peerAddress);

	/**
	 * This method is called as soon as an SpaceMessage has been received
	 *
	 * @param message
	 */
	public void messageFromSpace(SpaceMessage message, PeerCard sender);

	/**
	 * This method allows to add a new Peer to the Space.
	 *
	 * @param spaceDescriptor
	 *            All the information regarding the Space for the new Peer
	 * @param peer
	 *            The Peer to add to the Space
	 */
	public void addPeer(SpaceDescriptor spaceDescriptor, PeerCard peer);

	/**
	 * This method propagates the event of new PeerAdded to the whole Space
	 *
	 * @param spaceCard
	 *            The Space where to propagate the event
	 * @param peerCard
	 *            The new peer added
	 */
	public void announceNewPeer(SpaceCard spaceCard, PeerCard peerCard);

	/**
	 * This method configures the channel used in order to send/receive
	 * Space messages
	 */
	public void configureSpaceChannel();

	/**
	 * This method fetches the list of peer address joining to the same
	 * Space
	 *
	 * @param peers
	 * @return The consolidated list of peers
	 */
	public List<String> getPeersAddress();

}
