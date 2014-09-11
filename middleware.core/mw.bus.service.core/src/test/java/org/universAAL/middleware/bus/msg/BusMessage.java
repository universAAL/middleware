/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

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
package org.universAAL.middleware.bus.msg;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;

/**
 * Special implementation that should replace the one from mw.bus.model for unit
 * tests, especially for the distributed unit tests in mw.bus.service.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public class BusMessage implements BrokerMessage {
    /**
     * 
     */
    private static long counter = 0;
    private static MessageContentSerializer contentSerializer = null;
    private static PeerCard thisPeer = null;
    private PeerCard sender;
    private List<PeerCard> receiver = new ArrayList<PeerCard>();
    public String brokerName;
    
    // used only for special case of mw.bus.service unit tests; in this case,
    // thisPeer is null
    // we have to distingush these two cases because the module mw.bus.junit
    // also uses this class (why?)
    private PeerCard thisPeer2 = null;

    /**
     * generates a globally unique ID, based on a counter and some string that
     * represents this JVM
     * 
     * @return String - the created unique ID
     */
    public static String createUniqueID() {
	return UUID.randomUUID().toString();
    }

    private static synchronized String getLocalID() {
	return Long.toHexString(counter++);
    }

    public static void setMessageContentSerializer(MessageContentSerializer mcs) {
	if (contentSerializer == null && mcs != null) {
	    contentSerializer = mcs;
	}
    }

    public static void setThisPeer(PeerCard peer) {
	thisPeer = peer;
    }
    
    private PeerCard getThisPeer() {
	if (thisPeer != null)
	    return thisPeer;
	return thisPeer2;
    }

    public void setReceivers(List<PeerCard> receivers) {
	receiver = receivers;
    }

    /**
     * A safe wrapping for calling contentSerializer.serialize(o).
     * 
     * @param o
     *            - Object to serialize
     * @return String 1. the result of o.toString() if the serializer is null;
     *         2. otherwise the result of calling the serialize() method of the
     *         serializer, which might be null if the serializer fails to
     *         serialize the given object
     */
    public static String trySerializationAsContent(Object o) {
	return (contentSerializer == null) ? o.toString() : contentSerializer
		.serialize(o);
    }

    public static Object deserializeAsContent(String s) {
	return (contentSerializer == null) ? null : contentSerializer
		.deserialize(s);
    }

    private Object content;
    private String contentStr = null;
    private String id;
    private String inReplyTo = null;
    private MessageType type;
    private BrokerMessageTypes mType;

    public BusMessage() {
	mType = BrokerMessageTypes.BusMessage;
    }
    
    /**
     * Constructor - a message of particular type with particular content
     * 
     * @param type
     * @param content
     *            Content of the message, typically a {@link Resource}.
     */
    public BusMessage(MessageType type, Object content, AbstractBus creator) {
	if (type == null || content == null || type == MessageType.p2p_reply
		|| type == MessageType.reply || creator == null) {
	    throw new IllegalArgumentException();
	}

	brokerName = creator.getBrokerName();
	this.content = content;
	this.type = type;
	if (thisPeer != null)
	    sender = thisPeer;
	else {
	    thisPeer2 = creator.getPeerCard();
	    sender = thisPeer2;
	    if (thisPeer2 == null)
		System.out.println("ERROR: thisPeer2 is null");
	}
	id = getLocalID();
	mType = BrokerMessageTypes.BusMessage;
    }

    /**
     * Constructor - parses the string passed as a parameter and creates a
     * Message object.
     * 
     * @param message
     *            the string to parse (the serialization of a message object).
     */
    public BusMessage(String message, AbstractBus creator) {
	if (message == null) {
	    throw new NullPointerException();
	}
	
	thisPeer2 = creator.getPeerCard();

	if (message.startsWith("<![CDATA[") && message.endsWith("]]>")) {
	    message = message.substring(9, message.length() - 3);
	}

	if (!message.startsWith("<uAAL:BusMessage>\n  <uAAL:BusMessage#id>")) {
	    throw new IllegalArgumentException();
	}

	int i = message
		.indexOf("</uAAL:BusMessage#id>\n  <uAAL:BusMessage#type>");
	if (i < 0) {
	    throw new IllegalArgumentException();
	}
	id = message.substring(40, i);

	i += 46;
	int j = message.indexOf(
		"</uAAL:BusMessage#type>\n  <uAAL:BusMessage#content>\n", i);
	if (j < 0) {
	    throw new IllegalArgumentException();
	}
	type = MessageType.valueOf(message.substring(i, j));

	j += 52;
	i = message.indexOf("\n    </uAAL:BusMessage#content>", j);
	if (i < 0) {
	    throw new IllegalArgumentException();
	}

	// String aux = msg.substring(j, i);
	// if (aux.startsWith("<![CDATA[") && aux.endsWith("]]>"))
	// aux = aux.substring(9, i-j-3);
	// content = getContentSerializer().deserialize(aux);

	contentStr = message.substring(j, i);
	content = contentSerializer.deserialize(contentStr);
	if (content == null) {
	    throw new RuntimeException("Message content parsing failed!");
	}

	message = message.substring(i + 31);
	if (message.startsWith("\n  <uAAL:BusMessage#inReplyTo>")) {
	    i = message.indexOf("</uAAL:BusMessage#inReplyTo>");
	    if (i < 0) {
		throw new IllegalArgumentException();
	    }
	    inReplyTo = message.substring(30, i);
	    message = message.substring(i + 28);
	}

	if (!message.startsWith("\n  <uAAL:BusMessage#sender>")
		|| (i = message.indexOf("</uAAL:BusMessage#sender>")) < 0) {
	    throw new RuntimeException("Message sender not found!");
	}
	sender = new PeerCard(message.substring(27, i).trim());

	message = message.substring(i + 25);
	if (message.startsWith("\n  <uAAL:BusMessage#receiver>")) {
	    receiver = new ArrayList<PeerCard>();
	    message = message.substring(29);
	    while ((i = message.indexOf("[],")) > 0) {
		receiver.add(new PeerCard(message.substring(0, i)));
		message = message.substring(i + 3);
	    }
	    i = message.indexOf("</uAAL:BusMessage#receiver>");
	    if (i < 0) {
		throw new IllegalArgumentException();
	    }
	    receiver.add(new PeerCard(message.substring(0, i)));
	    message = message.substring(i + 27);
	}

	if (!message.startsWith("\n  <uAAL:BusMessage#brokerName>")
		|| (i = message.indexOf("</uAAL:BusMessage#brokerName>")) < 0) {
	    throw new RuntimeException("Bus name not found!");
	}
	brokerName = message.substring(31, i);

	if (!message.substring(i + 29).equals("\n</uAAL:BusMessage>")) {
	    throw new IllegalArgumentException();
	}
    }

    public void addReceiver(PeerCard pc) {
	if (pc == null) {
	    return;
	}
	if (receiver == null) {
	    receiver = new ArrayList<PeerCard>();
	}
	receiver.add(pc);
    }

    public void addReceivers(PeerCard[] receivers) {
	if (receivers == null || receivers.length == 0) {
	    return;
	}
	if (receiver == null) {
	    receiver = new ArrayList<PeerCard>();
	}
	for (PeerCard pc : receivers) {
	    receiver.add(pc);
	}
    }

    /**
     * Create reply message to this message, with the content passed as a
     * parameter.
     * 
     * @param content
     *            the content of the created reply, typically a {@link Resource}
     *            .
     * @return Message the reply message.
     */
    public BusMessage createReply(Object content) {
	if (content == null) {
	    return null;
	}

	BusMessage reply = new BusMessage();
	if (type == MessageType.request) {
	    reply.type = MessageType.reply;
	} else if (type == MessageType.p2p_request) {
	    reply.type = MessageType.p2p_reply;
	} else {
	    return null;
	}
	reply.id = getLocalID();
	reply.content = content;
	reply.brokerName = brokerName;
	if (thisPeer != null)
	    reply.sender = thisPeer;
	else {
	    reply.sender = thisPeer2;
	    reply.thisPeer2 = thisPeer2;
	}

	reply.inReplyTo = id;
	reply.receiver = new ArrayList<PeerCard>(1);
	reply.receiver.add(sender);

	return reply;
    }

    /**
     * Create a reply message to the message with the given ID, with the content
     * and receiver passed as a parameter.
     * 
     * @param messageIDInReplyTo
     *            ID of the message to which the returned message is a reply to.
     * @param receiver
     *            the receiving peer.
     * @param content
     *            Content of the message, typically a {@link Resource}.
     * @return a new {@link BusMessage}, or null if the parameters are invalid.
     */
    public static BusMessage createP2PReply(String messageIDInReplyTo,
	    PeerCard receiver, Object content, AbstractBus creator) {
	if (content == null || messageIDInReplyTo == null || receiver == null
		|| creator == null) {
	    return null;
	}

	BusMessage reply = new BusMessage();
	reply.id = getLocalID();
	reply.content = content;
	reply.brokerName = creator.getBrokerName();
	if (thisPeer != null)
	    reply.sender = thisPeer;
	else
	    reply.sender = creator.getPeerCard();

	reply.receiver = new ArrayList<PeerCard>(1);
	reply.receiver.add(receiver);

	reply.type = MessageType.p2p_reply;
	reply.inReplyTo = messageIDInReplyTo;

	return reply;
    }

    /**
     * 
     * @return Object the content of the message.
     */
    public Object getContent() {
	return content;
    }

    /**
     * 
     * @return String the string serialization of the content.
     */
    public String getContentAsString() {
	if (contentStr == null && contentSerializer != null) {
	    contentStr = contentSerializer.serialize(content);
	}
	return contentStr;
    }

    /**
     * 
     * @return the unique ID of the message.
     */

    public String getID() {
	return id;
    }

    public long getIDAsLong() {
	return Long.parseLong(id, 16);
    }

    /**
     * Returns the ID of the message this message replies to.
     * 
     * @return String the ID of the message this message replies to.
     */
    public String getInReplyTo() {
	return inReplyTo;
    }

    /**
     * 
     * @return {@link MessageType}
     */
    public MessageType getType() {
	return type;
    }
    
    public void setType(MessageType newType) {
	type = newType;
    }

    public boolean receiverResidesOnDifferentPeer() {
	if (receiver == null || receiver.isEmpty()) {
	    // a broadcast message is assumed to have remote receivers
	    return true;
	}
	for (PeerCard pc : receiver) {
	    try {
	    if (!pc.getPeerID().equals(getThisPeer().getPeerID())) {
		return true;
	    }
	    } catch (Exception e) {
		System.out.println("");
	    }
	}
	return false;
    }

    public boolean senderResidesOnDifferentPeer() {
	return sender != getThisPeer();
    }

    public boolean sentBySamePeerAs(PeerCard peer) {
	return peer != null
		&& sender != null
		&& peer.getPeerID() != null
		&& peer.getPeerID().equals(sender.getPeerID())
		&& (peer.getRole() == sender.getRole()
			|| peer.getRole() == null || sender.getRole() == null);
    }

    /**
     * If the given peer is not null, removes any existing receiver and adds the
     * given peer as the only receiver.
     * 
     * @param receiver
     */
    public void setReceiver(PeerCard receiver) {
	if (receiver != null) {
	    this.receiver = new ArrayList<PeerCard>(1);
	    this.receiver.add(receiver);
	}
    }

    /**
     * Serialize the message as string.
     * 
     * @return String the serialized message.
     */
    @Override
    public String toString() {
	if (sender == null)
	    System.out.println("sender null");;
	StringBuffer sb = new StringBuffer();
	// sb.append("<![CDATA[<uAAL:BusMessage>");
	sb.append("<uAAL:BusMessage>");
	sb.append("\n  <uAAL:BusMessage#id>").append(id).append(
		"</uAAL:BusMessage#id>");
	sb.append("\n  <uAAL:BusMessage#type>").append(type.name()).append(
		"</uAAL:BusMessage#type>");
	sb.append("\n  <uAAL:BusMessage#content>\n").append(
		getContentAsString())
		.append("\n    </uAAL:BusMessage#content>");
	if (inReplyTo != null) {
	    sb.append("\n  <uAAL:BusMessage#inReplyTo>").append(inReplyTo)
		    .append("</uAAL:BusMessage#inReplyTo>");
	}
	sb.append("\n  <uAAL:BusMessage#sender>\n").append(sender).append(
		"\n    </uAAL:BusMessage#sender>");
	if (receiver != null && !receiver.isEmpty()) {
	    sb.append("\n  <uAAL:BusMessage#receiver>").append(receiver.get(0));
	    for (int i = 1; i < receiver.size(); i++) {
		sb.append("[],").append(receiver.get(i));
	    }
	    sb.append("</uAAL:BusMessage#receiver>");
	}
	sb.append("\n  <uAAL:BusMessage#brokerName>\n").append(brokerName)
		.append("\n    </uAAL:BusMessage#brokerName>");
	// sb.append("\n</uAAL:BusMessage>]]>");
	sb.append("\n</uAAL:BusMessage>");
	return sb.toString();
    }

    public String marshall(BrokerMessage message) {
	if (message instanceof BusMessage) {
	    return message.toString();
	} else {
	    return null;
	}

    }

    public BrokerMessageTypes getMType() {
	return mType;
    }

    public PeerCard[] getReceivers() {
	return receiver.toArray(new PeerCard[receiver.size()]);
    }

    public boolean hasReceiver(PeerCard receiver) {
	for (PeerCard current : getReceivers()) {
	    if (receiver.equals(current)) {
		return true;
	    }
	}
	return false;
    }

    public PeerCard getSender() {
	return sender;
    }

    public void setSender(PeerCard sender) {
	this.sender = sender;
    }
}
