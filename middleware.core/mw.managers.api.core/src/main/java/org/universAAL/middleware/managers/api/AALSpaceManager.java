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
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;

/**
 * AALSpace manager interface. These methods are managing an AALSpace
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public interface AALSpaceManager extends Manager {

    /**
     * This method returns the PeerCard of the current MW instance
     * 
     * @return PeerCard
     */
    public PeerCard getMyPeerCard();

    /**
     * This method returns the list of AALSpace discovered
     * 
     * @return Set of AALSpace found
     */
    public Set<AALSpaceCard> getAALSpaces();

    /**
     * This method returns the AALSpaceDescriptor of the AALSpace where the MW
     * belongs to or null if the mw instance does not join to any AAL Space.
     * 
     * @return AALSpaceDescriptor
     */
    public AALSpaceDescriptor getAALSpaceDescriptor();

    /**
     * This method return a map of AALSpace managed by this MW instance
     * 
     * @return
     */
    public Map<String, AALSpaceDescriptor> getManagedAALSpaces();

    /**
     * method to join an existing AALSpace according the configuration file: -to
     * configure the peering channel -to send a join request -to receive the
     * join response -to get the AALSpaceDescriptor -to configure the
     * communication channels
     * 
     * @param space
     *            AAL Space to join
     */
    public void join(AALSpaceCard spaceCard);

    /**
     * Method used to leave an AALSpace
     * 
     * @param spaceDescriptor
     */
    public void leaveAALSpace(AALSpaceDescriptor spaceDescriptor);

    /**
     * This method return the list of discovered peers joinig the current
     * AALSpace
     * 
     * @return Map of peers: peerID, PeerCard
     */
    public Map<String, PeerCard> getPeers();

    /**
     * Add a new AAL Space listener.
     * 
     * @param listener
     */
    public void addAALSpaceListener(AALSpaceListener listener);

    /**
     * Remove an AAL Space Listener
     * 
     * @param listener
     */
    public void removeAALSpaceListener(AALSpaceListener listener);

}
