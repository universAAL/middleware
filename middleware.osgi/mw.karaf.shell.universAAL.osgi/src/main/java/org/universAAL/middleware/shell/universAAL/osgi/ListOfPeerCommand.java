package org.universAAL.middleware.shell.universAAL.osgi;

import java.util.Map;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.api.AALSpaceManager;

/**
 * Commands for universAAL
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
@Command(scope = "universAAL", name = "peers", description = "Get the list of peers joining the AALSpace")
public class ListOfPeerCommand extends OsgiCommandSupport {

    private AALSpaceManager aalSpaceManager;

    @Override
    protected Object doExecute() throws Exception {
	log.debug("Executing command...");
	ServiceReference ref = bundleContext
		.getServiceReference(AALSpaceManager.class.getName());
	if (ref != null) {
	    aalSpaceManager = (AALSpaceManager) bundleContext.getService(ref);
	} else {
	    return null;
	}
	Map<String, PeerCard> peers = aalSpaceManager.getPeers();
	if (peers != null) {
	    System.out.println(" Found: " + peers.size() + " Peers");
	    System.out.println(" ----------------------------------------");
	    for (String key : peers.keySet()) {
		if (key.equals(aalSpaceManager.getmyPeerCard().getPeerID()))
		    System.out.println(" * " + peers.get(key));
		else
		    System.out.println(peers.get(key));
	    }
	}

	return null;

    }

}
