package org.universAAL.middleware.connectors.deploy.karaf.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.connectors.DeployConnector;
import org.universAAL.middleware.connectors.deploy.karaf.KarafDeployConnector;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;

/**
 * Activator for karaf Deploy Connector
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * 
 */
public class Activator implements BundleActivator {
    KarafDeployConnector kDeployConnector;

    public void start(BundleContext context) throws Exception {

	ModuleContext moduleContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
		new Object[] { "Starting the KarafDeployConnector..." }, null);
	kDeployConnector = new KarafDeployConnector(moduleContext);

	kDeployConnector.init();

	uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
		kDeployConnector,
		new Object[] { DeployConnector.class.getName() });
	LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
		new Object[] { "Started the KarafDeployConnector" }, null);

    }

    public void stop(BundleContext context) throws Exception {
	if (kDeployConnector != null)
	    kDeployConnector.dispose();
    }

}
