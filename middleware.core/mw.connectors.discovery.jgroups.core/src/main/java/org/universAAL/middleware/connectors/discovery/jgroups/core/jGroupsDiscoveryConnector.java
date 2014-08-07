package org.universAAL.middleware.connectors.discovery.jgroups.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.discovery.jgroups.core.jGroupsDiscoveryConnector;
import org.universAAL.middleware.connectors.discovery.jgroups.core.messages.Announce;
import org.universAAL.middleware.connectors.discovery.jgroups.core.messages.DiscoveryMessage;
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
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:federico.volpini@isti.cnr.it">Federico Volpini</a>
 */
public class jGroupsDiscoveryConnector 
	implements DiscoveryConnector, SharedObjectListener, Receiver {

	private ModuleContext context;
	private JChannel discoveryChannel = null;
	private String name, description, provider, version;
	
	private boolean initialized = false;
	private List<ServiceListener> serviceListeners = new ArrayList<ServiceListener>();
	private AALSpaceManager aalSpaceManager;
	private ChannelDescriptor discoveryChannelDescriptor;
	private PeerCard myPeerCard;
	
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
	 * @return a String
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
		
		final String METHOD = "init";
		
		if (!initialized) {

			// LOG
			LogUtils.logDebug(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Initializing JGroups Discovery Connector..." }, 
				null);

			Object[] aalManagers = context.getContainer()
					.fetchSharedObject(
							context,
							new Object[] { AALSpaceManager.class.getName()
									.toString() }, this);
			
			if (aalManagers != null && aalManagers.length > 0) {
				aalSpaceManager = (AALSpaceManager) aalManagers[0];
				discoveryChannelDescriptor = aalSpaceManager.getAALSpaceDefaultConfigurartion()
						.getDiscoveryChannel().getChannelDescriptor();
				
				myPeerCard = aalSpaceManager.getMyPeerCard();
				
				if(verifyChannedDescriptor(discoveryChannelDescriptor) && configureDiscoveryChannel(discoveryChannelDescriptor)){
					// LOG
					LogUtils.logDebug(
						context, 
						jGroupsDiscoveryConnector.class,
						METHOD,
						new Object[] { "JGroups Discovery Connector initialized" }, 
						null);
		
					initialized = true;
				}

			}
		}
		
		return initialized;
	}

	/**
	 * Verify if the channedDescriptor properties Name, URL and Value are set
	 * 
	 * @param channelDescriptor
	 * @return true if Name and at least one of the other two (URL and Value) are set, false otherwise.
	 */
	private boolean verifyChannedDescriptor(ChannelDescriptor channelDescriptor) {
		
		final String METHOD = "verifyChannedDescriptor";
		
		boolean isOk = true; 
		
		// LOG
		LogUtils.logDebug(
			context, 
			jGroupsDiscoveryConnector.class,
			METHOD,
			new Object[] { "Verifying Channel Descriptor Data" }, null);
				
		if(!channelDescriptor.isSetChannelName()){
			isOk = false;
			//LOG
			LogUtils.logError(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Discovery Channel Name is not set" }, 
				null);
		}
		
		if(!channelDescriptor.isSetChannelURL()){
			//LOG
			LogUtils.logWarn(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "channelURL is not set" }, 
				null);
		}

		if(!channelDescriptor.isSetChannelValue()){
			//LOG
			LogUtils.logWarn(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "channelValue is not set" }, 
				null);
		}

		if(!channelDescriptor.isSetChannelURL() && !channelDescriptor.isSetChannelValue()){
			isOk = false;
			//LOG
			LogUtils.logError(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Both channelURL and channelValue are not set" }, 
				null);
		}

		// LOG
		LogUtils.logDebug(
			context, 
			jGroupsDiscoveryConnector.class,
			METHOD,
			new Object[] { "Channel Descriptor Data properly set" }, 
			null);
				
		return isOk;
	}

	/**
	 * This method try to connect to the Discovery Channel (eventually creating it if id doesn't exists) 
	 * using the properties of the discoveryChannelDescriptor
	 * 
	 * @param discoveryChannelDescriptor
	 * @return true if ok, false otherwise
	 */
	private boolean configureDiscoveryChannel(ChannelDescriptor discoveryChannelDescriptor) {
		
		if(discoveryChannel != null) return true;
		
		final String METHOD = "configureDiscoveryChannel";
		
		try {
			// LOG
			LogUtils.logDebug(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Configuring Channel "+discoveryChannelDescriptor.getChannelName() }, 
				null);
			
			// Prova a configurare il canale con la channelUrl. Se non settata o irraggiungibile
			// viene utilizzato il parametro channelValue
			
			String channelUrl = null;
			
			if(!discoveryChannelDescriptor.isSetChannelURL()){
				// LOG
				unableToReadChannelUrlLog(channelUrl);
				channelUrl = discoveryChannelDescriptor.getChannelValue();
			} else {
				channelUrl = discoveryChannelDescriptor.getChannelURL();
				HttpURLConnection connection = null;
			    try {
			        URL u = new URL(channelUrl);
			        connection = (HttpURLConnection) u.openConnection();
			        connection.setRequestMethod("HEAD");
			        int code = connection.getResponseCode();
			        // code = 200 is success.
			        if(code != 200){
			        	unableToReadChannelUrlLog(channelUrl);
				        channelUrl = discoveryChannelDescriptor.getChannelValue();
			        }
			    } catch (MalformedURLException e) {
			    	// LOG
					unableToReadChannelUrlLog(channelUrl);
				    channelUrl = discoveryChannelDescriptor.getChannelValue();
			    } catch (IOException e) {
			    	// LOG
					unableToReadChannelUrlLog(channelUrl);
				    channelUrl = discoveryChannelDescriptor.getChannelValue();
			    } finally {
			        if (connection != null) {
			            connection.disconnect();
			            
			        }
			    }
			}
			
			discoveryChannel = new JChannel(channelUrl);
			discoveryChannel.setName(myPeerCard.getPeerID());
			
			// Imposto l'istanza di classe come Receiver dei messaggi
			discoveryChannel.setReceiver(this);
			
			// LOG
			LogUtils.logDebug(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Channel correctly configured." },
				null);

			discoveryChannel.connect(discoveryChannelDescriptor.getChannelName());
			
			// LOG
			LogUtils.logDebug(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { myPeerCard.getPeerID()+" connected to cluster "+discoveryChannelDescriptor.getChannelName() },
				null);
			
		} catch (Exception e) {
			// LOG
			LogUtils.logError(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Error initializing JGroups Discovery Connector: "+e.getMessage() }, 
				e);
			return false;
		}
		return true;
	}

	private void unableToReadChannelUrlLog(String channelUrl) {
		
		final String METHOD = "configureChannel";
		
		// LOG
		LogUtils.logWarn(
			context, 
			jGroupsDiscoveryConnector.class,
			METHOD,
			new Object[] { "Unable to read congifuration data from URL "+channelUrl+"; Used channelValue instead." }, 
			null);
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
				new Object[] { "AALSpaceManager service added" }, 
				null);
			initialized = false;
			init();
			
		}
	}

	public void sharedObjectRemoved(Object removeHook) {

		final String METHOD = "sharedObjectRemoved";
		
		if (removeHook instanceof AALSpaceManager) {
		// LOG
			LogUtils.logDebug(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "AALSpaceManager service removed." }, 
				null);
			initialized = false;
			
		}

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
		
		final String METHOD = "announceAALSpace";
		
		// LOG
		LogUtils.logDebug(
			context, 
			jGroupsDiscoveryConnector.class,
			METHOD,
			new Object[] { "Announcing the AALSpace..." }, 
			null);
	
		if (init()) {
			try {
				if(discoveryChannel.getClusterName() == null){
					// LOG
					LogUtils.logDebug(
						context, 
						jGroupsDiscoveryConnector.class,
						METHOD,
						new Object[] { "Trying to announce the AALSpace trought a closed Channel..." }, 
						null);
					return;
				}
				
				Announce myAnnounce = new Announce(myPeerCard, spaceCard);
				Message msg = new Message(null, null, myAnnounce.toString());
				
				discoveryChannel.send(msg);
								
			} catch (Exception e) {
				// LOG
				LogUtils.logError(
					context, 
					jGroupsDiscoveryConnector.class,
					METHOD,
					new Object[] { "Unable to announce the AALSpace "+spaceCard.toString()+" trought the channel "+discoveryChannel.getClusterName() + ": "+e.getMessage() }, 
					e);
			}
		} else {
			// LOG
			LogUtils.logWarn(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Jgroups Discovery Connector not initialized" }, 
				null);
		}
	}

	/*
	private Address getAddress(PeerCard card){
		
		final String METHOD = "getAddress";

		String cardID = card.getPeerID();
		Address dst = null;
        View discoveryChannelView = discoveryChannel.getView();
		
        if (discoveryChannelView == null) {
        	//LOG
        	LogUtils.logError(
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
    	// LOG
        LogUtils.logWarn(
			context, 
	 		jGroupsDiscoveryConnector.class,
            METHOD,
	 		"Address of Peer "+card.toString()+" not found");
        return null;
	}
	*/
	
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
					e);
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
			serviceListeners.add(listener);
			
			// LOG
			LogUtils.logDebug(context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "New AALSpaceListener" }, 
				null);
		}

	}

	public void removeAALSpaceListener(ServiceListener listener) {
		
		final String METHOD = "removeAALSpaceListener";
		
		if (listener != null && !serviceListeners.contains(listener)) {
			serviceListeners.remove(listener);
			
			// LOG
			LogUtils.logDebug(context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "AALSpaceListener "+listener.toString()+" removed" }, 
				null);
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
		
		final String METHOD = "receive";
		String msgBuffer = (String) msg.getObject();		
		
		try {
	        Address sender = msg.getSrc();
			String typeOfSender = msg.getSrc().equals(discoveryChannel.getView().getMembers().get(0)) ? "COORDINATOR" : "PEER";
			
			// LOG
			LogUtils.logDebug(context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Receiving message from "+typeOfSender+" "+sender.toString()+" -> "+msg.toString() }, 
				null);
			
			DiscoveryMessage dm = DiscoveryMessage.unmarshall(msgBuffer);
					
			switch (dm.getMessageType()){
			
			case ANNOUNCE:
				
				Announce announce = Announce.unmarshall(msgBuffer);
				AALSpaceCard spaceCard = announce.getSpaceCard();
				// LOG
				LogUtils.logDebug(
		                context,
		                jGroupsDiscoveryConnector.class,
		                METHOD,
		                new Object[] { "AALSpace Announce received - AALSpaceCard:"+spaceCard.toString() }, 
		                null);
				
				// TODO: remove static channel name
				spaceCard.setPeeringChannelName("mw.modules.aalspace.osgi");
				
				if (spaceCard.getCoordinatorID() != null) {
					aalSpaceManager.getAALSpaces().add(spaceCard);
				    LogUtils.logTrace(
					    context,
					    jGroupsDiscoveryConnector.class,
		                METHOD,
					    new Object[] { "AALSpace added - "+spaceCard.toString() },
					    null);
				}
				
				break;

			case QUERY:
				break;

			case RESPONSE:
				
			default:
				break;
			}
		} catch (Exception e) {
            //LOG
        	LogUtils.logError(
                context,
                jGroupsDiscoveryConnector.class,
                METHOD,
                new Object[] { "Failed to unmarhall message due to exception "
                        + e.getMessage() }, 
                e);
        }

        
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
