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

import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;

/**
 * The implementations of this interface manage the AALSpace life-cycle:
 * creation, update, destroy.
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public interface AALSpaceModule extends Module {
    /**
     * This method fetches a list of AALSpaces without any filter
     * 
     * @return List of discovered AAL Spaces
     */
    public List<AALSpaceCard> getAALSpaces();

    /**
     * This method fetches a list of AALSpaces according to a filter
     * 
     * @param filters
     *            A set of key, value pairs for filtering the AAL Spaces
     * @return
     */
    public List<AALSpaceCard> getAALSpaces(Dictionary<String, String> filters);

    /**
     * This method creates a new AALSpace
     * 
     * @param configurations
     *            The configuration parameters for the AALSpace
     * @return
     */
    public void newAALSpace(AALSpaceCard aalSpaceCard);

    /**
     * This method renews the AALSpace
     * 
     * @param spaceCard
     */
    public void renewAALSpace(AALSpaceCard aalSpaceCard);

    /**
     * Destroy an AAL Space
     * 
     * @param spaceCard
     *            AALSpace to destroy
     */
    public void destroyAALSpace(AALSpaceCard aalSpaceCard);

    /**
     * This method allows to join to an existing AALSpace
     * 
     * @param spaceCoordinator
     *            The PeerCard of the AALSpace coordinator to which to sent the
     *            request
     * @param spaceCard
     *            The AALSpaceCard of the AAL space the peer aims to join
     */
    public void joinAALSpace(PeerCard spaceCoordinator,
	    AALSpaceCard aalSpaceCard);

    /**
     * This method allows to leave an AALSpace
     * 
     * @param spaceCard
     *            Space Card of the AALSpace to leave
     */
    public void leaveAALSpace(PeerCard spaceCoordinator,
	    AALSpaceCard aalSpaceCard);

    /**
     * This method announces to all the peers to leave the AAL space
     * 
     * @param spaceDescriptor
     */
    public void requestToLeave(AALSpaceDescriptor aalSpaceDescriptor);

    /**
     * This method allows to send a request for the PeerCard of the peer with
     * the specified address
     * 
     * @param peerAddress
     *            the address of the peer
     * @param spaceDescriptor
     *            the AAL Space descriptor
     */
    public void requestPeerCard(AALSpaceDescriptor spaceDescriptor,
	    String peerAddress);

    /**
     * This method is called as soon as an AALSpaceMessage has been received
     * 
     * @param message
     */
    public void messageFromSpace(AALSpaceMessage message, PeerCard sender);

    /**
     * This method allows to add a new Peer to the AALSpace.
     * 
     * @param spaceDescriptor
     *            All the information regarding the AALSpace for the new Peer
     * @param peer
     *            The Peer to add to the AALSpace
     */
    public void addPeer(AALSpaceDescriptor aalSpaceDescriptor, PeerCard peer);

    /**
     * This method propagates the event of new PeerAdded to the whole AALSpace
     * 
     * @param spaceCard
     *            The AALSpace where to propagate the event
     * @param peerCard
     *            The new peer added
     */
    public void announceNewPeer(AALSpaceCard aalSpaceCard, PeerCard peerCard);

    /**
     * This method configures the channel used in order to send/receive AAL
     * Space messages
     
     */
    public void configureAALSpaceChannel();

    /**
     * This method fetches the list of peer address joining to the same AAL
     * Space
     * 
     * @param peers
     * @return The consolidated list of peers
     */
    public List<String> getPeersAddress();

}
