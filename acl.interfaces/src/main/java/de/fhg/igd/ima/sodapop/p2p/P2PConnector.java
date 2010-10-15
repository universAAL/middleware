/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
	Copyright 2008-2010 CNR-ISTI, http://isti.cnr.it
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
package de.fhg.igd.ima.sodapop.p2p;

/**
 * Classes implementing this interface make P2P connection between instances
 * of {@link de.fhg.igd.ima.sodapop.p2p.SodaPopPeer SodaPopPeer} using a specific
 * underlying technology like "UPnP" and "R-OSGi", which is called here
 * the protocol.
 * 
 * @author mtazari
 *
 */
public interface P2PConnector {
	/**
	 * Adds a {@link de.fhg.igd.ima.sodapop.p2p.PeerDiscoveryListener 
	 * PeerDiscoveryListener} willing to be notified whenever a new remote
	 * instance of SodaPopPeer is discovered.
	 * 
	 * @param listener the listener to be added.
	 */
	public void addPeerDiscoveryListener(PeerDiscoveryListener listener);
	
	/**
	 * Returns the ID of the underlying technology for the implementation of
	 * this P2PConnector (e.g. "UPnP" or "R-OSGi"), which is called here the
	 * protocol.
	 */
	public String getProtocol();
	
	/**
	 * A component bridging between this P2PConnector and another P2PConnector
	 * that uses another "protocol" may call this method in order to notify
	 * this P2PConnector that a peer previously found by the other protocol
	 * is now lost.
	 * 
	 * @param peerID The ID of the lost SodaPopPeer.
	 */
	public void noticeLostBridgedPeer(String peerID);
	
	/**
	 * A component bridging between this P2PConnector and another P2PConnector
	 * that uses another "protocol" may call this method in order to notify
	 * this P2PConnector that a new peer was found by the other protocol so
	 * that this P2PConnector can relay it to its own network.
	 * 
	 * @param newPeer The new peer.
	 */
	public void noticeNewBridgedPeer(SodaPopPeer newPeer);
	
	/**
	 * The single local instance of SodaPopPeer that wants to be accessible
	 * to its remote peers uses this method to register to this P2PConnector.
	 * It must allow only for one registration.
	 * 
	 * @param localInstance the single local instance of SodaPopPeer
	 */
	public void register(SodaPopPeer localInstance);
}
