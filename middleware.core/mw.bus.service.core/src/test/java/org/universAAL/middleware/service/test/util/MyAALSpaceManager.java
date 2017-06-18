package org.universAAL.middleware.service.test.util;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.MatchingResult;
import org.universAAL.middleware.managers.api.SpaceManager;

/**
 * A fake AAL Space Manager for distributed unit tests. As this manager is used
 * by different instances of the service bus and normally we only have one
 * instance (multiple instances is necessary for simulating the distribution) we
 * have to make some weird hacks. The problem is that {@link AbstractBus} has
 * only one static reference to this manager.
 * 
 * @author cs
 * 
 */
public class MyAALSpaceManager implements SpaceManager {
	HashMap<String, PeerCard> mapCards;
	HashMap<AbstractBus, PeerCard> mapCardForBus;
	List<PeerCard> lstCards;
	int cnt = -1;

	MyAALSpaceManager(HashMap<String, PeerCard> mapCards, List<PeerCard> lstCards) {
		this.mapCards = mapCards;
		this.lstCards = lstCards;
	}

	public void dispose() {
	}

	public boolean init() {
		return false;
	}

	public void loadConfigurations(@SuppressWarnings("rawtypes") Dictionary arg0) {
	}

	public void addAALSpaceListener(AALSpaceListener arg0) {
	}

	public SpaceDescriptor getSpaceDescriptor() {
		return new SpaceDescriptor() {
			private static final long serialVersionUID = -7504183020450042989L;

			public SpaceCard getSpaceCard() {
				SpaceCard sc = new SpaceCard();
				sc.setSpaceID("TestSpaceID");
				return sc;
			}
		};
	}

	public Set<SpaceCard> getAALSpaces() {
		return null;
	}

	public Map<String, SpaceDescriptor> getManagedAALSpaces() {
		return null;
	}

	public MatchingResult getMatchingPeers(Map<String, Serializable> arg0) {
		return null;
	}

	public PeerCard getMyPeerCard() {
		// HACK: we assume that this method is called only by the bus and that
		// it is called exactly two times for a bus
		// System.out.println(" --- getMyPeerCard");
		cnt++;
		return lstCards.get(cnt / 2);
		// return null;
	}

	public Map<String, Serializable> getPeerAttributes(List<String> arg0, PeerCard arg1) {
		return null;
	}

	public Map<String, PeerCard> getPeers() {
		return mapCards;
	}

	public void join(SpaceCard arg0) {
	}

	public void leaveAALSpace(SpaceDescriptor arg0) {
	}

	public void removeAALSpaceListener(AALSpaceListener arg0) {
	}
}
