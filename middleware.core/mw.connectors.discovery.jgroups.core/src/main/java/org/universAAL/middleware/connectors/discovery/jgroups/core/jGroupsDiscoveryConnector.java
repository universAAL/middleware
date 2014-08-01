package org.universAAL.middleware.connectors.discovery.jgroups.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.xml.model.ChannelDescriptor;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.discovery.jgroups.core.jGroupsDiscoveryConnector;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorErrorCodes;
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
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:federico.volpini@isti.cnr.it">Federico Volpini</a>
 */
public class jGroupsDiscoveryConnector 
	implements DiscoveryConnector, SharedObjectListener, Receiver {

	private ModuleContext context;
	private JChannel discoveryChannel;
	private String name, description, provider, version;
	
	private boolean initialized = false;
	private List<ServiceListener> serviceListeners = new ArrayList<ServiceListener>();
	private AALSpaceManager aalSpaceManager;
	private ChannelDescriptor discoveryChannelDescriptor;
	
	public jGroupsDiscoveryConnector(ModuleContext context){
		this.context = context;
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
		// TODO Creo il canale e mi conetto al canale
		
		final String METHOD = "init";
		
		if (!initialized) {

			// LOG
			LogUtils.logDebug(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Initializing JGroups Discovery Connector..." }, null);

			Object[] aalManagers = context.getContainer()
					.fetchSharedObject(
							context,
							new Object[] { AALSpaceManager.class.getName()
									.toString() }, this);
			
			if (aalManagers != null && aalManagers.length > 0) {
				aalSpaceManager = (AALSpaceManager) aalManagers[0];
				discoveryChannelDescriptor = aalSpaceManager.getAALSpaceDefaultConfigurartion()
						.getDiscoveryChannel().getChannelDescriptor();
				
				if(configureChannel(discoveryChannelDescriptor)){
					// LOG
					LogUtils.logDebug(
						context, 
						jGroupsDiscoveryConnector.class,
						METHOD,
						new Object[] { "JGroups Discovery Connector initialized" }, null);
		
					initialized = true;
				}

			}
		}
		
		return initialized;
	}

	private boolean configureChannel(ChannelDescriptor discoveryChannelDescriptor) {
		
		final String METHOD = "configureChannel";
		
		try {
			discoveryChannel = new JChannel(discoveryChannelDescriptor.getChannelURL());
			discoveryChannel.setName(discoveryChannelDescriptor.getChannelName());
			discoveryChannel.connect(discoveryChannelDescriptor.getChannelName());
			//Imposto l'istanza di classe come Receiver dei messaggi
			discoveryChannel.setReceiver(this);
		} catch (Exception e) {
			// LOG
			LogUtils.logDebug(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Error initializing JGroups Discovery Connector: "+e.getMessage() }, null);
			return false;
		}
		return true;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		
		final String METHOD = "sharedObjectAdded";
		
		if (sharedObj instanceof AALSpaceManager) {
			// LOG
			LogUtils.logDebug(
					context, 
					jGroupsDiscoveryConnector.class,
					METHOD,
					new Object[] { "AALSpaceManager service added" }, null);
			aalSpaceManager = (AALSpaceManager) sharedObj;

		}
	}

	public void sharedObjectRemoved(Object removeHook) {
		// TODO Auto-generated method stub

	}

	public List<AALSpaceCard> findAALSpace(Dictionary<String, String> filters)
			throws DiscoveryConnectorException {
		// TODO Inviare messaggio in multicast con la query per l'AALSpace e il filtro
		Set<AALSpaceCard> aalSpaceCards = aalSpaceManager.getAALSpaces();
		
		List<AALSpaceCard> listOfAALSpaces = new ArrayList(aalSpaceCards);
		return listOfAALSpaces;
	}

	public List<AALSpaceCard> findAALSpace() throws DiscoveryConnectorException {
		return this.findAALSpace(null);
	}

	public void announceAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		// TODO Inviare messaggio in mutlicast per annunciare un nuovo AALSpace
		
		final String METHOD = "announceAALSpace";
		
		// LOG
		LogUtils.logTrace(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Announcing the AALSpace..." }, null);
		
		if (init()) {
			try {
				String serializedMessage = GsonParserBuilder.getInstance().toJson(spaceCard);
				discoveryChannel.send(null, serializedMessage);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Address getAddress(PeerCard card){
		
		String cardID = card.getPeerID();
		
		final String METHOD = "getAddress";
		
		Address dst = null;
        View discoveryChannelView = discoveryChannel.getView();
		
        if (discoveryChannelView == null) {
        	LogUtils.logTrace(
    				context, 
    		 		jGroupsDiscoveryConnector.class,
                    METHOD,
    		 		"Unable to get the View on the channel " + discoveryChannel.getClusterName()
                            + " We may not be connected to it");
            return null;
        }
       
        for (Address address : discoveryChannelView.getMembers()) {
            if (cardID.equals(discoveryChannel.getName(address))) {
                dst = address;
        		return dst;
        	}
        }
    	LogUtils.logTrace(
				context, 
		 		jGroupsDiscoveryConnector.class,
                METHOD,
		 		"Address of Peer "+card.toString()+" not found");
        return null;
	}

	public void deregisterAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		
		// TODO Inviare messaggio in mutlicast per cancellare un AALSpace esistente
		
		final String METHOD = "deregisterAALSpace";
		
		// LOG
		LogUtils.logDebug(
				context,
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "De-Registering the AALSpace: "
						+ spaceCard.toString() + "..." }, null);
		if (init()) {
			try {
				
			} catch (Exception e) {
				// LOG
				LogUtils.logError(
						context,
						jGroupsDiscoveryConnector.class,
						METHOD,
						new Object[] { "Unable to de-register space: "
								+ spaceCard.toString() + " --> " + e.toString() },
						null);
				throw new DiscoveryConnectorException(
						DiscoveryConnectorErrorCodes.DEREGISTER_ERROR,
						e.toString());
			}
		} else {
			// LOG
			LogUtils.logWarn(
					context,
					jGroupsDiscoveryConnector.class,
					METHOD,
					new Object[] { "JGroupDiscoveryConnector is not initialized!" },
					null);
		}
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
			IAALSpace space) {
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
			properties.put(Consts.AALSpaceCoordinator, coordinatorID);
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
	
	public String getSDPPRotocol() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addAALSpaceListener(ServiceListener listener) {
		
		final String METHOD = "addAALSpaceListener";
		
		if (listener != null && !serviceListeners.contains(listener)) {
			this.serviceListeners.add(listener);
			
			// LOG
			LogUtils.logDebug(context, 
					jGroupsDiscoveryConnector.class,
					METHOD,
					new Object[] { "New AALSpaceListener" }, null);
		}

	}

	public void removeAALSpaceListener(ServiceListener listener) {
		
		final String METHOD = "removeAALSpaceListener";
		
		if (listener != null && !serviceListeners.contains(listener)) {
			this.serviceListeners.remove(listener);
			
			// LOG
			LogUtils.logDebug(context, 
					jGroupsDiscoveryConnector.class,
					METHOD,
					new Object[] { "AALSpaceListener "+listener.toString()+" removed" }, null);
		}
	}
	
	/**
	 * TODO: inviare messaggio in unicast al richiedente con l'AALSpaceeCard .
	 * @param query
	 * @return
	 */
	private boolean asnwerToAQuery(String query){
		return true;
	}
	
	public void receive(Message msg) {
		// TODO Auto-generated method stub
	}

	public void getState(OutputStream output) throws Exception {
		// TODO Auto-generated method stub
		
	}
	public void setState(InputStream input) throws Exception {
		// TODO Auto-generated method stub
		
	}
	public void viewAccepted(View new_view) {
		// TODO Auto-generated method stub
		
	}
	public void suspect(Address suspected_mbr) {
		// TODO Auto-generated method stub
		
	}
	public void block() {
		// TODO Auto-generated method stub
		
	}
	public void unblock() {
		// TODO Auto-generated method stub
		
	}
}
