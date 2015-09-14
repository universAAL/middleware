package org.universAAL.middleware.managers.distributedmw.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;

/**
 * 
 * @author Carsten Stockloew
 * 
 */
public class SpaceListener implements AALSpaceListener, SharedObjectListener {

    private AALSpaceManager theAALSpaceManager = null;

    // the singleton instance
    private static SpaceListener instance = null;

    private List<AALSpaceListener> listeners = new ArrayList<AALSpaceListener>();

    private SpaceListener() {
    }

    public static SpaceListener getInstance() {
	if (instance == null) {
	    instance = new SpaceListener();
	    instance.start();
	}
	return instance;
    }

    public void register(AALSpaceListener l) {
	synchronized (listeners) {
	    listeners.add(l);
	}
    }

    public void start() {
	// get AAL Space Manager to register this listener
	Object[] o = DistributedMWManagerImpl.context.getContainer()
		.fetchSharedObject(DistributedMWManagerImpl.context,
			new Object[] { AALSpaceManager.class.getName() }, this);
	if (o != null) {
	    if (o.length != 0) {
		if (o[0] instanceof AALSpaceManager) {
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
		theAALSpaceManager.removeAALSpaceListener(this);
	    }
	}
	// and remove this object
	instance = null;
    }

    public void aalSpaceJoined(AALSpaceDescriptor spaceDescriptor) {
	synchronized (listeners) {
	    for (AALSpaceListener l : listeners) {
		l.aalSpaceJoined(spaceDescriptor);
	    }
	}
    }

    public void aalSpaceLost(AALSpaceDescriptor spaceDescriptor) {
	synchronized (listeners) {
	    for (AALSpaceListener l : listeners) {
		l.aalSpaceLost(spaceDescriptor);
	    }
	}
    }

    public void newPeerJoined(PeerCard peer) {
	synchronized (listeners) {
	    for (AALSpaceListener l : listeners) {
		l.newPeerJoined(peer);
	    }
	}
    }

    public void peerLost(PeerCard peer) {
	synchronized (listeners) {
	    for (AALSpaceListener l : listeners) {
		l.peerLost(peer);
	    }
	}
    }

    public void aalSpaceStatusChanged(AALSpaceStatus status) {
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
	if (sharedObj instanceof AALSpaceManager) {
	    synchronized (this) {
		theAALSpaceManager = (AALSpaceManager) sharedObj;
		theAALSpaceManager.addAALSpaceListener(this);
	    }
	}
    }

    public void sharedObjectRemoved(Object removeHook) {
	if (removeHook instanceof AALSpaceManager) {
	    synchronized (this) {
		theAALSpaceManager = null;
	    }
	}
    }
}
