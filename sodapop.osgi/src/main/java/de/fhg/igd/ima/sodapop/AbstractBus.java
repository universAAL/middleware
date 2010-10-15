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

import java.util.Hashtable;
import java.util.Iterator;

import de.fhg.igd.ima.sodapop.msg.Message;

/**
 * @author mtazari
 *
 */
public abstract class AbstractBus implements Bus {
	
	private static String SODAPOP_BUS_MEMBER_ID_PREFIX = null;
	private static int SODAPOP_BUS_MEMBER_COUNT = 0;
	
	private String name;
	protected Hashtable registry; // <BusMember>
	protected BusStrategy busStrategy;
	private SodaPop sodapop;
	
	protected AbstractBus(String name, BusStrategy busStrategy, SodaPop sodapop) {
		if (SODAPOP_BUS_MEMBER_ID_PREFIX == null)
			SODAPOP_BUS_MEMBER_ID_PREFIX = sodapop.getID();
		else if (!sodapop.getID().equals(SODAPOP_BUS_MEMBER_ID_PREFIX))
			throw new RuntimeException("Cannot work with more than one instance of SODAPOP!");
		
		this.name = name;
		this.sodapop = sodapop;
		this.busStrategy = busStrategy;
		
		registry = new Hashtable();
		
		busStrategy.start();
	}
	
	public final String getBusName() {
		return name;
	}
	
	BusMember getBusMember(String memberID) {
		return (memberID == null)? null : (BusMember) registry.get(memberID);
	}
	
	protected String getBusMemberID(BusMember bm) {
		String result = null;
		if (bm != null) {
			for (Iterator i=registry.keySet().iterator(); i.hasNext();) {
				String id = (String) i.next();
				if (bm.equals(registry.get(id))) {
					result = id;
					break;
				}
			}
		}
		return result;
	}
	
	public BusMember[] getBusMembers() {
		BusMember[] members = null;
		synchronized (registry) {
			members = (BusMember[]) registry.values().toArray(
					new BusMember[registry.size()]);
		}
		return members;
	}
	
	public String[] getBusMembersByID() {
		String[] members = null;
		synchronized (registry) {
			members = (String[]) registry.keySet().toArray(
					new String[registry.size()]);
		}
		return members;
	}
	
	public final void handleRemoteMessage(Message m) {
		busStrategy.handleMessage(m, null);
	}
	
	public String register(BusMember m) {
		if (m == null)
			return null;

		synchronized (registry) {
			String id = getBusMemberID(m);
			if (id == null) {
				id = SODAPOP_BUS_MEMBER_ID_PREFIX + "_" + (++SODAPOP_BUS_MEMBER_COUNT);
				registry.put(id, m);
				if (registry.size() == 1)
					sodapop.join(this);
			}
			return id;
		}
	}

	public void sendMessage(String senderID, Message msg) {
		busStrategy.handleMessage(msg, senderID);
	}
	
	public void stopBus() {
		Hashtable members = registry;
		registry = null;
		busStrategy.stopThread();
		for (Iterator i=members.values().iterator(); i.hasNext();)
			((BusMember) i.next()).busDyingOut(this);
	}
	
	public void unregister(String memberID, BusMember m) {
		if (memberID != null) {
			synchronized (registry) {
				Object o = registry.remove(memberID);
				if (o != null)
					if (o != m)
						registry.put(memberID, o);
					else if (registry.isEmpty())
						sodapop.leave(this);
			}
		}
	}
}
