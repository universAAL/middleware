/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
package org.universAAL.middleware.connectors.communication.jgroups.osgi;

import java.security.Security;
import java.util.Dictionary;
import java.util.Hashtable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.universAAL.middleware.connectors.CommunicationConnector;
import org.universAAL.middleware.connectors.communication.jgroups.JGroupsCommunicationConnector;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;

/**
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:filippo.palumbo@isti.cnr.it">Filippo Palumbo</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 */
public class Activator implements BundleActivator, ManagedService {

    private static String SERVICE_PID = "mw.connectors.communication.jgroups.core";
    private JGroupsCommunicationConnector jgroupsCommunicationConnector;
    private ServiceRegistration myRegistration;

    public void start(BundleContext bc) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        ModuleContext moduleContext = uAALBundleContainer.THE_CONTAINER
                .registerModule(new Object[] { bc });
        LogUtils.logDebug(
                moduleContext,
                Activator.class,
                "startBrokerClient",
                new Object[] { "Starting the JGroupCommunicationConnector..." },
                null);

        jgroupsCommunicationConnector = new JGroupsCommunicationConnector(
                moduleContext);

        Dictionary props = new Hashtable();
        props.put(Constants.SERVICE_PID, SERVICE_PID);
        myRegistration = bc.registerService(ManagedService.class.getName(),
                this, props);

        ConfigurationAdmin configurationAdmin = null;
        ServiceReference sr = bc.getServiceReference(ConfigurationAdmin.class
                .getName());
        if (sr != null && bc.getService(sr) instanceof ConfigurationAdmin)
            configurationAdmin = (ConfigurationAdmin) bc.getService(sr);
        Configuration config = configurationAdmin.getConfiguration(SERVICE_PID);

        Dictionary jGroupDCOnnector = config.getProperties();

        if (jGroupDCOnnector != null) {
            jgroupsCommunicationConnector.loadConfigurations(jGroupDCOnnector);
        }

        uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
                jgroupsCommunicationConnector,
                new Object[] { CommunicationConnector.class.getName() });
        LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
                new Object[] { "Started the JGroupCommunicationConnector..." },
                null);
    }

    public void stop(BundleContext bc) throws Exception {
        jgroupsCommunicationConnector.dispose();
        myRegistration.unregister();
    }

    public void updated(Dictionary props) throws ConfigurationException {
        jgroupsCommunicationConnector.loadConfigurations(props);
    }

}
