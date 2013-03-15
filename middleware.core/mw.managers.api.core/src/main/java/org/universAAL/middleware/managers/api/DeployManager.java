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
package org.universAAL.middleware.managers.api;

import java.net.URI;
import java.util.Map;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.mpa.UAPPStatus;
import org.universAAL.middleware.interfaces.mpa.model.Part;

/**
 * Deploy Manager Service interface
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public interface DeployManager extends Manager {

    /**
     * 
     * @param app
     *            {@link UAPPPackage} the representing the uAPP application
     *            deployment plan to install on uAAL
     * @return {@link InstallationResults} as result of the installation
     */
    public InstallationResults requestToInstall(UAPPPackage app);

    /**
     * True if I'm the Deploy coordinator
     * 
     * @return <code>true</code>if and only if the node has the Deploy Manager
     *         Coordinator role
     */
    public boolean isDeployCoordinator();

    /**
     * 
     * @return
     */
    public Map<String, UAPPStatus> getUAPPRegistry();

    /**
     * Remove an installed uApp by means of the unique pair {@link String}
     * serviceId and {@link String} id
     * 
     * @param serviceId
     *            the {@link String} representing the unique identifier of the
     *            service provided by the uStore
     * @param id
     *            the {@link String} representing the unique id (with the
     *            service) of the uApp to remove
     * @return the result of the uninstall task of the uApp
     */
    public InstallationResults requestToUninstall(String serviceId, String id);
}
