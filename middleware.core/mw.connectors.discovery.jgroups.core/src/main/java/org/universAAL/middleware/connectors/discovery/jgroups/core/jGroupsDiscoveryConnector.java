package org.universAAL.middleware.connectors.discovery.jgroups.core;

import java.util.Dictionary;
import java.util.List;

import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

/**
 * Hello world!
 *
 */
public class jGroupsDiscoveryConnector implements DiscoveryConnector,
SharedObjectListener 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	public List<AALSpaceCard> findAALSpace() throws DiscoveryConnectorException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		
	}

	public void removeAALSpaceListener(ServiceListener listener) {
		// TODO Auto-generated method stub
		
	}
}
