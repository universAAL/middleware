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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.discovery.slp.util.Consts;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorErrorCodes;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;

import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.Locator;
import ch.ethz.iks.slp.ServiceLocationEnumeration;
import ch.ethz.iks.slp.ServiceLocationException;
import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;

/**
 * Implementation of the SLP Discovery Connector
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 */
public class SLPDiscoveryConnector implements DiscoveryConnector,
	SharedObjectListener {

    // SLP stuff
    private Locator locator;
    private Advertiser advertiser;
    // Connector properties
    private String aalSpaceServiceType = "service:aalspace";
    private String name;
    private String version;
    private String description;
    private String provider;
    private String sdpProtocol;
    private ModuleContext context;
    private int initDelay;
    private int delay;
    private boolean browser;
    // true if the connector have been successfully initialized
    private boolean initalized = false;
    private List<ServiceListener> listeners;
    private SLPBrowser slpBrowser;
    private final ScheduledExecutorService scheduler = Executors
	    .newScheduledThreadPool(10);
    private final Object DISCOVERY_MUTEX = new Object();
    private static final int MAX_RETRY = 3;

    public SLPDiscoveryConnector(ModuleContext context) {
	this.context = context;
	this.listeners = new ArrayList<ServiceListener>();
    }

    public synchronized boolean init() {
	if (!initalized) {

	    LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "Initializing SLP Connector..." }, null);

	    if (slpBrowser == null && browser) {
		LogUtils.logDebug(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "Initializing SLP Browser..." }, null);
		slpBrowser = new SLPBrowser(getSLPLocator(),
			aalSpaceServiceType, Consts.SEARCH_ALL, context,
			listeners);
		scheduler.scheduleAtFixedRate(slpBrowser, initDelay, delay,
			TimeUnit.SECONDS);
		LogUtils.logDebug(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "SLP Browser initialized!." }, null);
	    } else if (slpBrowser != null && slpBrowser.isStop()) {
		slpBrowser.setStop(false);
		scheduler.scheduleAtFixedRate(slpBrowser, initDelay, delay,
			TimeUnit.SECONDS);
	    }

	    LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "SLP Connector initialized" }, null);

	    initalized = true;
	}
	return initalized;
    }

    private Advertiser getSLPAdvertiser() {
	synchronized (DISCOVERY_MUTEX) {
	    if (this.advertiser != null) {
		return this.advertiser;
	    }
	    LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "Fetching the SLP Advertiser..." }, null);
	    Object[] advertisers = context.getContainer().fetchSharedObject(
		    context, new Object[] { "ch.ethz.iks.slp.Advertiser" },
		    this);
	    if (advertisers != null) {
		LogUtils.logDebug(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "SLP Advertiser found" }, null);
		if (advertisers[0] instanceof Advertiser)
		    this.advertiser = (Advertiser) advertisers[0];
		return advertiser;
	    }
	    LogUtils.logWarn(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "SLP Locator and Advertiser not found!" },
		    null);
	    return null;
	}
    }

    private Locator getSLPLocator() {
	synchronized (DISCOVERY_MUTEX) {
	    if (this.locator != null) {
		return locator;
	    }
	    LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "Fetching the SLP Locator..." }, null);
	    Object[] locators = context.getContainer().fetchSharedObject(
		    context, new Object[] { "ch.ethz.iks.slp.Locator" }, this);
	    if (locators != null) {
		LogUtils.logDebug(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "SLP Locator found" }, null);
		if (locators[0] instanceof Locator)
		    this.locator = (Locator) locators[0];
		return locator;
	    }
	    LogUtils.logWarn(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "SLP Locator not found!" }, null);
	    return null;
	}
    }

    public void dispose() {
	context.getContainer().removeSharedObjectListener(this);
	removeAALSpaces();
	if (slpBrowser != null) {
	    slpBrowser.setStop(true);
	}
	List<Runnable> activeTasks = scheduler.shutdownNow();
	slpBrowser = null;

    }

    private void removeAALSpaces() {
	List<AALSpaceCard> spaces = findAALSpace();
	if (spaces != null && spaces.size() > 0) {
	    for (AALSpaceCard space : spaces) {
		deregisterAALSpace(space);
	    }
	}
    }

    /**
     * This method implements how to announce an AALSpace by adopting the SLP
     * protocol
     */
    public void announceAALSpace(AALSpaceCard card)
	    throws DiscoveryConnectorException {
	LogUtils.logTrace(context, SLPDiscoveryConnector.class,
		"SLPDiscoveryConnector",
		new Object[] { "Announcing the AALSpace..." }, null);
	if (init()) {
	    try {
		getSLPAdvertiser().register(
			new ServiceURL(aalSpaceServiceType + "://"
				+ card.getCoordinatorID(),
				card.getAalSpaceLifeTime()),
			card.serializeCard());
		LogUtils.logTrace(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "AALSpace announced" }, null);
	    } catch (ServiceLocationException e) {
		LogUtils.logError(
			context,
			SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "Error during AALSpace announce for space: "
				+ card.serializeCard().toString()
				+ " -->"
				+ e.toString() }, null);
		throw new DiscoveryConnectorException(
			DiscoveryConnectorErrorCodes.ANNOUNCE_ERROR,
			e.toString());
	    }
	} else {
	    LogUtils.logWarn(
		    context,
		    SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "SLPDiscoveryConnector is not initialized!" },
		    null);
	}
    }

    public void deregisterAALSpace(AALSpaceCard spaceCard)
	    throws DiscoveryConnectorException {
	LogUtils.logDebug(
		context,
		SLPDiscoveryConnector.class,
		"SLPDiscoveryConnector",
		new Object[] { "De-Registering the AALSpace: "
			+ spaceCard.toString() + "..." }, null);
	if (init()) {
	    try {
		getSLPAdvertiser().deregister(
			new ServiceURL(aalSpaceServiceType + "://"
				+ spaceCard.getCoordinatorID(), spaceCard
				.getAalSpaceLifeTime()));
		LogUtils.logDebug(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "AALSpace de-registered" }, null);
	    } catch (ServiceLocationException e) {
		LogUtils.logError(
			context,
			SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "Error during AALSpace de-registering for space: "
				+ spaceCard.toString() + " -->" + e.toString() },
			null);
		throw new DiscoveryConnectorException(
			DiscoveryConnectorErrorCodes.DEREGISTER_ERROR,
			e.toString());
	    }
	} else {
	    LogUtils.logWarn(
		    context,
		    SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "SLPDiscoveryConnector is not initialized!" },
		    null);
	}

    }

    public List<AALSpaceCard> findAALSpace() throws DiscoveryConnectorException {
	return this.findAALSpace(null);
    }

    /**
     * This method finds an AALSpace by using the specified filter in the SLP
     * network
     */
    public List<AALSpaceCard> findAALSpace(Dictionary<String, String> filters)
	    throws DiscoveryConnectorException {
	List<AALSpaceCard> spaces = new ArrayList<AALSpaceCard>();

	if (init()) {
	    String filter = formatFilter(filters);
	    try {
		ServiceLocationEnumeration slenum = null;
		Locator l = getSLPLocator();
		if (l == null) {
		    return spaces;
		} else {
		    slenum = l.findServices(
			    new ServiceType(aalSpaceServiceType), null, filter);
		}
		while (slenum.hasMoreElements()) {
		    ServiceURL serviceURL = (ServiceURL) slenum.next();
		    ServiceLocationEnumeration attribs = getSLPLocator()
			    .findAttributes(serviceURL, null,
				    AALSpaceCard.getSpaceAttributes());
		    if (attribs != null) {
			LogUtils.logDebug(
				context,
				SLPDiscoveryConnector.class,
				"SLPDiscoveryConnector",
				new Object[] { "Unmarshalling AALSpace attributes..." },
				null);
			AALSpaceCard card = new AALSpaceCard(
				unmarshalServiceAttributes(attribs));
			// hack
			card.setPeeringChannelName("mw.modules.aalspace.osgi");
			card.setRetry(MAX_RETRY);
			if (card.getCoordinatorID() == null) {
			    LogUtils.logDebug(
				    context,
				    SLPDiscoveryConnector.class,
				    "SLPDiscoveryConnector",
				    new Object[] { "Not a valid AALSpaceCard" },
				    null);
			} else {
			    spaces.add(card);
			    LogUtils.logDebug(
				    context,
				    SLPDiscoveryConnector.class,
				    "SLPDiscoveryConnector",
				    new Object[] { "AALSpace attributes unmarshalled" },
				    null);
			}
		    }
		}
	    } catch (IllegalArgumentException e) {
		LogUtils.logError(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "Error during AAL Space search: "
				+ filter + " -->" + e.toString() }, null);
		throw new DiscoveryConnectorException(
			DiscoveryConnectorErrorCodes.SEARCH_ERROR, e.toString());
	    } catch (ServiceLocationException e) {
		LogUtils.logError(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "Error during AAL Space search: "
				+ filter + " -->" + e.toString() }, null);
		throw new DiscoveryConnectorException(
			DiscoveryConnectorErrorCodes.SEARCH_ERROR, e.toString());
	    } catch (DiscoveryConnectorException e) {
		throw e;
	    }
	} else {
	    LogUtils.logWarn(
		    context,
		    SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "SLPDiscoveryConnector is not initialized!" },
		    null);
	}
	return spaces;
    }

    private String formatFilter(Dictionary<String, String> filters) {
	String filter = "";
	if (filters == null || filters.isEmpty()) {
	    LogUtils.logWarn(
		    context,
		    SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "The set of filters is empty, returning the default one" },
		    null);
	    filter = "(" + Consts.SEARCH_ALL + ")";
	    return filter;
	}
	LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		"SLPDiscoveryConnector",
		new Object[] { "Building the SLP filter..." }, null);
	Enumeration<String> keys = filters.keys();

	while (keys.hasMoreElements()) {
	    String theKey = keys.nextElement();
	    filter = "(" + theKey + "=" + filters.get(theKey) + ")";
	}
	if (filters.size() > 1) {
	    filter = "(&" + filter + ")";
	}
	LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		"SLPDiscoveryConnector", new Object[] { "The SLP filter is : "
			+ filter }, null);
	return filter;
    }

    /**
     * This method converts the list of attributes as a Dictionary of
     * properties.
     * 
     * @param attributes
     * @return
     */
    protected static Dictionary unmarshalServiceAttributes(
	    ServiceLocationEnumeration attributes)
	    throws DiscoveryConnectorException {

	String nextAttrib = "";
	Dictionary attribs = new Properties();
	while (attributes.hasMoreElements()) {
	    try {
		nextAttrib = (String) attributes.next();
		if (nextAttrib != null) {
		    // remove the stating and the ending character: '(' and ')'
		    if (nextAttrib.startsWith("(") && nextAttrib.endsWith(")"))
			nextAttrib = nextAttrib.substring(1,
				nextAttrib.length() - 1);
		    String[] tokens = nextAttrib.split("=");
		    if (tokens.length > 1)
			attribs.put(tokens[0], tokens[1]);
		}
	    } catch (Exception e) {
		throw new DiscoveryConnectorException(
			DiscoveryConnectorErrorCodes.AALSPACE_UNMASHALLING_ERROR,
			e.toString());
	    }
	}
	return attribs;

    }

    public void loadConfigurations(Dictionary properties) {
	LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		"SLPDiscoveryConnector",
		new Object[] { "updating SLP Connector properties" }, null);
	if (properties == null) {
	    LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "SLP Connector properties are null" }, null);
	    return;
	}
	try {
	    this.aalSpaceServiceType = (String) properties
		    .get(Consts.AALSPaceServiceTypeName);
	    this.sdpProtocol = (String) properties.get(Consts.SDPProtocols);
	    this.name = (String) properties
		    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_NAME);
	    this.version = (String) properties
		    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_VERSION);
	    this.description = (String) properties
		    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_DESCRIPTION);
	    this.provider = (String) properties
		    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_PROVIDER);
	    this.initDelay = Integer.parseInt((String) properties
		    .get(Consts.SLP_INIT_DELAY_SCAN));
	    this.delay = Integer.parseInt((String) properties
		    .get(Consts.SLP_PERIOD_SCAN));
	    this.browser = Boolean.parseBoolean((String) properties
		    .get(Consts.BROWSE_SLP_NETWORK));
	} catch (NumberFormatException e) {
	    LogUtils.logError(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "Error during SLP properties update" }, null);
	} catch (NullPointerException e) {
	    LogUtils.logError(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "Error during SLP properties update" }, null);
	} catch (Exception e) {
	    LogUtils.logError(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "Error during SLP properties update" }, null);
	}
	LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		"SLPDiscoveryConnector",
		new Object[] { "SLP Connector properties updated" }, null);
    }

    public String getDescription() {
	return description;
    }

    public String getName() {
	return this.name;
    }

    public String getProvider() {
	return provider;
    }

    public String getVersion() {
	return this.version;
    }

    public String getSDPPRotocol() {
	return sdpProtocol;
    }

    /**
     * This method prints the Connector properties: name, version, description
     * and provider
     * 
     * @return
     */
    public String toString() {
	return this.name + "-" + this.description + "-" + this.provider + "-"
		+ this.version;
    }

    public void addAALSpaceListener(ServiceListener listener) {
	if (listener != null && !listeners.contains(listener)) {
	    this.listeners.add(listener);
	    if (slpBrowser != null)
		slpBrowser.addListener(listener);

	    LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "New AALSpaceListener" }, null);
	}

    }

    public void removeAALSpaceListener(ServiceListener listener) {
	if (listener != null) {
	    this.listeners.remove(listener);
	    if (slpBrowser != null)
		slpBrowser.removeListener(listener);
	    LogUtils.logDebug(context, SLPDiscoveryConnector.class,
		    "SLPDiscoveryConnector",
		    new Object[] { "AALSpaceListener removed" }, null);
	}
    }

    public void sharedObjectAdded(Object sharedObj, Object removeHook) {
	synchronized (DISCOVERY_MUTEX) {

	    if (sharedObj instanceof Locator) {
		LogUtils.logDebug(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "New SLP Locator Service added" }, null);
		this.locator = (Locator) sharedObj;
	    }
	    if (sharedObj instanceof Advertiser) {
		LogUtils.logDebug(context, SLPDiscoveryConnector.class,
			"SLPDiscoveryConnector",
			new Object[] { "New SLP Advertiser Service added" },
			null);
		this.advertiser = (Advertiser) sharedObj;
	    }
	}
    }

    public void sharedObjectRemoved(Object removeHook) {
	synchronized (DISCOVERY_MUTEX) {
	    if (removeHook instanceof Locator) {
		this.locator = null;
		this.slpBrowser.setStop(true);
		this.initalized = false;
	    } else if (removeHook instanceof Advertiser) {
		this.advertiser = null;
		this.slpBrowser.setStop(true);
		this.initalized = false;
	    }
	}
    }

}
