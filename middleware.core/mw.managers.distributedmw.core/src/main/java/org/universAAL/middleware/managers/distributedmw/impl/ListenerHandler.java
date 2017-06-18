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

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.interfaces.space.SpaceStatus;
import org.universAAL.middleware.managers.api.SpaceListener;
import org.universAAL.middleware.managers.distributedmw.impl.DistributedMWManagerImpl.Handler;
import org.universAAL.middleware.rdf.Resource;

/**
 * 
 * @author Carsten Stockloew
 * 
 */
public abstract class ListenerHandler<T> implements SpaceListener {

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

	/**
	 * Set of listener on this node that have subscribed to changes from all
	 * remote nodes. If a node joins the space, then we have to subscribe on
	 * these nodes for these listeners.
	 */
	private Set<T> allPeersListeners;

	ListenerHandler(String add, String remove) {
		TYPE_ADD = add;
		TYPE_REMOVE = remove;
		listeners = new HashMap<PeerCard, Set<T>>();
		localListeners = new HashSet<T>();
		subscribers = new HashSet<PeerCard>();
		allPeersListeners = new HashSet<T>();
		MySpaceListener.getInstance().register(this);
	}

	public class AddListenerHandler implements Handler {
		public void handle(PeerCard sender, Resource r) {
			// a remote peer subscribes to messages from this node
			synchronized (localListeners) {
				synchronized (subscribers) {
					subscribers.add(sender);

					if (subscribers.size() == 1 && localListeners.size() == 0) {
						// this is the first remote subscriber and we have no
						// local listeners
						// -> register the local listener at the bus
						addListenerLocally();
					}
				}
			}
		}
	}

	public class RemoveListenerHandler implements Handler {
		public void handle(PeerCard sender, Resource r) {
			// a remote peer unsubscribes to messages from this node
			synchronized (localListeners) {
				synchronized (subscribers) {
					subscribers.remove(sender);

					if (subscribers.size() == 0 && localListeners.size() == 0) {
						// there is no one left interested in the messages
						// (local and remote)
						// -> remove the listener
						removeListenerLocally();
					}
				}
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
					// the listener subscribes for all nodes
					synchronized (allPeersListeners) {
						allPeersListeners.add(listener);
					}

					// create the list of all nodes from AAL Space Manager
					nodes = MySpaceListener.getInstance().getPeers();
					if (nodes == null) {
						LogUtils.logError(DistributedMWManagerImpl.context, ListenerHandler.class, "addListener",
								"No peers available, not even this peer?");
						return;
					}
				} else if (nodes.size() == 0) {
					// an empty list -> subscribe to this peer only
					nodes.add(DistributedMWManagerImpl.shared.getAalSpaceManager().getMyPeerCard());
				}

				for (PeerCard node : nodes) {
					boolean local = node.equals(DistributedMWManagerImpl.myPeer);
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
							synchronized (subscribers) {
								if (subscribers.size() == 0)
									addListenerLocally();
							}
						} else {
							requests.add(node);
						}
					}
				}

				subscribe(requests);
			}
		}
	}

	public void removeListener(T listener, List<PeerCard> nodes) {
		// the list of all peers for which we have subscribed before
		// -> an un-subscription must be sent to that node
		List<PeerCard> requests = new ArrayList<PeerCard>();
		synchronized (listeners) {
			synchronized (localListeners) {
				if (nodes == null) {
					// the listener un-subscribes from all nodes
					synchronized (allPeersListeners) {
						allPeersListeners.remove(listener);
					}

					// create the list of all nodes from AAL Space Manager
					nodes = MySpaceListener.getInstance().getPeers();
					if (nodes == null) {
						LogUtils.logError(DistributedMWManagerImpl.context, ListenerHandler.class, "addListener",
								"No peers available, not even this peer?");
						return;
					}
				} else if (nodes.size() == 0) {
					// an empty list -> un-subscribe from this peer only
					nodes.add(DistributedMWManagerImpl.shared.getAalSpaceManager().getMyPeerCard());
				}

				for (PeerCard node : nodes) {
					boolean local = node.equals(DistributedMWManagerImpl.myPeer);
					if (local) {
						localListeners.remove(listener);
					} else {
						Set<T> st = listeners.get(node);
						if (st != null) {
							st.remove(listener);
							if (st.size() == 0) {
								listeners.remove(node);
								requests.add(node);
							}
						}
					}

					if (subscribers.size() == 0 && localListeners.size() == 0) {
						// there is no one left interested in the messages
						// (local and remote)
						// -> remove the listener
						removeListenerLocally();
					}
				}

				unsubscribe(requests);
			}
		}
	}

	private void subscribe(List<PeerCard> peers) {
		if (peers == null)
			return;
		if (peers.size() == 0)
			return;
		Resource r = new Resource();
		r.addType(TYPE_ADD, true);
		DistributedMWManagerImpl.sendMessage(r, peers);
	}

	private void unsubscribe(List<PeerCard> peers) {
		if (peers == null)
			return;
		if (peers.size() == 0)
			return;
		Resource r = new Resource();
		r.addType(TYPE_REMOVE, true);
		DistributedMWManagerImpl.sendMessage(r, peers);
	}

	protected abstract void addListenerLocally();

	protected abstract void removeListenerLocally();

	public void spaceJoined(SpaceDescriptor spaceDescriptor) {
		// TODO: subscribe for all allPeersListeners in all peers?
		// or do we get a call to newPeerJoined?
	}

	public void spaceLost(SpaceDescriptor spaceDescriptor) {
		// clear everything
		synchronized (listeners) {
			synchronized (localListeners) {
				synchronized (subscribers) {
					listeners.clear();
					subscribers.clear();
					if (localListeners.size() == 0)
						removeListenerLocally();
				}
			}
		}
	}

	public void peerJoined(final PeerCard peer) {
		// check if we are called with this peer
		if (peer.getPeerID().equals(DistributedMWManagerImpl.shared.getAalSpaceManager().getMyPeerCard()))
			return;

		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// for all listeners in 'allPeersListeners': subscribe to the
				// new peer
				synchronized (listeners) {
					synchronized (allPeersListeners) {
						if (allPeersListeners.size() != 0) {
							// the peer cannot exist already in 'listeners', add
							// it
							Set<T> st = new HashSet<T>();
							st.addAll(allPeersListeners);
							listeners.put(peer, st);

							// and subscribe on the remote node
							List<PeerCard> l = new ArrayList<PeerCard>();
							l.add(peer);
							subscribe(l);
						}
					}
				}
			}
		}.start();
	}

	public void peerLost(PeerCard peer) {
		// remove the peer
		synchronized (listeners) {
			listeners.remove(peer);
		}
		synchronized (localListeners) {
			synchronized (subscribers) {
				subscribers.remove(peer);

				if (subscribers.size() == 0 && localListeners.size() == 0) {
					// there is no one left interested in the messages (local
					// and remote)
					// -> remove the listener
					removeListenerLocally();
				}
			}
		}
	}

	public void spaceStatusChanged(SpaceStatus status) {
		// not needed
	}
}
