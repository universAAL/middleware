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

    public SpaceListener() {
    }

    public void start() {
	// get AAL Space Manager to register this listener
	Object o = DistributedMWManagerImpl.context.getContainer()
		.fetchSharedObject(DistributedMWManagerImpl.context,
			new Object[] { AALSpaceManager.class.getName() }, this);
	if (o instanceof AALSpaceManager) {
	    sharedObjectAdded(o, null);
	}
    }

    public void stop() {
	// remove me as AALSpaceListener
	synchronized (this) {
	    if (theAALSpaceManager != null) {
		theAALSpaceManager.removeAALSpaceListener(this);
	    }
	}
    }

    public void aalSpaceJoined(AALSpaceDescriptor spaceDescriptor) {
    }

    public void aalSpaceLost(AALSpaceDescriptor spaceDescriptor) {
    }

    public void newPeerJoined(PeerCard peer) {
    }

    public void peerLost(PeerCard peer) {
    }

    public void aalSpaceStatusChanged(AALSpaceStatus status) {
	// not needed
    }

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
