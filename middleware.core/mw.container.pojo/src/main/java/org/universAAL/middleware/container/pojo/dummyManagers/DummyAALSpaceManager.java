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
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.MatchingResult;

/**
 * @author amedrano
 *
 */
public class DummyAALSpaceManager implements AALSpaceManager {
	/**
	 * 
	 */
	private final PeerCard myCard;

	/**
	 * @param myCard
	 */
	public DummyAALSpaceManager(PeerCard myCard) {
		this.myCard = myCard;
	}

	public void dispose() {
	}

	public boolean init() {
		return false;
	}

	public void loadConfigurations(Dictionary arg0) {
	}

	public void addAALSpaceListener(AALSpaceListener arg0) {
	}

	public AALSpaceDescriptor getAALSpaceDescriptor() {
		return new AALSpaceDescriptor() {
			private static final long serialVersionUID = -7504183020450042989L;

			public AALSpaceCard getSpaceCard() {
				AALSpaceCard sc = new AALSpaceCard();
				sc.setSpaceID("TestSpaceID");
				return sc;
			}
		};
	}

	public Set<AALSpaceCard> getAALSpaces() {
		return null;
	}

	public Map<String, AALSpaceDescriptor> getManagedAALSpaces() {
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

	public void join(AALSpaceCard arg0) {
	}

	public void leaveAALSpace(AALSpaceDescriptor arg0) {
	}

	public void removeAALSpaceListener(AALSpaceListener arg0) {
	}
}