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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;

/**
 * AALSpace manager interface. These methods are managing an AALSpace
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 */
public interface AALSpaceManager extends Manager {

    public static final String COMUNICATION_TIMEOUT_KEY = "uAAL.synchronous.timeout";
    public static final String COMUNICATION_TIMEOUT_VALUE = "10000";

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
     * This method return the list of neighborhood peers joined to the current
     * AALSpace.<br />
     * <b>NOTE:</b> The list does not contains the invoker
     *
     * @return Map of peers: peerID, PeerCard
     */
    public Map<String, PeerCard> getPeers();

    /**
     *
     * A method for identifying a possible set of Peer in the AAL space that can
     * be used for installing a part of the universAAL application.<br>
     * The matching algorithm returns only the Peer of the space that match the
     * following rules
     * <ul>
     * <li>
     * For each key of the {@link Map} with <code>null</code> value, an
     * attribute with any value must be available on the matching Peer</li>
     * <li>
     * For each key of the {@link Map} with a not <code>null</code> value, an
     * attribute with same value must be available on the matching Peer. The
     * {@link #equals(Object)} is used for checking if the value are the same</li>
     * Please consider the following example:<br>
     * <ul>
     * <li>The AAL space contains four peer: A, B, C, D, and E</li>
     * <li>The attributes of the peer A are { container=Foo, tool=OSGi,
     * version=3.0 }</li>
     * <li>The attributes of the peer B are { container=Karaf, tool=Bar,
     * version=1.0 }</li>
     * <li>The attributes of the peer C are { container=Karaf, tool=OSGi,
     * version=3.0 }</li>
     * <li>The attributes of the peer D are { container=Karaf, tool=OSGi }</li>
     * <li>The attributes of the peer E are { container=Karaf, tool=OSGi,
     * version=2.5 }</li>
     * <li>The {@link Map} filter is set to { container=Karaf, tool=OSGI,
     * version=null }</li>
     * The {@link MatchingResult} will contain the peers C and E, while the
     * other were discarded because
     * <ul>
     * <li>peer A is skipped because container=Foo, but container=Karaf was
     * searched</li>
     * <li>peer B is skipped because tool=Bar, but tool=OSGi was searched</li>
     * <li>peer D is skipped because the attribute version is missing</li>
     *
     * @param filter
     *            a {@link Map} that contains a pair of {@link String} as Key
     *            and {@link Object} as value which will be used for looking for
     *            matching among the one available on the AAL space
     * @return {@link MatchingResult} representing the filtering result
     * @since 1.3.2
     */
    public MatchingResult getMatchingPeers(Map<String, Serializable> filter);

    /**
     *
     * @param attributes
     *            The list of attribute to get value,
     * @param target
     *            {@link PeerCard} the peer card that identifies the peer that
     *            we want to read attribute from
     * @return a {@link Map} with the pair {@link String}, {@link Serializable}
     *         representing the attribute requested along with its value.
     */
    public Map<String, Serializable> getPeerAttributes(List<String> attributes,
            PeerCard target);

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
