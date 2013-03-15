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
package org.universAAL.middleware.interfaces;

import java.util.UUID;

/**
 * This class identifies a Peer in the AAL space
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class PeerCard {

    private String peerID;
    private PeerRole role;
    private String PLATFORM_UNIT;
    private String CONTAINER_UNIT;
    private String OS = System.getProperty("os.name") + " - "
	    + System.getProperty("os.version") + "- "
	    + System.getProperty("os.arch");

    /**
     * Instantiate a PeerCard and generated the peer unique ID
     * 
     * @param role
     */
    public PeerCard(PeerRole role, String containerUnit, String platformUnit) {
	this.peerID = UUID.randomUUID().toString();
	this.role = role;
	this.CONTAINER_UNIT = containerUnit;
	this.PLATFORM_UNIT = platformUnit;
    }

    public PeerCard(String ID, PeerRole role) {
	this.peerID = ID;
	this.role = role;
    }

    public PeerCard(String strSerialization) {
	int i = strSerialization.indexOf(" - Peer Role: ");
	if (!strSerialization.startsWith("Peer ID: ") || i < 10)
	    throw new RuntimeException(
		    "Cannot create the PeerCard by deserializing the given string");

	this.peerID = strSerialization.substring(9, i);
	this.role = PeerRole.valueOf(strSerialization.substring(i + 14));
    }

    /**
     * Return the peer unique ID
     * 
     * @return String representing the ID
     */
    public String getPeerID() {
	return peerID;
    }

    public PeerRole getRole() {
	return role;
    }

    public void setRole(PeerRole role) {
	this.role = role;
    }

    /**
     * 
     * @return true if the Peer owns the Coordinator role, false otherwise
     */
    public boolean isCoordinator() {
	if (role.equals(PeerRole.COORDINATOR))
	    return true;
	return false;
    }

    public String toString() {
	return "Peer ID: " + peerID + " - Peer Role: " + role.toString();
    }

    public String getPLATFORM_UNIT() {
	return PLATFORM_UNIT;
    }

    public void setPLATFORM_UNIT(String pLATFORM_UNIT) {
	PLATFORM_UNIT = pLATFORM_UNIT;
    }

    public String getCONTAINER_UNIT() {
	return CONTAINER_UNIT;
    }

    public void setCONTAINER_UNIT(String cONTAINER_UNIT) {
	CONTAINER_UNIT = cONTAINER_UNIT;
    }

    public String getOS() {
	return OS;
    }

    public void setOS(String oS) {
	OS = oS;
    }

}
