package org.universAAL.middleware.shell.universAAL.osgi;

import java.util.Map;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.interfaces.mpa.UAPPStatus;
import org.universAAL.middleware.interfaces.mpa.Pair;
import org.universAAL.middleware.managers.api.DeployManager;

/**
 * Commands for MPA applications. Print the list of mpa applications managed by
 * the DeployManager (the coordinator of the aal space)
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
@Command(scope = "universAAL", name = "uaap", description = "Get the list of MPA applications and the status")
public class UAPPCommand extends OsgiCommandSupport {

    private DeployManager deployManager;

    protected Object doExecute() throws Exception {
	log.debug("Executing command...");
	ServiceReference ref = bundleContext
		.getServiceReference(DeployManager.class.getName());
	if (ref != null) {
	    deployManager = (DeployManager) bundleContext.getService(ref);
	} else {
	    return null;
	}

	if (deployManager.isDeployCoordinator()) {
	    if (deployManager.getUAPPRegistry() != null) {
		Map<String, UAPPStatus> registry = deployManager
			.getUAPPRegistry();
		System.out.println("Multi Part applications");
		System.out.println("---------------------------------------");

		for (String mpaID : registry.keySet()) {

		    UAPPStatus mpaStatus = registry.get(mpaID);
		    System.out.println("MPA: "
			    + mpaStatus.getMpaCard().toString());
		    Map<String, Pair> parts = mpaStatus.getMpaParts();
		    for (String partID : parts.keySet()) {
			Pair pair = parts.get(partID);
			System.out.println("Part ID: " + partID
				+ " - Peer ID: " + pair.getPeerID()
				+ " - Part status : "
				+ pair.getPartStatus().name());
		    }
		}
	    }
	}
	return null;

    }

}
