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
package org.universAAL.middleware.sodapop;

import java.util.Vector;

import org.universAAL.middleware.sodapop.msg.Message;

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
	    msg = m;
	}

	public void run() {
	    if (msg != null && msg.length == 2 && msg[0] instanceof Message)
		// System.out.println("Calling the strategy-specific handle method!");
		handle((Message) msg[0], (String) msg[1]);
	}
    }

    protected AbstractBus bus;
    protected SodaPop sodapop;
    private Vector queue; // <Message>
    private boolean stopped = false;

    /**
     * Constructor receiving SodaPop instance and creating queue for the
     * messages.
     * 
     * @param sodapop
     *            SodaPop instance
     */
    protected BusStrategy(SodaPop sodapop) {
	this.sodapop = sodapop;
	queue = new Vector();
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

    // TODO add description of the method, what it does
    /**
     * 
     * @param m
     *            message
     * @param senderID
     *            sender (bus member) ID
     */
    protected abstract void handle(Message m, String senderID);

    /**
     * Returns bus based on the passed bus name.
     * 
     * @param name
     *            bus name
     * @return bus
     */
    protected AbstractBus getLocalBusByName(String name) {
	return sodapop.getLocalBusByName(name);
    }

    // TODO add desc and more comments in the method
    /**
     * 
     * @param m
     *            message to be handled
     * @param senderID
     *            sender ID
     */
    public final void handleMessage(Message m, String senderID) {
	if (m == null)
	    return;
	Object[] toAdd = new Object[] { m, senderID };
	synchronized (queue) {
	    if (queue.isEmpty()) {
		queue.add(toAdd);
		queue.notify();
	    } else {
		boolean inserted = false;
		for (int i = 0; !inserted && i < queue.size(); i++)
		    if (m.getSourceTimeOrder() < ((Message) ((Object[]) queue
			    .get(i))[0]).getSourceTimeOrder()) {
			queue.add(i, toAdd);
			inserted = true;
		    }
		if (!inserted)
		    queue.add(toAdd);
	    }
	}
    }

    /**
     * Runs BusStrategy. Until stopping process messages (in separate Threads)
     */
    public final void run() {
	while (!stopped) {
	    Object[] m = null;
	    while (m == null) {
		try {
		    synchronized (queue) {
			if (queue.isEmpty())
			    queue.wait();
			m = (Object[]) queue.remove(0);
			new HandlerThread(m).start(); // process message in
			// separete Thread
		    }
		} catch (Exception e) {
		}
	    }
	}
    }

    /**
     * Joins the bus and the BusStrategy.
     * 
     * @param bus
     *            bus
     */
    public void setBus(AbstractBus bus) {
	if (this.bus != null)
	    throw new RuntimeException("Bus already set!");
	this.bus = bus;
    }

    /**
     * Stops BusStrategy Thread.
     */
    public final void stopThread() {
	stopped = true;
    }
}
