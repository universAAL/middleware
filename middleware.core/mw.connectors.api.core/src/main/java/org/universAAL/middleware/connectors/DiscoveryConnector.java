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
package org.universAAL.middleware.connectors;

import java.util.Dictionary;
import java.util.List;

import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.interfaces.space.SpaceCard;

/**
 * Interface for the discovery connector.
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public interface DiscoveryConnector extends Connector {

	/**
	 * Discovers all the Space matching agains the filter
	 *
	 * @param filter
	 * @return
	 */
	public List<SpaceCard> findSpace(Dictionary<String, String> filters) throws DiscoveryConnectorException;

	/**
	 * Discovers all the Space without a filter
	 *
	 * @return
	 */
	public List<SpaceCard> findSpace() throws DiscoveryConnectorException;

	/**
	 * Announce the existence of a Space
	 *
	 * @param card
	 */
	public void announceSpace(SpaceCard spaceCard) throws DiscoveryConnectorException;

	/**
	 * De-register a Space
	 *
	 * @param spaceCard
	 * @throws DiscoveryConnectorException
	 */
	public void deregisterSpace(SpaceCard spaceCard) throws DiscoveryConnectorException;

	public String getSDPPRotocol();

	public void addSpaceListener(ServiceListener listener);

	public void removeSpaceListener(ServiceListener listener);

}
