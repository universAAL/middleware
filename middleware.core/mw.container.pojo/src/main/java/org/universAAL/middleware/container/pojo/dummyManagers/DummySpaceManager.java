/*******************************************************************************
 * Copyright 2017 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.container.pojo.dummyManagers;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.managers.api.MatchingResult;
import org.universAAL.middleware.managers.api.SpaceListener;
import org.universAAL.middleware.managers.api.SpaceManager;

/**
 * @author amedrano
 *
 */
public class DummySpaceManager implements SpaceManager {
	/**
	 * 
	 */
	private final PeerCard myCard;

	/**
	 * @param myCard
	 */
	public DummySpaceManager(PeerCard myCard) {
		this.myCard = myCard;
	}

	public void dispose() {
	}

	public boolean init() {
		return false;
	}

	public void loadConfigurations(Dictionary arg0) {
	}

	public void addSpaceListener(SpaceListener arg0) {
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

	public Set<SpaceCard> getSpaces() {
		return null;
	}

	public Map<String, SpaceDescriptor> getManagedSpaces() {
		return null;
	}

	public MatchingResult getMatchingPeers(Map<String, Serializable> arg0) {
		return null;
	}

	public PeerCard getMyPeerCard() {
		return myCard;
	}

	public Map<String, Serializable> getPeerAttributes(List<String> arg0,
			PeerCard arg1) {
		return null;
	}

	public Map<String, PeerCard> getPeers() {
		HashMap map = new HashMap();
		map.put(myCard.getPeerID(), myCard);
		return map;
	}

	public void join(SpaceCard arg0) {
	}

	public void leaveSpace(SpaceDescriptor arg0) {
	}

	public void removeSpaceListener(SpaceListener arg0) {
	}
}