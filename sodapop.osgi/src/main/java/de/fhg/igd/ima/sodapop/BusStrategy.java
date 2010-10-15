/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package de.fhg.igd.ima.sodapop;

import java.util.Vector;

import de.fhg.igd.ima.sodapop.msg.Message;

/**
 * A bus strategy for handling messages. Subclasses must implement their strategy
 * within the <code>handle(Message)</code> method.
 * 
 * @author mtazari
 */
public abstract class BusStrategy extends Thread {
	
	private class HandlerThread extends Thread {
		private Object[] msg;
		
		HandlerThread(Object[] m) {
			msg = m;
		}
		
		public void run() {
			if (msg != null  &&  msg.length == 2  &&  msg[0] instanceof Message)
			// System.out.println("Calling the strategy-specific handle method!");
				handle((Message) msg[0], (String) msg[1]);
		}
	}
	
	protected AbstractBus bus;
	protected SodaPop sodapop;
	private Vector queue; // <Message>
	private boolean stopped = false;
	
	protected BusStrategy(SodaPop sodapop) {
		this.sodapop = sodapop;
		queue = new Vector();
	}
	
	protected BusMember getBusMember(String memberID) {
		return bus.getBusMember(memberID);
	}
	
	protected abstract void handle(Message m, String senderID);
	
	protected AbstractBus getLocalBusByName(String name) {
		return sodapop.getLocalBusByName(name);
	}
	
	public final void handleMessage(Message m, String senderID) {
		if (m == null)
			return;
		Object[] toAdd = new Object[] {m, senderID};
		synchronized (queue) {
			if (queue.isEmpty()) {
				queue.add(toAdd);
				queue.notify();
			} else {
				boolean inserted = false;
				for (int i=0; !inserted && i<queue.size(); i++)
					if (m.getSourceTimeOrder() < ((Message) ((Object[]) queue.get(i))[0]).getSourceTimeOrder()) {
						queue.add(i, toAdd);
						inserted = true;
					}
				if (!inserted)
					queue.add(toAdd);
			}
		}
	}
	
	public final void run() {
		while (!stopped) {
			Object[] m = null;
			while (m == null) {
				try {
					synchronized (queue) {
						if (queue.isEmpty())
							queue.wait();
						m = (Object[]) queue.remove(0);
						new HandlerThread(m).start();
					}
				} catch (Exception e) {}
			}
		}
	}
	
	public void setBus(AbstractBus bus) {
		if (this.bus != null)
			throw new RuntimeException("Bus already set!");
		this.bus = bus;
	}
	
	public final void stopThread() {
		stopped = true;
	}
}
