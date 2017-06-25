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
package org.universAAL.middleware.connectors.discovery.slp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.space.SpaceCard;

import ch.ethz.iks.slp.Locator;
import ch.ethz.iks.slp.ServiceLocationEnumeration;
import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;

/**
 * This thread periodically browses the SLP network in order to find all the
 * Spaces registered
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class SLPBrowser implements Runnable {
	private Locator locator;
	private String spaceServiceType;
	private String filter;
	private ModuleContext context;
	private List<ServiceListener> listeners;
	private boolean stop = false;
	private static int MAX_RETRY = 3;
	private Set<SpaceCard> spaces;

	public SLPBrowser(Locator locator, String spaceServiceType, String filter, ModuleContext context,
			List<ServiceListener> listeners) {
		this.locator = locator;
		this.spaceServiceType = spaceServiceType;
		this.filter = filter;
		this.listeners = listeners;
		this.context = context;
	}

	public void addListener(ServiceListener listener) {
		this.listeners.add(listener);

		LogUtils.logDebug(context, SLPBrowser.class, "addListener", new Object[] { "New listener added!" }, null);
	}

	public void removeListener(ServiceListener listener) {
		this.listeners.remove(listener);
		LogUtils.logDebug(context, SLPBrowser.class, "removeListener", new Object[] { "Listener removed!" }, null);
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public boolean isStop() {
		return this.stop;
	}

	public void run() {
		spaces = new HashSet<SpaceCard>();
		ServiceLocationEnumeration slenum = null;
		ServiceLocationEnumeration attribs = null;
		if (!stop) {
			try {
				slenum = locator.findServices(new ServiceType(spaceServiceType), null, filter);
				while (slenum.hasMoreElements()) {
					ServiceURL serviceURL = (ServiceURL) slenum.next();
					attribs = locator.findAttributes(serviceURL, null, SpaceCard.getSpaceAttributes());
					// FIX JSLP sometimes returns null attributes

					// attribs = locator.findAttributes(new ServiceType(
					// spaceServiceType), null, SpaceCard
					// .getSpaceAttributes());
					if (attribs != null) {
						LogUtils.logTrace(context, SLPBrowser.class, "run",
								new Object[] { "Unmarshalling Space attributes..." }, null);
						SpaceCard spaceCard = new SpaceCard(
								SLPDiscoveryConnector.unmarshalServiceAttributes(attribs));
						// TODO: remove the static name of the channel name
						spaceCard.setPeeringChannelName("mw.modules.space.osgi");
						spaceCard.setRetry(MAX_RETRY);
						if (spaceCard.getCoordinatorID() != null) {
							spaces.add(spaceCard);
							LogUtils.logTrace(context, SLPBrowser.class, "run",
									new Object[] { "Space attributes unmarshalled" }, null);
						}
					}
				}
				// Calling all the ServiceListeners

				for (ServiceListener listener : listeners) {
					LogUtils.logTrace(context, SLPBrowser.class, "run",
							new Object[] { "Calling the SpaceModule listeners..." }, null);
					listener.newSpacesFound(spaces);
				}

			} catch (Exception e) {
				LogUtils.logError(context, SLPBrowser.class, "run",
						new Object[] { "Error during Space search: " + e.toString() }, e);
			}
		}
	}

}
