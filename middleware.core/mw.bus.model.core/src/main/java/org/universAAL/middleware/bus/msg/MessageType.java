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

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 *         This class contains message types in SodaPop layer. The message types
 *         with prefix "P2P" relate to the "bus - bus" communication while the
 *         ones without that prefix relate to the "bus member - bus"
 *         communication.
 */
public class MessageType {
    public static final int EVENT = 0;
    public static final int P2P_EVENT = 1;
    public static final int P2P_REPLY = 2;
    public static final int P2P_REQUEST = 3;
    public static final int REPLY = 4;
    public static final int REQUEST = 5;

    public static final MessageType event = new MessageType(EVENT);
    public static final MessageType p2p_event = new MessageType(P2P_EVENT);
    public static final MessageType p2p_reply = new MessageType(P2P_REPLY);
    public static final MessageType p2p_request = new MessageType(P2P_REQUEST);
    public static final MessageType reply = new MessageType(REPLY);
    public static final MessageType request = new MessageType(REQUEST);

    private static final String[] names = { "event", "p2p_event", "p2p_reply",
	    "p2p_call", "reply", "request" };

    /**
     * 
     * @param name
     *            name of the message type
     * @return MessageType representation based on the given message type
     */
    public static MessageType valueOf(String name) {
	for (int i = EVENT; i <= REQUEST; i++)
	    if (names[i].equals(name)) {
		switch (i) {
		case EVENT:
		    return event;
		case P2P_EVENT:
		    return p2p_event;
		case P2P_REPLY:
		    return p2p_reply;
		case P2P_REQUEST:
		    return p2p_request;
		case REPLY:
		    return reply;
		case REQUEST:
		    return request;
		}
	    }
	return null;
    }

    private int order;

    /**
     * Default constructor.
     */
    private MessageType() {

    }

    /**
     * Constructor receiving ordinal.
     * 
     * @param order
     *            ordinal of the message type
     */
    private MessageType(int order) {
	this.order = order;
    }

    /**
     * 
     * @return name of the message type
     */
    public String name() {
	return names[order];
    }

    /**
     * 
     * @return order number
     */
    public int ord() {
	return order;
    }
}
