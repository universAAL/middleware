package org.universAAL.middleware.connectors.discovery.fake.core;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.Consts;
import org.universAAL.middleware.interfaces.aalspace.model.IAALSpace;
import org.universAAL.middleware.managers.api.AALSpaceManager;

/**
 * This class implements the AALSpace discovery connector based on jGroups
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 **/

public class FakeDiscoveryConnector implements DiscoveryConnector,
		SharedObjectListener {

	private ModuleContext context;
	private String name, description, provider, version;
	private AALSpaceManager aalSpaceManager;
	private List<ServiceListener> serviceListeners = new ArrayList<ServiceListener>();
	FakeBrower fakeBrowser;

	private boolean initialized = false;

	public FakeDiscoveryConnector(ModuleContext context) {
		this.context = context;
		fakeBrowser = new FakeBrower();
		Thread fakeThread = new Thread(fakeBrowser);
		fakeThread.start();
		init();
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	public String getDescription() {
		return description;
	}

	public String getProvider() {
		return this.provider;
	}

	public void loadConfigurations(Dictionary configurations) {
		this.name = (String) configurations
				.get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_NAME);
		this.version = (String) configurations
				.get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_VERSION);
		this.description = (String) configurations
				.get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_DESCRIPTION);
		this.provider = (String) configurations
				.get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_PROVIDER);

	}

	public boolean init() {
		if (!initialized) {
			LogUtils.logDebug(
					context,
					FakeDiscoveryConnector.class,
					"FakeDiscoveryConnector",
					new Object[] { "Initializing the FakeDiscoveryConnector..." },
					null);

			LogUtils.logDebug(context, FakeDiscoveryConnector.class,
					"FakeDiscoveryConnector",
					new Object[] { "fetching the FakeDiscoveryConnector..." },
					null);
			Object[] aalManagers = context.getContainer()
					.fetchSharedObject(
							context,
							new Object[] { AALSpaceManager.class.getName()
									.toString() }, this);
			if (aalManagers != null && aalManagers.length > 0) {
				aalSpaceManager = (AALSpaceManager) aalManagers[0];
				initialized = true;
			}
		}
		return initialized;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		if (sharedObj instanceof AALSpaceManager) {
			LogUtils.logDebug(context, FakeDiscoveryConnector.class,
					"FakeDiscoveryConnector",
					new Object[] { "AALSpaceManager service added" }, null);
			aalSpaceManager = (AALSpaceManager) sharedObj;

		}
	}

	public void sharedObjectRemoved(Object removeHook) {
		// TODO Auto-generated method stub

	}

	public List<AALSpaceCard> findAALSpace(Dictionary<String, String> filters)
			throws DiscoveryConnectorException {
		// TODO Auto-generated method stub
		return findAALSpace();
	}

	public List<AALSpaceCard> findAALSpace() throws DiscoveryConnectorException {
		ArrayList<AALSpaceCard> aalSpaces = new ArrayList<AALSpaceCard>();
		if (init()) {
			AALSpaceCard aalspaceCard = new AALSpaceCard(
					getAALSpaceProperties(aalSpaceManager
							.getAALSpaceDefaultConfigurartion(),aalSpaceManager));
			aalSpaces.add(aalspaceCard);
		}
		return aalSpaces;
	}

	public void announceAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		// TODO Auto-generated method stub

	}

	public void deregisterAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		// TODO Auto-generated method stub

	}

	public String getSDPPRotocol() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addAALSpaceListener(ServiceListener listener) {
		if (listener != null && !serviceListeners.contains(listener)) {
			this.serviceListeners.add(listener);

			LogUtils.logDebug(context, FakeDiscoveryConnector.class,
					"FakeDiscoveryConnector",
					new Object[] { "New AALSpaceListener" }, null);
		}
	}

	public void removeAALSpaceListener(ServiceListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method collects in a dictionary the properties associated with a new
	 * AAL Space in order to announce them. The properties are read from the
	 * data structure AalSpace. The properties added to the AALSpace card are
	 * the name,id,description and coordinator ID and the peering channel
	 * serialized as XML string
	 * 
	 * @param space
	 * @return
	 */
	public static Dictionary<String, String> getAALSpaceProperties(
			IAALSpace space, AALSpaceManager aalSpaceManager) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		try {
			// general purpose properties
			properties.put(Consts.AALSPaceName, space.getSpaceDescriptor()
					.getSpaceName());
			properties.put(Consts.AALSPaceID, space.getSpaceDescriptor()
					.getSpaceId());
			properties.put(Consts.AALSPaceDescription, space
					.getSpaceDescriptor().getSpaceDescription());
			
			String coordinatorID = space.getSpaceDescriptor()
					.getSpaceCoordinator();
			if(coordinatorID != null && !coordinatorID.isEmpty())
				properties.put(Consts.AALSpaceCoordinator, coordinatorID);
			else if(aalSpaceManager != null)
				properties.put(Consts.AALSpaceCoordinator, aalSpaceManager.getMyPeerCard().getPeerID());
				
			properties
					.put(Consts.AALSpacePeeringChannelURL, space
							.getPeeringChannel().getChannelDescriptor()
							.getChannelURL());
			properties.put(Consts.AALSpacePeeringChannelName, space
					.getPeeringChannel().getChannelDescriptor()
					.getChannelName());
			properties.put(Consts.AALSPaceProfile, space.getSpaceDescriptor()
					.getProfile());

		} catch (NullPointerException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
		return properties;

	}

	class FakeBrower implements Runnable {

		boolean stop = false;

		public FakeBrower() {

		}

		public void run() {
			while (!stop) {
				Set<AALSpaceCard> aalSpaces = new HashSet<AALSpaceCard>();
				if (aalSpaceManager != null && serviceListeners != null) {
					AALSpaceCard aalspaceCard = new AALSpaceCard(
							getAALSpaceProperties(aalSpaceManager
									.getAALSpaceDefaultConfigurartion(),aalSpaceManager));
					aalSpaces.add(aalspaceCard);
					for (ServiceListener listener : serviceListeners) {
						listener.newAALSpacesFound(aalSpaces);
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		public void setStop() {
			stop = true;
		}

	}

}
