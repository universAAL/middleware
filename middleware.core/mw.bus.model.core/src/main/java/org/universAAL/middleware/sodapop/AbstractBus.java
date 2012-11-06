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

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.sodapop.impl.SodaPopImpl;
import org.universAAL.middleware.sodapop.msg.Message;

/**
 * Defines a shared mechanisms realized by the bus. This mainly means that it
 * offers abstract view on the bus strategy and manages the bus message queue
 * and by that reduces the implementation of the concrete bus strategy.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public abstract class AbstractBus implements Bus {

    private static String SODAPOP_BUS_MEMBER_ID_PREFIX = null;
    private static int SODAPOP_BUS_MEMBER_COUNT = 0;

    private String name;
    protected IRegistry registry;

    // protected Hashtable registry; // <BusMember>
    protected BusStrategy busStrategy;
    protected SodaPop sodapop;

    protected AbstractBus() {
    }

    /**
     * Constructor taking bus name, bus strategy and SodaPop instance as an
     * input.
     * 
     * @param name
     *            bus name
     * @param sodapop
     *            SodaPop instance
     */
    protected AbstractBus(String name, SodaPop sodapop) {
	if (SODAPOP_BUS_MEMBER_ID_PREFIX == null)
	    SODAPOP_BUS_MEMBER_ID_PREFIX = sodapop.getID();
	else if (!sodapop.getID().equals(SODAPOP_BUS_MEMBER_ID_PREFIX))
	    throw new RuntimeException(
		    "Cannot work with more than one instance of SODAPOP!");

	this.name = name;
	this.sodapop = sodapop;
	this.busStrategy = createBusStrategy(sodapop);

	registry = createReigstry();

	busStrategy.start();
    }

    protected IRegistry createReigstry() {
	return new RegistryMap();
    }

    protected abstract BusStrategy createBusStrategy(SodaPop sodapop);

    /**
     * Returns bus name.
     */
    public final String getBusName() {
	return name;
    }

    /**
     * Returns bus member instance based on its member ID.
     * 
     * @param memberID
     *            bus member ID
     * @return bus member instance
     */
    public BusMember getBusMember(String memberID) {
	return registry.getBusMemberByID(memberID);
    }

    /**
     * Returns bus member ID based on bus member reference.
     * 
     * @param bm
     *            bus member instance
     * @return bus member ID
     */
    public String getBusMemberID(BusMember bm) {
	return registry.getBusMemberID(bm);
    }

    /**
     * 
     * @return all bus members in BusMember array
     */
    public BusMember[] getBusMembers() {
	BusMember[] members = null;
	synchronized (registry) {
	    members = registry.getAllBusMembers();
	}
	return members;
    }

    /**
     * 
     * @return IDs of all bus members in array
     */
    public String[] getBusMembersByID() {
	String[] members = null;
	synchronized (registry) {
	    members = registry.getAllBusMembersIds();
	}
	return members;
    }

    /**
     * Registers a new bus member (adds a bus member to the list of all bus
     * members but only if it has not been added before). Bus member gets its ID
     * and is stored together with the given bus member to the list of bus
     * members (bus member ID has following structure: sodapopID+"_"+ordinal
     * number of this bus member in overall SodaPop layer). If this is the first
     * bus member to register to the bus this method also joins this bus to the
     * SodaPop layer. Method returns (previously described) ID of the added bus
     * member.
     */
    public String register(BusMember m) {
	if (m == null)
	    return null;

	synchronized (registry) {
	    String id = getBusMemberID(m);
	    // register bus member only if it has not been added
	    // (registered) to the bus before
	    if (id == null) {
		// compose bus member ID
		id = SODAPOP_BUS_MEMBER_ID_PREFIX + "_"
			+ (++SODAPOP_BUS_MEMBER_COUNT);
		registry.addBusMember(id, m);

		if (registry.getBusMembersCount() == 1)
		    sodapop.join(this);
	    }
	    return id;
	}
    }

    /**
     * Passes the message to the BusStrategy to be handled.
     * 
     * @param m
     *            message to handle
     */
    public final void handleRemoteMessage(Message m) {
	LogUtils.logInfo(SodaPopImpl.moduleContext, AbstractBus.class,
		"handleRemoteMessage", new Object[] { name,
			" - Received message from peer ", m.getSource(), ":\n",
			m.getContentAsString() }, null);
	// sender ID is null for remote messages
	busStrategy.handleMessage(m, null);
    }

    /**
     * Calls message handling in BusStrategy (gives sender ID and message to
     * process on the bus).
     */
    public void sendMessage(String senderID, Message msg) {
	LogUtils.logInfo(SodaPopImpl.moduleContext, AbstractBus.class,
		"sendMessage", new Object[] { name,
			" - Received message from bus member ", senderID,
			":\n", msg.getContentAsString() }, null);
	busStrategy.handleMessage(msg, senderID);
    }

    /**
     * Stops the bus (deletes all bus members from the list, stops BusStrategy
     * thread and announces that the bus is being stopped to its members).
     */
    public void stopBus() {
	BusMember[] members = registry.getAllBusMembers();
	registry.reset();
	registry = null;
	busStrategy.stopThread();
	for (int i = 0; i < members.length; i++) {
	    members[i].busDyingOut(this);
	}
    }

    /**
     * Unregisters bus member from the bus (if the bus member with given ID
     * exists). It the last member unregistered from the bus than the bus leaves
     * SodaPop also.
     */
    public void unregister(String memberID, BusMember m) {
	if (memberID != null) {
	    synchronized (registry) {
		BusMember o = registry.removeMemberByID(memberID);
		if (o != null) {
		    //if (o.equals(m)) {
			//registry.addBusMember(memberID, o);
		    //} else 
		    if (registry.getBusMembersCount() == 0) {
			sodapop.leave(this);
		    }
		}
	    }
	}
    }

    public boolean addRegistryListener(IRegistryListener listener) {
	return registry.addRegistryListener(listener);
    }

    public boolean removeRegistryListener(IRegistryListener listener) {
	return registry.removeRegistryListener(listener);
    }
}
