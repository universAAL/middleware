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

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

/**
 * This class is a test unit for {@link PeerCard}
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 */
public class PeerCardTest {

    @Test
    public void testSetRole() {
	PeerCard peer = new PeerCard(PeerRole.COORDINATOR, "Karaf", "Java");
	try {
	    peer.setRole(null);
	    fail("Expected NullPointerException because trying to assign null value");
	} catch (Exception ex) {
	    assertTrue("Exception thrown but is not the right one",
		    ex instanceof NullPointerException);
	}
    }

    @Test
    public void testEqualsObject() {
	final String uuid = UUID.randomUUID().toString();
	PeerCard peerA = new PeerCard(uuid, PeerRole.COORDINATOR);
	PeerCard peerB = new PeerCard(uuid, PeerRole.COORDINATOR);
	PeerCard peerC = new PeerCard(uuid, PeerRole.PEER);
	assertTrue("equals for Peer should match only uuid",
		peerA.equals(peerB));
	assertTrue("equals for Peer should match only uuid",
		peerA.equals(peerC));
    }

    @Test
    public void testHashCode() {
	final String uuid = UUID.randomUUID().toString();
	PeerCard peerA = new PeerCard(uuid, PeerRole.COORDINATOR);
	PeerCard peerB = new PeerCard(uuid, PeerRole.COORDINATOR);
	PeerCard peerC = new PeerCard(uuid, PeerRole.PEER);
	assertEquals("equals for Peer should have same hashCode",
		peerA.hashCode(), peerB.hashCode());
	assertEquals("equals for Peer should have same hashCode",
		peerA.hashCode(), peerC.hashCode());
    }

    public void testPeerCard() {
	try {
	    PeerCard peerA = new PeerCard("ciao", PeerRole.COORDINATOR);
	    fail("Expected IllegalArgumentException because using a invalid UUID string");
	} catch (Exception ex) {
	    assertTrue("Exception thrown but is not the right one",
		    ex instanceof IllegalArgumentException);
	}
    }

    @Test
    public void testToURI() {
	final String uuid = UUID.randomUUID().toString();
	PeerCard peerA = new PeerCard(uuid, PeerRole.COORDINATOR);
	assertEquals("URI of PeerId is wrong", "urn:uuid:" + uuid, peerA
		.toURI().toString());
    }

}
