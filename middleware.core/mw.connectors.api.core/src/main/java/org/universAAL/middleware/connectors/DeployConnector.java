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
package org.universAAL.middleware.connectors;

import java.io.File;

import org.universAAL.middleware.interfaces.mpa.UAPPCard;

/**
 * Interface for the deploy connectors.
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public interface DeployConnector extends Connector {

    /**
     * This method allows to install a part on the running container
     * 
     * @param serializedPart
     *            The part to be installed
     * @param card
     *            The reference information of the part of the application
     *            within a service to install
     */
    public void installPart(File applicationPart, UAPPCard card);

    /**
     * This method allows to uninstall a part on the running container
     * 
     * @param card
     *            The reference information of the part of the application
     *            within a service to install
     */
    public void uninstallPart(UAPPCard card);

}
