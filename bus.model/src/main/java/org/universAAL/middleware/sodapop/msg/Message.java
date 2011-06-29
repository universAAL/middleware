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

import java.net.InetAddress;
import java.util.Random;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class Message {
    private static BundleContext context = null;
    private static long counter = 0;
    private static int idLength;
    public static final String thisJVM;
    static {
	String host = "localhost";
	try {
	    host = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}
	long now = System.currentTimeMillis();
	String peerID = System.getProperty("sodapop.peerID");
	if (peerID == null) {
	    thisJVM = Long.toHexString(now) + '@' + host + '+'
		    + Integer.toHexString(new Random(now).nextInt());
	} else {
	    thisJVM = peerID + '+'
		    + Integer.toHexString(new Random(now).nextInt());
	}
	idLength = thisJVM.length() + 17;
    }

    public static synchronized String createUniqueID() {
	StringBuffer b = new StringBuffer(idLength);
	b.append(fill0(Long.toHexString(counter++), 16));
	b.append(':').append(thisJVM);
	return b.toString();
    }

    public static synchronized String createUniqueLocalID(String localPrefix,
	    int counter, int counterLen) {
	return localPrefix + fill0(Long.toHexString(counter), counterLen);
    }

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

    public static void setBundleContext(BundleContext c) {
	context = c;
    }

    private Object content;
    private String id, inReplyTo = null;
    private String[] receivers = null;
    private MessageType type;
    private MessageContentSerializer contentSerializer = null;

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

	content = getContentSerializer().deserialize(msg.substring(j, i));
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

    private Message() {

    }

    public Message(MessageType type, Object content) {
	if (type == null || content == null || type == MessageType.p2p_reply
		|| type == MessageType.reply)
	    throw new IllegalArgumentException();

	this.content = content;
	this.type = type;
	id = createUniqueID();
    }

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

    public Object getContent() {
	return content;
    }

    public String getContentAsString() {
	MessageContentSerializer s = getContentSerializer();
	return (s == null) ? content.toString() : s.serialize(content);
    }

    private MessageContentSerializer getContentSerializer() {
	if (contentSerializer == null) {
	    ServiceReference sr = context
		    .getServiceReference(MessageContentSerializer.class
			    .getName());
	    contentSerializer = (sr == null) ? null
		    : (MessageContentSerializer) context.getService(sr);
	}
	return contentSerializer;
    }

    public String getID() {
	return id;
    }

    public String getInReplyTo() {
	return inReplyTo;
    }

    public String[] getReceivers() {
	return receivers;
    }

    public String getSource() {
	return id.substring(17);
    }

    public long getSourceTimeOrder() {
	return Long.parseLong(id.substring(0, 16), 16);
    }

    public MessageType getType() {
	return type;
    }

    public boolean isRemote() {
	return !thisJVM.equals(getSource());
    }

    public void setReceivers(String[] receivers) {
	if (this.receivers == null)
	    this.receivers = receivers;
	else
	    throw new RuntimeException("Cannot change the message receiver!");
    }

    public String toString() {
	String contentSerialization = getContentAsString();

	StringBuffer sb = new StringBuffer(512 + contentSerialization.length());
	// sb.append("<![CDATA[<sodapop:Message>");
	sb.append("<sodapop:Message>");
	sb.append("\n  <sodapop:id>").append(id).append("</sodapop:id>");
	sb.append("\n  <sodapop:type>").append(type.name()).append(
		"</sodapop:type>");
	sb.append("\n  <sodapop:content>\n").append(contentSerialization)
		.append("\n    </sodapop:content>");
	if (inReplyTo != null)
	    sb.append("\n  <sodapop:inReplyTo>").append(inReplyTo).append(
		    "</sodapop:inReplyTo>");
	// sb.append("\n</sodapop:Message>]]>");
	sb.append("\n</sodapop:Message>");
	return sb.toString();
    }
}
