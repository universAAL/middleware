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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.distributedmw.impl.DistributedMWManagerImpl.Handler;
import org.universAAL.middleware.rdf.Resource;

/**
 * 
 * @author Carsten Stockloew
 * 
 */
// TODO: use AAL Space Listener to remove nodes that have subscribed and then
// disappear
public abstract class ListenerHandler<T> {

    private String TYPE_ADD;
    private String TYPE_REMOVE;

    /**
     * Remote nodes that we have subscribed to. Message flow: remote -> local.
     */
    protected HashMap<PeerCard, Set<T>> listeners;

    /**
     * Listeners on this node that have subscribed to messages from this node.
     * Message flow: local -> local.
     */
    protected Set<T> localListeners;

    /**
     * Remote peers that have subscribed to messages from this node. Message
     * flow: local -> remote.
     */
    protected Set<PeerCard> subscribers;

    ListenerHandler(String add, String remove) {
	TYPE_ADD = add;
	TYPE_REMOVE = remove;
	listeners = new HashMap<PeerCard, Set<T>>();
	localListeners = new HashSet<T>();
	subscribers = new HashSet<PeerCard>();
    }

    public class AddListenerHandler implements Handler {
	public void handle(PeerCard sender, Resource r) {
	    // a remote peer subscribes to messages from this node
	    synchronized (subscribers) {
		subscribers.add(sender);
	    }
	}
    }

    public class RemoveListenerHandler implements Handler {
	public void handle(PeerCard sender, Resource r) {
	    // a remote peer unsubscribes to messages from this node
	    synchronized (subscribers) {
		subscribers.remove(sender);
	    }
	}
    }

    public void addListener(T listener, List<PeerCard> nodes) {
	// the list of all peers for which we did not subscribe before
	// -> a subscription must be sent to that node
	List<PeerCard> requests = new ArrayList<PeerCard>();
	synchronized (listeners) {
	    synchronized (localListeners) {
		if (nodes == null) {
		    // TODO
		    // the listener subscribes for all nodes
		    // -> create the list of all nodes from AAL Space Manager
		    return;
		}

		for (PeerCard node : nodes) {
		    boolean local = node
			    .equals(DistributedMWManagerImpl.myPeer);
		    Set<T> st;
		    if (local) {
			st = localListeners;
		    } else {
			st = listeners.get(node);
			if (st == null)
			    st = new HashSet<T>();
			listeners.put(node, st);
		    }

		    if (st.contains(listener))
			continue;

		    st.add(listener);

		    if (st.size() == 1) {
			// first listener for this node -> send request
			// check if the node is this node; handle locally
			if (local) {
			    addListenerLocally();
			} else {
			    requests.add(node);
			}
		    }
		}

		if (requests.size() != 0) {
		    Resource r = new Resource();
		    r.addType(TYPE_ADD, true);
		    DistributedMWManagerImpl.sendMessage(r, requests);
		}
	    }
	}
    }

    public void removeListener(T listener, List<PeerCard> nodes) {
	// TODO
    }

    protected abstract void addListenerLocally();

    // protected abstract void removeListenerLocally();
}
