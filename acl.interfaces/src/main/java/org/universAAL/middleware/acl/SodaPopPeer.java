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
 * The interface to be implemented by the SodaPop engine if it wants to
 * communicate with its remote instances.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public interface SodaPopPeer {
	/**
	 * Returns the globally unique ID of this instance of the SodaPop engine.
	 */
	public String getID();
	
	/**
	 * Whenever a new Bus is created on the side of a SodaPopPeer, it 
	 * must inform its remote peers of this fact by calling this method.
	 * 
	 * @param busName The name of the newly created {@link
	 *                de.fhg.igd.ima.sodapop.Bus Bus}.
	 * @param joiningPeer The globally unique ID of the calling instance.
	 */
	public void joinBus(String busName, String joiningPeer);
	
	/**
	 * As soon as the last {@link de.fhg.igd.ima.sodapop.BusMember BusMember}
	 * unregisters from a Bus, The hosting SodaPopPeer
	 * must inform its remote peers of this fact by calling this method.
	 * 
	 * @param busName The name of the {@link
	 *                de.fhg.igd.ima.sodapop.Bus Bus} that has no
	 *                local members any more.
	 * @param leavingPeer The globally unique ID of the calling instance.
	 */
	public void leaveBus(String busName, String leavingPeer);
	
	/**
	 * The method to be used for informing a newly discovered remote peer
	 * about the set of busses on the side of the calling SodaPopPeer.
	 * 
	 * @param peerID The globally unique ID of the caller
	 * @param busNames a comma-separated String of the names of the buses.
	 */
	public void noticePeerBusses(String peerID, String busNames);
	public void replyPeerBusses(String peerID, String busNames);
	
	/**
	 * If a SodaPopPeer has a message on a bus that must be sent to the
	 * same bus on the side of a remote peer, it calls this method on
	 * the handle of the corresponding remote peer.
	 * 
	 * @param busName the name of the bus
	 * @param msg the message to be processed
	 */
	public void processBusMessage(String busName, String msg);
	
	public void printStatus();

}
