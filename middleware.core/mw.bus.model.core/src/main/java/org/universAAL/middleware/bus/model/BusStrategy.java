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
package org.universAAL.middleware.bus.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;

/**
 * A bus strategy for handling messages. Subclasses must implement their
 * strategy within the <code>handle(Message)</code> method.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public abstract class BusStrategy extends Thread {

    /**
     * 
     * Private class that helps handling bus message by calling bus strategy
     * specific method (and passes message and sender ID to it).
     */
    private class HandlerThread extends Thread {
	private Object[] msg;

	HandlerThread(Object[] m) {
	    super(nameHandler);
	    msg = m;
	}

	@Override
	public void run() {
	    if (msg != null && msg.length == 2 && msg[0] instanceof BusMessage) {
		BusMessage message = (BusMessage) msg[0];
		String senderID = (String) msg[1];

		handle(message, senderID);
	    }
	}

    }

    protected AbstractBus bus;
    protected ModuleContext busModule;
    protected CommunicationModule commModule;
    private Vector<Object[]> queue; // <Message>
    private boolean stopped = false;
    private String nameHandler = "";

    /**
     * Constructor receiving the {@link CommunicationModule} instance and
     * creating queue for the messages.
     * 
     * @param commModule
     *            {@link CommunicationModule} instance
     */
    protected BusStrategy(CommunicationModule commModule) {
	this(commModule, "BusStrategy");
    }

    /**
     * Constructor receiving the {@link CommunicationModule} instance and
     * creating queue for the messages.
     * 
     * @param commModule
     *            {@link CommunicationModule} instance
     * @param name
     *            Human-readable name of the Bus Strategy
     */
    protected BusStrategy(CommunicationModule commModule, String name) {
	super(name);
	this.nameHandler = name + " Handler";
	this.commModule = commModule;
	queue = new Vector<Object[]>();
    }

    /**
     * Returns bus member instance based on the bus member ID.
     * 
     * @param memberID
     *            bus member ID
     * @return BusMember
     */
    protected BusMember getBusMember(String memberID) {
	return bus.getBusMember(memberID);
    }

    /**
     * Handle the message. This method is different from the handleMessage(), in
     * that handeMessage() handle the message asynchronously, while queuing the
     * messages to preserve the order of the messages. This method is executed
     * synchronously, without any queuing.
     * 
     * @param m
     *            message
     * @param senderID
     *            sender (bus member) ID
     */
    protected abstract void handle(BusMessage m, String senderID);

    /**
     * Returns bus based on the passed bus name.
     * 
     * @param name
     *            bus name
     * @return bus
     */
    protected AbstractBus getLocalBusByName(String name) {
	Object o = commModule.getListenerByNameAndType(name, AbstractBus.class);
	return (o instanceof AbstractBus) ? (AbstractBus) o : null;
    }

    /**
     * This method handles the message asynchronously, while trying to preserve
     * the order of the messages from the same source, according to the counters
     * of the messages. (The counters are part of the message ID). The messages
     * are queued, and they are inserted into the queue according to their
     * counter.
     * 
     * @param m
     *            message to be handled
     * @param senderID
     *            sender ID
     */
    public final void handleMessage(BusMessage m, String senderID) {
	if (m == null) {
	    return;
	}
	Object[] toAdd = new Object[] { m, senderID };
	synchronized (queue) {
	    if (queue.isEmpty()) {
		queue.add(toAdd);
		queue.notify();
	    } else {
		boolean inserted = false;
		for (int i = 0; !inserted && i < queue.size(); i++) {
		    BusMessage current = (BusMessage) queue.get(i)[0];
		    if (m.getSender().getPeerID()
			    .equals(current.getSender().getPeerID())
			    && m.getIDAsLong() < current.getIDAsLong()) {
			queue.add(i, toAdd);
			inserted = true;
		    }
		}
		if (!inserted) {
		    queue.add(toAdd);
		}
	    }
	}
    }

    /**
     * Runs BusStrategy. Until stopping process messages (in separate Threads)
     */
    @Override
    public final void run() {
	while (!stopped) {
	    Object[] m = null;
	    try {
		synchronized (queue) {
		    if (queue.isEmpty()) {
			queue.wait();
		    }
		    m = queue.remove(0);
		    new HandlerThread(m).start(); // process message in
		    // separate Thread
		}
	    } catch (Exception e) {
	    }
	}
    }

    void start(ModuleContext mc) {
	busModule = mc;
	start();
    }

    /**
     * Joins the bus and the BusStrategy.
     * 
     * @param bus
     *            bus
     */
    public void setBus(AbstractBus bus) {
	if (this.bus != null) {
	    throw new RuntimeException("Bus already set!");
	}
	this.bus = bus;
    }

    /**
     * Stops BusStrategy Thread.
     */
    public final void stopThread() {
	stopped = true;
	queue.notify();
    }

    protected ChannelMessage buildChannelMessage(BusMessage m) {
	// ...and wrap it as ChannelMessage
	List<String> channelName = new ArrayList<String>();
	channelName.add(bus.getBrokerName());
	ChannelMessage channelMessage = new ChannelMessage(bus.getPeerCard(),
		m.toString(), channelName);
	return channelMessage;
    }

    protected void send(BusMessage message) {
	PeerCard[] receivers = message.getReceivers();

	// wait until the communication connector has configured this channel
	int cnt = -1;
	while (!commModule.hasChannel(busModule.getID())) {
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    cnt++;
	    if (cnt % 50 == 0) // log a message every 5 seconds
		LogUtils.logError(
			busModule,
			BusStrategy.class,
			"send",
			new Object[] {
				"The communication connector is not yet configured correctly (channel ",
				busModule.getID(),
				" is unknown). Waiting for the configuration to send the message. Time elapsed: ",
				cnt / 10, " seconds." }, null);
	}

	// send the message
	if (isBroadcast(receivers)) {
	    commModule.sendAll(buildChannelMessage(message), bus);
	} else if (isUnicast(receivers)) {
	    commModule.send(buildChannelMessage(message), receivers[0]);
	} else {
	    commModule.sendAll(buildChannelMessage(message),
		    Arrays.asList(receivers), bus);
	}
    }

    private boolean isUnicast(PeerCard[] receivers) {
	return receivers.length == 1;
    }

    private boolean isBroadcast(PeerCard[] receivers) {
	return receivers == null || receivers.length == 0;
    }
}
