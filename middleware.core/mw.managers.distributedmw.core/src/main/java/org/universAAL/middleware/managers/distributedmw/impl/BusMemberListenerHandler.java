/*	
	Copyright 2007-2016 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.managers.distributedmw.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.member.BusMemberType;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.context.ContextBusFacade;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.distributedmw.api.DistributedBusMemberListener;
import org.universAAL.middleware.managers.distributedmw.impl.DistributedMWManagerImpl.Handler;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceBusFacade;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.middleware.ui.UIBusFacade;

public class BusMemberListenerHandler extends
	ListenerHandler<DistributedBusMemberListener> {
    public static final String TYPE_ADD_BUSMEMBER_LISTENER = DistributedMWManagerImpl.NAMESPACE
	    + "addBusMemberListener";
    public static final String TYPE_REMOVE_BUSMEMBER_LISTENER = DistributedMWManagerImpl.NAMESPACE
	    + "removeBusMemberListener";
    public static final String TYPE_BUSMEMBER_ADDED = DistributedMWManagerImpl.NAMESPACE
	    + "BusMemberAdded";
    public static final String TYPE_BUSMEMBER_REMOVED = DistributedMWManagerImpl.NAMESPACE
	    + "BusMemberRemoved";
    public static final String TYPE_BUSMEMBER_PARAMS_ADDED = DistributedMWManagerImpl.NAMESPACE
	    + "BusMemberParamsAdded";
    public static final String TYPE_BUSMEMBER_PARAMS_REMOVED = DistributedMWManagerImpl.NAMESPACE
	    + "BusMemberParamsRemoved";

    public static final String PROP_BUS_NAME = DistributedMWManagerImpl.NAMESPACE
	    + "busName";
    public static final String PROP_MEMBER_TYPE = DistributedMWManagerImpl.NAMESPACE
	    + "memberType";
    public static final String PROP_PARAMS = DistributedMWManagerImpl.NAMESPACE
	    + "params";
    public static final String MEMBER_TYPE_PREFIX = DistributedMWManagerImpl.NAMESPACE
	    + "memberType_";

    private LocalListener localListener = null;

    public class BusMemberAddedMessageHandler implements Handler {
	public void handle(PeerCard sender, Resource r) {
	    // a remote peer, to which we subscribed, sent us a message
	    // -> notify all listeners
	    String busMemberID = r.getURI();
	    String busName = (String) r.getProperty(PROP_BUS_NAME);
	    String label = r.getResourceLabel();
	    String comment = r.getResourceComment();
	    BusMemberType memberType = BusMemberType.valueOf(((String) r
		    .getProperty(PROP_MEMBER_TYPE))
		    .substring(MEMBER_TYPE_PREFIX.length()));

	    Set<DistributedBusMemberListener> st = null;
	    synchronized (listeners) {
		st = listeners.get(sender);
		if (st == null || st.size() == 0)
		    return;
		// dispatch message
		for (DistributedBusMemberListener l : st) {
		    l.busMemberAdded(sender, busMemberID, busName, memberType,
			    label, comment);
		}
	    }
	}
    }

    public class BusMemberRemovedMessageHandler implements Handler {
	public void handle(PeerCard sender, Resource r) {
	    // a remote peer, to which we subscribed, sent us a message
	    // -> notify all listeners
	    Set<DistributedBusMemberListener> st = null;
	    synchronized (listeners) {
		st = listeners.get(sender);
		if (st == null || st.size() == 0)
		    return;
		// dispatch message
		for (DistributedBusMemberListener l : st) {
		    l.busMemberRemoved(sender, r.getURI());
		}
	    }
	}
    }

    public class RegParamsAddedMessageHandler implements Handler {
	public void handle(PeerCard sender, Resource r) {
	    // a remote peer, to which we subscribed, sent us a message
	    // -> notify all listeners
	    Set<DistributedBusMemberListener> st = null;
	    synchronized (listeners) {
		st = listeners.get(sender);
		if (st == null || st.size() == 0)
		    return;
		// dispatch message
		for (DistributedBusMemberListener l : st) {
		    l.regParamsAdded(sender, r.getURI(),
			    (Resource[]) ((List<?>) r.getProperty(PROP_PARAMS))
				    .toArray());
		}
	    }
	}
    }

    public class RegParamsRemovedMessageHandler implements Handler {
	public void handle(PeerCard sender, Resource r) {
	    // a remote peer, to which we subscribed, sent us a message
	    // -> notify all listeners
	    Set<DistributedBusMemberListener> st = null;
	    synchronized (listeners) {
		st = listeners.get(sender);
		if (st == null || st.size() == 0)
		    return;
		// dispatch message
		for (DistributedBusMemberListener l : st) {
		    l.regParamsRemoved(sender, r.getURI(),
			    (Resource[]) ((List<?>) r.getProperty(PROP_PARAMS))
				    .toArray());
		}
	    }
	}
    }

    public class LocalListener implements IBusMemberRegistryListener {
	public void busMemberAdded(BusMember member, BusType type) {
	    synchronized (listeners) {
		// init info
		String busMemberID = member.getURI();
		String busName = "";
		switch (type) {
		case Service:
		    busName = ((AbstractBus) (ServiceBusFacade
			    .fetchBus(DistributedMWManagerImpl.context)))
			    .getBrokerName();
		    break;
		case Context:
		    busName = ((AbstractBus) (ContextBusFacade
			    .fetchBus(DistributedMWManagerImpl.context)))
			    .getBrokerName();
		    break;
		case UI:
		    busName = ((AbstractBus) (UIBusFacade
			    .fetchBus(DistributedMWManagerImpl.context)))
			    .getBrokerName();
		    break;
		}
		String label = member.getLabel();
		String comment = member.getComment();
		BusMemberType memberType = member.getType();

		// local subscriptions
		for (DistributedBusMemberListener l : localListeners) {
		    l.busMemberAdded(DistributedMWManagerImpl.myPeer,
			    busMemberID, busName, memberType, label, comment);
		}

		// remote subscriptions
		if (subscribers != null) {
		    Resource r = new Resource(busMemberID);
		    r.addType(TYPE_BUSMEMBER_ADDED, true);
		    r.setProperty(PROP_BUS_NAME, busName);
		    r.setProperty(PROP_MEMBER_TYPE, MEMBER_TYPE_PREFIX
			    + memberType.toString());
		    r.setResourceLabel(label);
		    r.setResourceComment(comment);

		    for (PeerCard peer : subscribers) {
			DistributedMWManagerImpl.sendMessage(r, peer);
		    }
		}
	    }
	}

	public void busMemberRemoved(BusMember member, BusType type) {
	    synchronized (listeners) {
		// local subscriptions
		for (DistributedBusMemberListener l : localListeners) {
		    l.busMemberRemoved(DistributedMWManagerImpl.myPeer,
			    member.getURI());
		}

		// remote subscriptions
		if (subscribers != null) {
		    Resource r = new Resource(member.getURI());
		    r.addType(TYPE_BUSMEMBER_REMOVED, true);

		    for (PeerCard peer : subscribers) {
			DistributedMWManagerImpl.sendMessage(r, peer);
		    }
		}
	    }
	}

	public void regParamsAdded(String busMemberID, Resource[] params) {
	    synchronized (listeners) {
		// local subscriptions
		for (DistributedBusMemberListener l : localListeners) {
		    l.regParamsAdded(DistributedMWManagerImpl.myPeer,
			    busMemberID, params);
		}

		// remote subscriptions
		if (subscribers != null) {
		    Resource r = new Resource(busMemberID);
		    r.addType(TYPE_BUSMEMBER_PARAMS_ADDED, true);
		    r.setProperty(PROP_PARAMS,
			    new ArrayList<Object>(Arrays.asList(params)));

		    for (PeerCard peer : subscribers) {
			DistributedMWManagerImpl.sendMessage(r, peer);
		    }
		}
	    }
	}

	public void regParamsRemoved(String busMemberID, Resource[] params) {
	    synchronized (listeners) {
		// local subscriptions
		for (DistributedBusMemberListener l : localListeners) {
		    l.regParamsRemoved(DistributedMWManagerImpl.myPeer,
			    busMemberID, params);
		}

		// remote subscriptions
		if (subscribers != null) {
		    Resource r = new Resource(busMemberID);
		    r.addType(TYPE_BUSMEMBER_PARAMS_REMOVED, true);
		    r.setProperty(PROP_PARAMS,
			    new ArrayList<Object>(Arrays.asList(params)));

		    for (PeerCard peer : subscribers) {
			DistributedMWManagerImpl.sendMessage(r, peer);
		    }
		}
	    }
	}
    }

    public BusMemberListenerHandler() {
	super(TYPE_ADD_BUSMEMBER_LISTENER, TYPE_REMOVE_BUSMEMBER_LISTENER);
    }

    @Override
    protected void addListenerLocally() {
	synchronized (this) {
	    if (localListener == null) {
		localListener = new LocalListener();

		IBusMemberRegistry registry = (IBusMemberRegistry) DistributedMWManagerImpl.context
			.getContainer().fetchSharedObject(
				DistributedMWManagerImpl.context,
				IBusMemberRegistry.busRegistryShareParams);
		registry.addListener(localListener, true);
	    }
	}
    }
}
