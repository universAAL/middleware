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

package org.universAAL.middleware.managers.api;


import java.io.Serializable;
import java.util.Map;

import org.universAAL.middleware.interfaces.PeerCard;

/**
 * The result of searching Peer by means of a set of {@link Map} containg pairs of < {@link String}, {@link Object} >
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public interface MatchingResult {

	/**
	 * 
	 * @return an array of {@link PeerCard} representing the peers whom matches the search
	 */
	public PeerCard[] getPeers();
	
	/**
	 * 
	 * @param peer the that we are asking the attribute for
	 * @return a {@link Map} with the current value of the all the attribute that were searched
	 */
	public Map<String, Serializable> getPeerAttribute(PeerCard peer);
}
