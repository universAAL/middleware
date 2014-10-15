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
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.deploymanager.uapp.model.Part;

/**
 * The deployment data required for installing an uApp on the platform, which is
 * used by the {@link DeployManager}<br>
 * <b>NOTE</b>: The unique identifier of an {@link UAPPPackage} is defined by
 * the the pair {@link #getServiceId()} and {@link #getId()}
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class UAPPPackage {

    private final URI folder;
    private final String id;
    private final String serviceId;
    private final Map<PeerCard, List<Part>> deploy;

    /**
     * 
     * @param serviceId
     *            the {@link String} representing an unique identifier of the
     *            service which is provided by the uStore
     * @param id
     *            the {@link String} representing an unique identifier (within
     *            the service) of the application that we are going to install
     * @param folder
     *            is the {@link URI} where the uApp has been unpacked, which
     *            contains all the uApp file and configuration required for
     *            installing the uAAL service
     * @param layout
     *            contains a map describing the node where each uApp of
     *            contained in the uSrv has to be installed
     */
    public UAPPPackage(String serviceId, String id, URI folder,
	    Map<PeerCard, List<Part>> layout) {
	super();
	this.serviceId = serviceId;
	this.folder = folder;
	this.id = id;
	this.deploy = layout;
    }

    /**
     * 
     * @return the {@link String} representing the unique serviceId from uStore
     *         that contains the uApp
     */
    public String getServiceId() {
	return serviceId;
    }

    /**
     * 
     * @return the {@link URI} where the uApp has been upacked by the platform
     */
    public URI getFolder() {
	return folder;
    }

    /**
     * 
     * @return the unique {@link String} id (within the service) representing
     *         the application
     */
    public String getId() {
	return id;
    }

    /**
     * 
     * @return the deployment plan for the uApp application
     */
    public Map<PeerCard, List<Part>> getDeploy() {
	return deploy;
    }

}
