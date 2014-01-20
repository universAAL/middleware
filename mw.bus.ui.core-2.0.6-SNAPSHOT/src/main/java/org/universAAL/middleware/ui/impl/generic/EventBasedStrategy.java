/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.ui.impl.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * A set of methods to send messages. Main implementation for
 * {@link EventMessage} handling. <br>
 * 
 * <center> <img style="background-color:lightgray;"
 * src="doc-files/EventBasedStrategy.png" alt="UIStrategy messages"
 * width="70%"/> </center>
 * 
 * When a message who's content implements the {@link EventMessage} Interface
 * then the
 * {@link EventMessage#onReceived(EventBasedStrategy, BusMessage, String)
 * method} is called. For this the Message content has to be deserialized into a
 * {@link Resource} that implements such method, thus an ontology is needed.
 * <br>
 * Overall when an event it sent, The {@link BusMessage} content 
 * has to be a {@link EventMessage}.
 * <center> <img style="background-color:lightgray;"
 * src="doc-files/SDSend.png" alt="UIStrategy messages"
 * width="70%"/> </center>
 * <br>
 * So when received it is deserialized and the instance's 
 * {@link EventMessage#onReceived(EventBasedStrategy, BusMessage, String) 
 * callback} is called, being of the type of the sent message then the correct
 * operation is perfomed
 * <center> <img style="background-color:lightgray;"
 * src="doc-files/SDReceive.png" alt="UIStrategy messages"
 * width="70%"/> </center>
 * <br>
 * <center> <img style="background-color:lightgray;"
 * src="doc-files/EventBasedStrategy-Sample.png" alt="UIStrategy messages"
 * width="70%"/> </center>
 * @author amedrano
 * 
 */
public abstract class EventBasedStrategy extends BusStrategy {

    /**
     * @param commModule
     */
    public EventBasedStrategy(CommunicationModule commModule) {
	super(commModule);
    }

    /**
     * @param commModule
     * @param name
     */
    public EventBasedStrategy(CommunicationModule commModule, String name) {
	super(commModule, name);
    }

    /** {@ inheritDoc} */
    protected void handle(BusMessage m, String senderID) {
	Object o = m.getContent();
	if (o instanceof EventMessage) {
	    ((EventMessage) o).onReceived(this, m, senderID);
	    return;
	}
    }

    /**
     * Helper method to send a Unicast Event.
     * 
     * @param memberID
     * @param content
     */
    protected final void sendEventToRemoteBusMember(String memberID,
	    EventMessage content) {
	sendEventToRemoteBusMember(
		AbstractBus.getPeerFromBusResourceURI(memberID), content);
    }

    /**
     * Helper method to send a Multicast Event.
     * 
     * @param memberID
     * @param content
     */
    protected final void sendEventToRemoteBusMember(String[] membersID,
	    EventMessage content) {
	((UIBusImpl) bus).assessContentSerialization((Resource) content);
	BusMessage m = new BusMessage(MessageType.p2p_event, content, bus);
	List<PeerCard> receivers = new ArrayList<PeerCard>(membersID.length);
	for (int i = 0; i < membersID.length; i++) {
	    receivers.add(AbstractBus.getPeerFromBusResourceURI(membersID[i]));
	}
	m.setReceivers(receivers);
	send(m);
    }

    /**
     * Helper method to send a Broadcast Event.
     * 
     * @param memberID
     * @param content
     */
    protected final void sendEventToRemoteBusMember(EventMessage content) {
	((UIBusImpl) bus).assessContentSerialization((Resource) content);
	BusMessage m = new BusMessage(MessageType.p2p_event, content, bus);
	send(m);
    }

    /**
     * Helper method to send a Unicast Event.
     * 
     * @param member
     * @param content
     */
    protected final void sendEventToRemoteBusMember(PeerCard member,
	    EventMessage content) {
	((UIBusImpl) bus).assessContentSerialization((Resource) content);
	BusMessage m = new BusMessage(MessageType.p2p_event, content, bus);
	m.setReceiver(member);
	send(m);
    }

    /**
     * Helper method to send a Multicast Event.
     * 
     * @param memberID
     * @param content
     */
    protected final void sendEventToRemoteBusMember(PeerCard[] members,
	    EventMessage content) {
	((UIBusImpl) bus).assessContentSerialization((Resource) content);
	BusMessage m = new BusMessage(MessageType.p2p_event, content, bus);
	List<PeerCard> receivers = new ArrayList<PeerCard>(
		Arrays.asList(members));
	m.setReceivers(receivers);
	send(m);
    }
}
