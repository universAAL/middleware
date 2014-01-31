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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * This class identifies a Peer in the AAL space
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 */
public class PeerCard {

    final String URI_PREFIX = "urn:uuid:";

    private String peerID;
    private PeerRole role;
    private String platform;
    private String container;
    private String os = System.getProperty("os.name") + " - "
            + System.getProperty("os.version") + "- "
            + System.getProperty("os.arch");

    private URI uri = null;

    /**
     * Instantiate a PeerCard and generated the peer unique ID
     *
     * @param role
     */
    public PeerCard(PeerRole role, String containerUnit, String platformUnit) {
        this.peerID = UUID.randomUUID().toString();
        this.role = role;
        this.container = containerUnit;
        this.platform = platformUnit;
    }

    public PeerCard(String ID, PeerRole role) {
        this.peerID = UUID.fromString(ID).toString();
        this.role = role;
    }

    /**
     *
     * @param strSerialization
     * @deprecated
     */
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
        if (role == null)
            throw new NullPointerException("Cannot assign null as Role");
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
        return "Peer ID: " + peerID + " - Peer Role: " + role;
    }

    public int hashCode() {
        return peerID.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof PeerCard) {
            PeerCard peer = (PeerCard) o;
            return peer.peerID.equals(peerID);
        }
        return false;
    }

    public String getPLATFORM_UNIT() {
        return platform;
    }

    public void setPLATFORM_UNIT(String pLATFORM_UNIT) {
        platform = pLATFORM_UNIT;
    }

    public String getCONTAINER_UNIT() {
        return container;
    }

    public void setCONTAINER_UNIT(String cONTAINER_UNIT) {
        container = cONTAINER_UNIT;
    }

    public String getOS() {
        return os;
    }

    public void setOS(String oS) {
        os = oS;
    }

    /**
     *
     * @return a {@link URI} representing the PeerCard, that is actually based on the PeerId
     * @since 2.0.3
     */
    public URI toURI() {
        synchronized (this) {
            if (uri != null) {
                return uri;
            }
            try {
                uri = new URI(URI_PREFIX + peerID);
            } catch (URISyntaxException ex) {
                System.err.println("Failed to generate URI for PeerCard, due to exception");
                ex.printStackTrace(System.err);
            }
            return uri;
        }
    }
}
