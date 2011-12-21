/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.acl;

/**
 * Components willing to be notified whenever a new remote instance of
 * SodaPopPeer is discovered must implement this interface and add themselves
 * to a P2PConnector as such a listener.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public interface PeerDiscoveryListener {
	/**
	 * The method called by a P2PConnector whenever a new remote instance of
	 * SodaPopPeer is discovered.
	 * 
	 * @param peer the actual handle of the discovered remote SodaPopPeer
	 * @param discoveryProtocol the name of the underlying technology
	 *                          used by the corresponding P2PConnector
	 *                          that discovered this peer. It can be useful
	 *                          for implementing protocol bridging.
	 */
	public void noticeNewPeer(SodaPopPeer peer, String discoveryProtocol);
	
	/**
	 * The method called by a P2PConnector when a previously found peer
	 * gets lost.
	 * 
	 * @param peerID The ID of the lost SodaPopPeer.
	 * @param discoveryProtocol the name of the underlying technology
	 *                          used by the corresponding P2PConnector
	 *                          that discovered this peer. It can be useful
	 *                          for implementing protocol bridging.
	 */
	public void noticeLostPeer(String peerID, String discoveryProtocol);
}
