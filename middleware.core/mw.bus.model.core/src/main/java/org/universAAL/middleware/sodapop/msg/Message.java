/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
package org.universAAL.middleware.sodapop.msg;

import org.universAAL.middleware.sodapop.impl.SodaPopImpl;

/**
 * This class represents messages sent between SodaPop bus members
 * Each message has a unique ID, a list of receivers, the content (the actual 
 * payload), inReplyTo field to correlate with the message its message is a 
 * reply to
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class Message {
    private static long counter = 0;
    private static int idLength;
    public static final String thisJVM;
    static {
    	thisJVM = PeerIDGenerator.generatePeerID();
    	idLength = thisJVM.length() + 17;
    }
    
    /**
     * generates a globally unique ID, based on a counter and some string that 
     * represents this JVM
     * 
     * @return String - the created unique ID
     */
    public static synchronized String createUniqueID() {
	StringBuffer b = new StringBuffer(idLength);
	b.append(fill0(Long.toHexString(counter++), 16));
	b.append(':').append(thisJVM);
	return b.toString();
    }
    
    /**
     * generates a locally unique ID
     * 
     * @param localPrefix
     * @param counter
     * @param counterLen
     * @return String - the created unique local ID
     */
    public static synchronized String createUniqueLocalID(String localPrefix,
	    int counter, int counterLen) {
	return localPrefix + fill0(Long.toHexString(counter), counterLen);
    }
    
    /**
     * Padd the string passed as the first parameter with zeros up to the length
     * passed as the second parameter 
     * 
     * @param arg - the string to padd
     * @param len - the required length of the padded result
     * @return the padded string
     */
    private static String fill0(String arg, int len) {
	int diff = len - arg.length();
	if (diff == 0)
	    return arg;
	if (diff < 0)
	    return arg.substring(diff);
	StringBuffer b = new StringBuffer(len);
	while (diff > 0) {
	    b.append("0");
	    diff--;
	}
	b.append(arg);
	return b.toString();
    }
    
    /**
     * serialize the object passed as a parameter
     * 
     * @param o - Object to serialize
     * @return String - the serialization
     */
    public static String trySerializationAsContent(Object o) {
	MessageContentSerializer s = SodaPopImpl.getContentSerializer();
	return (s == null) ? o.toString() : s.serialize(o);
    }

    private Object content;
    private String contentStr = null, id, inReplyTo = null;
    private String[] receivers = null;

    private MessageType type;

    private Message() {

    }
    
    /**
     * Constructor - a message of particular type with particular content
     * 
     * @param type
     * @param content
     */
    public Message(MessageType type, Object content) {
	if (type == null || content == null || type == MessageType.p2p_reply
		|| type == MessageType.reply)
	    throw new IllegalArgumentException();

	this.content = content;
	this.type = type;
	id = createUniqueID();
    }

    /**
     * Constructor - parses the string passed as a parameter and creates
     *  a Message object  
     * 
     * @param msg - the string to parse (the serialization of a message object)
     */
    public Message(String msg) {
	if (msg == null)
	    throw new NullPointerException();

	if (msg.startsWith("<![CDATA[") && msg.endsWith("]]>"))
	    msg = msg.substring(9, msg.length() - 3);

	if (!msg.startsWith("<sodapop:Message>\n  <sodapop:id>"))
	    throw new IllegalArgumentException();

	int i = msg.indexOf("</sodapop:id>\n  <sodapop:type>");
	if (i < 0)
	    throw new IllegalArgumentException();
	id = msg.substring(32, i);

	i += 30;
	int j = msg.indexOf("</sodapop:type>\n  <sodapop:content>\n", i);
	if (j < 0)
	    throw new IllegalArgumentException();
	type = MessageType.valueOf(msg.substring(i, j));

	j += 36;
	i = msg.indexOf("\n    </sodapop:content>", j);
	if (i < 0)
	    throw new IllegalArgumentException();

	// String aux = msg.substring(j, i);
	// if (aux.startsWith("<![CDATA[") && aux.endsWith("]]>"))
	// aux = aux.substring(9, i-j-3);
	// content = getContentSerializer().deserialize(aux);

	contentStr = msg.substring(j, i);
	content = SodaPopImpl.getContentSerializer().deserialize(contentStr);
	if (content == null)
	    throw new RuntimeException("Message content parsing failed!");

	msg = msg.substring(i + 23);
	if (msg.length() > 18 && msg.startsWith("\n  <sodapop:inReplyTo>")) {
	    i = msg.indexOf("</sodapop:inReplyTo>");
	    if (i < 0)
		throw new IllegalArgumentException();
	    inReplyTo = msg.substring(22, i);
	    msg = msg.substring(i + 20);
	}

	if (!msg.equals("\n</sodapop:Message>"))
	    throw new IllegalArgumentException();
    }
    
    /**
     * Create reply message to this message, with the content passed as a 
     * parameter
     * 
     * @param content - the content of the created reply
     * @return Message - the reply message
     */
    public Message createReply(Object content) {
	if (content == null)
	    return null;

	Message reply = new Message();
	reply.id = createUniqueID();
	reply.content = content;

	if (type == MessageType.request)
	    reply.type = MessageType.reply;
	else if (type == MessageType.p2p_request)
	    reply.type = MessageType.p2p_reply;
	else
	    return null;

	reply.inReplyTo = id;
	reply.receivers = new String[] { getSource() };
	return reply;
    }
    
    public static Message createReply(String messageIDInReplyTo, String receiver, Object content) {
    	Message reply = new Message();
    	
    	reply.id 		= createUniqueID();
    	reply.content 	= content;
    	reply.type 		= MessageType.p2p_reply;
    	reply.inReplyTo = messageIDInReplyTo;
    	reply.receivers = new String[] { receiver };
    	
    	return reply;
    }

    /**
     * 
     * @return Object - the content of the message
     */
    public Object getContent() {
	return content;
    }
    
    /**
     * 
     * @return String - the string serialization of the content
     */
    public String getContentAsString() {
	if (contentStr == null)
	    contentStr = trySerializationAsContent(content);
	return contentStr;
    }

    /**
     * 
     * @return - the unique ID of the message
     */

    public String getID() {
	return id;
    }

    /**
     * Returns the ID of the message this message replies to
     * 
     * @return String - the ID of the message this message replies to 
     */
    public String getInReplyTo() {
	return inReplyTo;
    }

    /**
     * 
     * @return String[] - array of receiver IDs
     */
    public String[] getReceivers() {
	return receivers;
    }
    
    /**
     * 
     * @return String - the id of the sender. Please note that the id of the 
     * message is composed of a counter and the ID of the sender (thisJVM 
     * variable of the message)
     */
    public String getSource() {
	return id.substring(17);
    }
    
    /**
     * 
     * @return the counter of the sender. Please note that the id of the 
     * message is composed of the counter and the ID of the sender 
     */
    public long getSourceTimeOrder() {
	return Long.parseLong(id.substring(0, 16), 16);
    }

    /**
     * 
     * @return MessageType
     */
    public MessageType getType() {
	return type;
    }
    
    /**
     * 
     * @return boolean - true iff this message was sent from a remote peer
     */
    public boolean isRemote() {
	return !thisJVM.equals(getSource());
    }
    
    /**
     * sets receivers to this message
     * @param receivers
     */
    public void setReceivers(String[] receivers) {
	if (this.receivers == null)
	    this.receivers = receivers;
	else
	    throw new RuntimeException("Cannot change the message receiver!");
    }
    
    /**
     * Serialize the message as string
     * 
     * @return String - the serialized message
     */
    public String toString() {
	StringBuffer sb = new StringBuffer(512 + getContentAsString().length());
	// sb.append("<![CDATA[<sodapop:Message>");
	sb.append("<sodapop:Message>");
	sb.append("\n  <sodapop:id>").append(id).append("</sodapop:id>");
	sb.append("\n  <sodapop:type>").append(type.name()).append(
		"</sodapop:type>");
	sb.append("\n  <sodapop:content>\n").append(contentStr).append(
		"\n    </sodapop:content>");
	if (inReplyTo != null)
	    sb.append("\n  <sodapop:inReplyTo>").append(inReplyTo).append(
		    "</sodapop:inReplyTo>");
	// sb.append("\n</sodapop:Message>]]>");
	sb.append("\n</sodapop:Message>");
	return sb.toString();
    }
}
