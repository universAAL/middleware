package org.universAAL.middleware.connectors.discovery.jgroups.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

/**
 * This class implements the AALSpace discovery connector based on jGroups
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:federico.volpini@isti.cnr.it">Federico Volpini</a>
 */
public class jGroupsDiscoveryConnector implements DiscoveryConnector,
SharedObjectListener, Receiver  {


	private ModuleContext context;


	public jGroupsDiscoveryConnector(ModuleContext context){
		this.context = context;

	}
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	public void loadConfigurations(Dictionary configurations) {
		// TODO Auto-generated method stub

	}

	public boolean init() {
		// TODO Creo il canale e mi conetto al canale
		return false;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		// TODO Auto-generated method stub

	}

	public void sharedObjectRemoved(Object removeHook) {
		// TODO Auto-generated method stub

	}

	public List<AALSpaceCard> findAALSpace(Dictionary<String, String> filters)
			throws DiscoveryConnectorException {
		// TODO // TODO Inviare messaggio in multicast con la query per l'AALSpace e il filtro
		return null;
	}

	public List<AALSpaceCard> findAALSpace() throws DiscoveryConnectorException {
		// TODO Inviare messaggio in multicast con la query per l'AALSpace
		return null;
	}

	public void announceAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		// TODO Inviare messaggio in mutlicast per annunciare un nuovo AALSpace

	}

	public void deregisterAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		// TODO Inviare messaggio in mutlicast per cancellare un AALSpace esistente

	}

	public String getSDPPRotocol() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addAALSpaceListener(ServiceListener listener) {
		// TODO Auto-generated method stub

	}

	public void removeAALSpaceListener(ServiceListener listener) {
		// TODO Auto-generated method stub

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
