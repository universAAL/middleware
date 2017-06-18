package org.universAAL.middleware.managers.distributedmw.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.interfaces.space.SpaceStatus;
import org.universAAL.middleware.managers.api.SpaceListener;
import org.universAAL.middleware.managers.api.SpaceManager;

/**
 * 
 * @author Carsten Stockloew
 * 
 */
public class MySpaceListener implements SpaceListener, SharedObjectListener {

	private SpaceManager theAALSpaceManager = null;

	// the singleton instance
	private static MySpaceListener instance = null;

	private List<SpaceListener> listeners = new ArrayList<SpaceListener>();

	private MySpaceListener() {
	}

	public static MySpaceListener getInstance() {
		if (instance == null) {
			instance = new MySpaceListener();
			instance.start();
		}
		return instance;
	}

	public void register(SpaceListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	public void start() {
		// get AAL Space Manager to register this listener
		Object[] o = DistributedMWManagerImpl.context.getContainer().fetchSharedObject(DistributedMWManagerImpl.context,
				new Object[] { SpaceManager.class.getName() }, this);
		if (o != null) {
			if (o.length != 0) {
				if (o[0] instanceof SpaceManager) {
					sharedObjectAdded(o[0], null);
				}
			}
		}
	}

	public void stop() {
		synchronized (listeners) {
			listeners.clear();
		}
		// remove me as AALSpaceListener
		synchronized (this) {
			if (theAALSpaceManager != null) {
				theAALSpaceManager.removeSpaceListener(this);
			}
		}
		// and remove this object
		instance = null;
	}

	public void spaceJoined(SpaceDescriptor spaceDescriptor) {
		synchronized (listeners) {
			for (SpaceListener l : listeners) {
				l.spaceJoined(spaceDescriptor);
			}
		}
	}

	public void spaceLost(SpaceDescriptor spaceDescriptor) {
		synchronized (listeners) {
			for (SpaceListener l : listeners) {
				l.spaceLost(spaceDescriptor);
			}
		}
	}

	public void peerJoined(PeerCard peer) {
		synchronized (listeners) {
			for (SpaceListener l : listeners) {
				l.peerJoined(peer);
			}
		}
	}

	public void peerLost(PeerCard peer) {
		synchronized (listeners) {
			for (SpaceListener l : listeners) {
				l.peerLost(peer);
			}
		}
	}

	public void spaceStatusChanged(SpaceStatus status) {
		// not needed
	}

	/**
	 * Get all peers, including this peer.
	 * 
	 * @return
	 */
	public List<PeerCard> getPeers() {
		List<PeerCard> ret = new ArrayList<PeerCard>();

		Map<String, PeerCard> peers = theAALSpaceManager.getPeers();
		if (peers != null) {
			for (PeerCard pc : peers.values()) {
				ret.add(pc);
			}
		}
		ret.add(theAALSpaceManager.getMyPeerCard());
		return ret;
	}

	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		if (sharedObj instanceof SpaceManager) {
			synchronized (this) {
				theAALSpaceManager = (SpaceManager) sharedObj;
				theAALSpaceManager.addSpaceListener(this);
			}
		}
	}

	public void sharedObjectRemoved(Object removeHook) {
		if (removeHook instanceof SpaceManager) {
			synchronized (this) {
				theAALSpaceManager = null;
			}
		}
	}
}
