package org.universAAL.middleware.connectors.discovery.jgroups.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
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
import org.universAAL.middleware.connectors.discovery.jgroups.core.messages.Query;
import org.universAAL.middleware.connectors.discovery.jgroups.core.messages.Response;
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
	private AALSpaceManager aalSpaceManager = null;
	private ChannelDescriptor discoveryChannelDescriptor;
	private PeerCard myPeerCard;
	private List<AALSpaceCard> listOfFilteredAALSpaces = new ArrayList<AALSpaceCard>();
	
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

			Object[] aalManagers = context.getContainer()
					.fetchSharedObject(
							context,
							new Object[] { AALSpaceManager.class.getName()
									.toString() }, this);
			
			if (aalManagers != null && aalManagers.length > 0 && aalSpaceManager == null) {
				
				// LOG
				LogUtils.logDebug(
					context, 
					jGroupsDiscoveryConnector.class,
					METHOD,
					new Object[] { "Initializing JGroups Discovery Connector..." }, 
					null);

				aalSpaceManager = (AALSpaceManager) aalManagers[0];
				loadConfigurationData();
			}
		}
		
		return initialized;
	}

	private void loadConfigurationData() {
		
		final String METHOD = "loadConfigurationData";
		
		discoveryChannelDescriptor = aalSpaceManager.getAALSpaceDefaultConfigurartion()
				.getDiscoveryChannel().getChannelDescriptor();
		
		myPeerCard = aalSpaceManager.getMyPeerCard();
		
		if(verifyChannelDescriptor(discoveryChannelDescriptor) && configureDiscoveryChannel(discoveryChannelDescriptor)){
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

	/**
	 * Verify if the channedDescriptor properties Name, URL and Value are set
	 * 
	 * @param channelDescriptor
	 * @return true if Name and at least one of the other two (URL and Value) are set, false otherwise.
	 */
	private boolean verifyChannelDescriptor(ChannelDescriptor channelDescriptor) {
		
		if(discoveryChannel != null) return true;
		
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
			//discoveryChannel.setDiscardOwnMessages(true);
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

			aalSpaceManager = (AALSpaceManager)sharedObj;
			loadConfigurationData();
			
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

	public List<AALSpaceCard> findAALSpace(Dictionary<String, String> filters){
		
		final String METHOD = "findAALSpace";
		
		listOfFilteredAALSpaces.clear();
		
		for(AALSpaceCard sc : aalSpaceManager.getAALSpaces()){
			listOfFilteredAALSpaces.add(sc);
		}

		/*
		if (init()) {
			try{
				
				listOfFilteredAALSpaces.clear();
				
				if(filters == null || filters.size() == 0){
					for(AALSpaceCard sc : aalSpaceManager.getAALSpaces()){
						listOfFilteredAALSpaces.add(sc);
					}
				} else {
					LogUtils.logDebug(
						context,
						jGroupsDiscoveryConnector.class,
						METHOD,
						new Object[] { "Looking for an AALSpace - filter set: "+filters.toString() },
						null);
					// TODO Inviare messaggio in multicast con la query per l'AALSpace e il filtro
					try{
						
						Query myQuery = new Query(myPeerCard, (Hashtable<String, String>) filters);
						Message msg = new Message(null, null, myQuery.toString());
						
						discoveryChannel.send(msg);
						
					} catch (Exception e) {
						// LOG
						LogUtils.logError(
							context, 
							jGroupsDiscoveryConnector.class,
							METHOD,
							new Object[] { "Unable to send query with filters "+filters.toString()+" through the channel "+discoveryChannel.getClusterName() + ": "+e.getMessage() }, 
							e);
					}
				}
			} catch (DiscoveryConnectorException e){
				// LOG
				LogUtils.logError(
					context, 
					jGroupsDiscoveryConnector.class,
					METHOD,
					new Object[] { "DiscoveryConnector Exception: "+e.getMessage() }, 
					e);
	
			}
		}
		//return listOfFilteredAALSpaces.size() > 0 ? listOfFilteredAALSpaces : null;
		*/
		return listOfFilteredAALSpaces;
		
	}

	public List<AALSpaceCard> findAALSpace() throws DiscoveryConnectorException {
		return this.findAALSpace(null);
	}

	public void announceAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		
		final String METHOD = "announceAALSpace";
		
		if (init()) {
			
			// LOG
			LogUtils.logDebug(
				context, 
				jGroupsDiscoveryConnector.class,
				METHOD,
				new Object[] { "Announcing the AALSpace..." }, 
				null);
			
			try {
				if(discoveryChannel.getClusterName() == null){
					// LOG
					LogUtils.logDebug(
						context, 
						jGroupsDiscoveryConnector.class,
						METHOD,
						new Object[] { "Trying to send messasges through a closed Channel..." }, 
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
					new Object[] { "Unable to announce the AALSpace "+spaceCard.toString()+" through the channel "+discoveryChannel.getClusterName() + ": "+e.getMessage() }, 
					e);
			}
		} 
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
				
				listOfFilteredAALSpaces.remove(spaceCard);
				
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
	
	public void receive(Message msg) {
		
		final String METHOD = "receive";
		String msgBuffer = (String) msg.getObject();		
		if(init()){
		
			try {
		        Address sender = msg.getSrc();
		        
		        // LOG
				LogUtils.logDebug(context, 
					jGroupsDiscoveryConnector.class,
					METHOD,
					new Object[] { "Receiving message from node "+sender.toString()+" -> "+msgBuffer }, 
					null);
				
				DiscoveryMessage dm = DiscoveryMessage.unmarshall(msgBuffer);
				AALSpaceCard spaceCard = null; 
				
				switch (dm.getMessageType()){
				
				
					case ANNOUNCE:
						
						Announce announce = Announce.unmarshall(msgBuffer);
						spaceCard = announce.getSpaceCard();
						// LOG
						LogUtils.logDebug(
				                context,
				                jGroupsDiscoveryConnector.class,
				                METHOD,
				                new Object[] { "AALSpace Announce received - AALSpaceCard: "+spaceCard.toString() }, 
				                null);
						
						// TODO: remove static channel name
						spaceCard.setPeeringChannelName("mw.modules.aalspace.osgi");
						
						if (spaceCard.getCoordinatorID() != null) {
							for(ServiceListener listener: serviceListeners){
								Set<AALSpaceCard> spaceCards = new HashSet<AALSpaceCard>();
								spaceCards.add(spaceCard);
								listener.newAALSpacesFound(spaceCards);
							}
							
							LogUtils.logTrace(
							    context,
							    jGroupsDiscoveryConnector.class,
				                METHOD,
							    new Object[] { "AALSpace added - "+spaceCard.toString() },
							    null);
						}
						
						break;
		
					case QUERY:
						
						if (!myPeerCard.getPeerID().equals(sender.toString())){
						
							Query query = Query.unmarshall(msgBuffer);
							Dictionary<String, String> filters = query.getFilter();
							// LOG
							LogUtils.logDebug(
					                context,
					                jGroupsDiscoveryConnector.class,
					                METHOD,
					                new Object[] { "Query for AALSpace received - filters: "+filters.toString() }, 
					                null);
							
							if(myPeerCard.isCoordinator() && aalSpaceManager.getAALSpaceDescriptor() != null){
								spaceCard = aalSpaceManager.getAALSpaceDescriptor().getSpaceCard();
								if(AALSpaceMatch(spaceCard, filters)){
									// LOG
									LogUtils.logDebug(
							                context,
							                jGroupsDiscoveryConnector.class,
							                METHOD,
							                new Object[] { "AALSpace join the filters. Sending Response to "+msg.getSrc()}, 
							                null);
									
									try {
										if(discoveryChannel.getClusterName() == null){
											// LOG
											LogUtils.logDebug(
												context, 
												jGroupsDiscoveryConnector.class,
												METHOD,
												new Object[] { "Trying to send messages through a closed Channel..." }, 
												null);
											return;
										}
										
										Response myResponseToAQuery = new Response(myPeerCard, spaceCard);
										Message responseMsg = new Message(msg.getSrc(), null, myResponseToAQuery.toString());
										
										discoveryChannel.send(responseMsg);
														
									} catch (Exception e) {
										// LOG
										LogUtils.logError(
											context, 
											jGroupsDiscoveryConnector.class,
											METHOD,
											new Object[] { "Unable to respond to the query through the channel "+discoveryChannel.getClusterName() + ": "+e.getMessage() }, 
											e);
									}
								} else {
									// LOG
									LogUtils.logWarn(
						                context,
						                jGroupsDiscoveryConnector.class,
						                METHOD,
						                new Object[] { "AALSpace not found with filter: "+filters.toString() }, 
						                null);
								}
							} 
						}
						break;
		
					case RESPONSE:
						Response myResponse = Response.unmarshall(msgBuffer);
						spaceCard = myResponse.getSpaceCard();
						
						listOfFilteredAALSpaces.add(spaceCard);
						// LOG
						LogUtils.logDebug(
			                context,
			                jGroupsDiscoveryConnector.class,
			                METHOD,
			                new Object[] { "AALSpace "+spaceCard.getSpaceName()+" added to filtered list" }, 
			                null);
						break;
						
					default:
						// it should never get here
						break;
				}
					
			} catch (Exception e) {
	            //LOG
	        	LogUtils.logError(
	                context,
	                jGroupsDiscoveryConnector.class,
	                METHOD,
	                new Object[] { e.getMessage() }, 
	                e);
	        }

		}
        
	}

	private boolean AALSpaceMatch(AALSpaceCard spaceCard,
			Dictionary<String, String> filters) {
		return filters.get(Consts.AALSPaceID) == spaceCard.getSpaceID();
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
