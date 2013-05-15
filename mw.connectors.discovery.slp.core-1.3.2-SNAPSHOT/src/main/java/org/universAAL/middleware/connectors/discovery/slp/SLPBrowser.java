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
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import ch.ethz.iks.slp.Locator;
import ch.ethz.iks.slp.ServiceLocationEnumeration;
import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;

/**
 * This thread periodically browses the SLP network in order to find all the
 * AALSpaces registered
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class SLPBrowser implements Runnable {
    private Locator locator;
    private String aalSpaceServiceType;
    private String filter;
    private ModuleContext context;
    private List<ServiceListener> listeners;
    private boolean stop = false;
    private static int MAX_RETRY = 3;
    private Set<AALSpaceCard> aalSpaces;

    public SLPBrowser(Locator locator, String aalSpaceServiceType,
	    String filter, ModuleContext context,
	    List<ServiceListener> listeners) {
	this.locator = locator;
	this.aalSpaceServiceType = aalSpaceServiceType;
	this.filter = filter;
	this.listeners = listeners;
	this.context = context;
    }

    public void addListener(ServiceListener listener) {
	this.listeners.add(listener);

	LogUtils.logDebug(context, SLPBrowser.class, "SLPBrowser",
		new Object[] { "New listener added!" }, null);
    }

    public void removeListener(ServiceListener listener) {
	this.listeners.remove(listener);
	LogUtils.logDebug(context, SLPBrowser.class, "SLPBrowser",
		new Object[] { "Listener removed!" }, null);
    }

    public void setStop(boolean stop) {
	this.stop = stop;
    }

    public boolean isStop() {
	return this.stop;
    }

    public void run() {
	aalSpaces = new HashSet<AALSpaceCard>();
	ServiceLocationEnumeration slenum = null;
	ServiceLocationEnumeration attribs = null;
	if (!stop) {
	    try {
		slenum = locator.findServices(new ServiceType(
			aalSpaceServiceType), null, filter);
		while (slenum.hasMoreElements()) {
		    ServiceURL serviceURL = (ServiceURL) slenum.next();
		    attribs = locator.findAttributes(serviceURL, null,
			    AALSpaceCard.getSpaceAttributes());
		    // FIX JSLP sometimes returns null attributes

		    // attribs = locator.findAttributes(new ServiceType(
		    // aalSpaceServiceType), null, AALSpaceCard
		    // .getSpaceAttributes());
		    if (attribs != null) {
			LogUtils.logTrace(
				context,
				SLPBrowser.class,
				"SLPBrowser",
				new Object[] { "Unmarshalling AALSpace attributes..." },
				null);
			AALSpaceCard spaceCard = new AALSpaceCard(
				SLPDiscoveryConnector
					.unmarshalServiceAttributes(attribs));
			spaceCard
				.setPeeringChannelName("mw.modules.aalspace.osgi");
			spaceCard.setRetry(MAX_RETRY);
			if (spaceCard.getCoordinatorID() != null) {
			    aalSpaces.add(spaceCard);
			    LogUtils.logTrace(
				    context,
				    SLPBrowser.class,
				    "SLPBrowser",
				    new Object[] { "AALSpace attributes unmarshalled" },
				    null);
			}
		    }
		}
		// Calling all the ServiceListeners

		for (ServiceListener listener : listeners) {
		    LogUtils.logTrace(
			    context,
			    SLPBrowser.class,
			    "SLPBrowser",
			    new Object[] { "Calling the AALSpaceModule listeners..." },
			    null);
		    listener.newAALSpacesFound(aalSpaces);
		}

	    } catch (Exception e) {
		LogUtils.logError(
			context,
			SLPBrowser.class,
			"SLPBrowser",
			new Object[] { "Error during AALSpace search: "
				+ e.toString() }, null);
	    }
	}
    }

}
