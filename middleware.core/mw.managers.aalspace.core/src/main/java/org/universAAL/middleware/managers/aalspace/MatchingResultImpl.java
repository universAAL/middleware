/*

        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

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
package org.universAAL.middleware.managers.aalspace;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.api.MatchingResult;

/**
 * The implementation of the {@link MatchingResult} interface
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class MatchingResultImpl implements MatchingResult {

	final private Map<PeerCard, Map<String, Serializable>> peerMap;
	private PeerCard[] peers;

	public MatchingResultImpl(Map<PeerCard, Map<String, Serializable>> data) {
		this.peerMap = data;
	}

	public PeerCard[] getPeers() {
		if (peers == null) {
			Set<PeerCard> keys = peerMap.keySet();
			peers = keys.toArray(new PeerCard[] {});
		}
		return peers;
	}

	public Map<String, Serializable> getPeerAttribute(PeerCard peer) {
		Map<String, Serializable> attributes = peerMap.get(peer);
		return attributes;
	}
}
