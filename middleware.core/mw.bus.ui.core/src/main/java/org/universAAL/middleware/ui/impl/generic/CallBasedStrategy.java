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

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * A set of methods to place calls and responses, both for synchronous and
 * asynchronous calls.
 * 
 * @author amedrano
 * 
 */
public class CallBasedStrategy extends EventBasedStrategy {

    /**
     * A class to store the status of any given call.
     * 
     * @author amedrano
     * 
     */
    private static class CallStatus {
	Object output = null;
	boolean returned = false;
	boolean purged = false;

	void setResutlt(Object o) {
	    synchronized (this) {
		returned = true;
		output = o;
		notifyAll();
	    }
	}
    }

    /**
     * The status of all pending syncronous calls.
     */
    private Map<String, CallStatus> syncCalls;

    /**
     * @param commModule
     * @param name
     */
    public CallBasedStrategy(CommunicationModule commModule, String name) {
	super(commModule, name);
	syncCalls = new HashMap<String, CallBasedStrategy.CallStatus>();
	// TODO monitor peers and abort all synchronous call to a peer when this
	// disconnects form the space.
    }

    /**
     * @param commModule
     */
    public CallBasedStrategy(CommunicationModule commModule) {
	super(commModule);
    }

    /** {@ inheritDoc} */
    protected void handle(BusMessage m, String senderID) {
	super.handle(m, senderID);

	// Collect response from synchronous call
	if (m.getType().equals(MessageType.p2p_reply)) {
	    String id = m.getInReplyTo();
	    synchronized (syncCalls) {
		CallStatus st = syncCalls.remove(id);
		if (st != null) {
		    st.setResutlt(m.getContent());
		}
	    }
	}
    }

    /**
     * Place a Synchronous request, The {@link CallMessage} is sent and the
     * message is anotated as waiting for response, the calling thread will be
     * set in a waiting state until either a response to the message is
     * received, or the call is {@link CallBasedStrategy#abortCall(String)
     * aborted}.
     * 
     * @param memberID
     *            The recipient of the request
     * @param callMessage
     *            The Message to send.
     * @return the response to the message
     * @throws InterruptedException
     *             if the call was aborted.
     */
    protected Object placeSynchronousRequest(String memberID,
	    CallMessage<? extends CallBasedStrategy> callMessage)
	    throws InterruptedException {
	CallStatus status = new CallStatus();
	((UIBusImpl) bus).assessContentSerialization(callMessage);
	BusMessage m = new BusMessage(MessageType.p2p_request, callMessage, bus);
	m.setReceiver(AbstractBus.getPeerFromBusResourceURI(memberID));
	synchronized (syncCalls) {
	    syncCalls.put(m.getID(), status);
	}
	send(m);
	// synchronize
	synchronized (status) {
	    while (!status.returned) {
		try {
		    status.wait();
		} catch (InterruptedException e) {
		}
	    }
	}
	if (status.purged) {
	    throw new InterruptedException();
	}
	return status.output;
    }

    /**
     * Send an asynchronous request to memberID. This method will not store the
     * call thus the response has to be able to perform the response part by it
     * self, by for example replying a {@link EventMessage} or a
     * {@link CallMessage} the
     * {@link CallMessage#onResponse(BusStrategy, BusMessage, String)
     * onResponse} will have to manage.
     * 
     * @param memberID
     * @param callMessage
     */
    protected void placeAsynchronousRequest(String memberID,
	    CallMessage<? extends CallBasedStrategy> callMessage) {
	((UIBusImpl) bus).assessContentSerialization(callMessage);
	BusMessage m = new BusMessage(MessageType.p2p_request, callMessage, bus);
	m.setReceiver(AbstractBus.getPeerFromBusResourceURI(memberID));
	send(m);
    }

    /**
     * Place a Synchronous request, The {@link CallMessage} is sent and the
     * message is anotated as waiting for response, the calling thread will be
     * set in a waiting state until either a response to the message is
     * received, or the call is {@link CallBasedStrategy#abortCall(String)
     * aborted}.
     * 
     * @param memberID
     *            The recipient of the request
     * @param callMessage
     *            The Message to send.
     * @return the response to the message
     * @throws InterruptedException
     *             if the call was aborted.
     */
    protected Object placeSynchronousRequest(PeerCard peer,
	    CallMessage<? extends CallBasedStrategy> callMessage)
	    throws InterruptedException {
	CallStatus status = new CallStatus();
	((UIBusImpl) bus).assessContentSerialization(callMessage);
	BusMessage m = new BusMessage(MessageType.p2p_request, callMessage, bus);
	m.setReceiver(peer);
	synchronized (syncCalls) {
	    syncCalls.put(m.getID(), status);
	}
	send(m);
	// synchronize
	synchronized (status) {
	    while (!status.returned) {
		try {
		    status.wait();
		} catch (InterruptedException e) {
		}
	    }
	}
	if (status.purged) {
	    throw new InterruptedException();
	}
	return status.output;
    }

    /**
     * Send an asynchronous request to memberID. This method will not store the
     * call thus the response has to be able to perform the response part by it
     * self, by for example replying a {@link EventMessage} or a
     * {@link CallMessage} the
     * {@link CallMessage#onResponse(BusStrategy, BusMessage, String)
     * onResponse} will have to manage.
     * 
     * @param memberID
     * @param callMessage
     */
    protected void placeAsynchronousRequest(PeerCard peer,
	    CallMessage<? extends CallBasedStrategy> callMessage) {
	((UIBusImpl) bus).assessContentSerialization(callMessage);
	BusMessage m = new BusMessage(MessageType.p2p_request, callMessage, bus);
	m.setReceiver(peer);
	send(m);
    }

    /**
     * Abort the call with the given ID. it unblocks the call and the
     * {@link CallSynchronizer#performCall(Object, Object)} will throw a
     * {@link InterruptedException}.
     * 
     * @param msgId
     *            the messageId of the call that should be aborted.
     */
    public void abortCall(String msgId) {
	synchronized (syncCalls) {
	    CallStatus st = syncCalls.remove(msgId);
	    if (st != null) {
		st.purged = true;
		st.setResutlt(null);
	    }
	}
    }

    /**
     * Abort all pending calls,
     */
    public synchronized void abortAll() {
	for (CallStatus st : syncCalls.values()) {
	    st.purged = true;
	    st.setResutlt(null);
	}
	syncCalls.clear();
    }

    /**
     * For use of {@link CallMessage} subclasses to send a response to the call.
     * 
     * @param original
     * @param resp
     */
    public void sendSynchronousResponse(BusMessage original, Object resp) {
	send(original.createReply(resp));
    }

    public void sendAsynchronousResponse(PeerCard peer,
	    CallMessage<? extends CallBasedStrategy> response) {
	((UIBusImpl) bus).assessContentSerialization(response);
	BusMessage m = BusMessage.createP2PReply("async", peer, response, bus);
	send(m);
    }

    public void sendAsynchronousResponse(String memberID,
	    CallMessage<? extends CallBasedStrategy> response) {
	sendAsynchronousResponse(
		AbstractBus.getPeerFromBusResourceURI(memberID), response);
    }
}
