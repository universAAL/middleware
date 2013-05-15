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

    protected Object doExecute() throws Exception {
		log.debug("Executing command...");
		System.out.println("Command not existing anymore");
		return null;
    }

}
