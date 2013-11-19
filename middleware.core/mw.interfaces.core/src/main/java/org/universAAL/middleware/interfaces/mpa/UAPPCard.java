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

package org.universAAL.middleware.interfaces.mpa;

import java.io.Serializable;

import org.universAAL.middleware.deploymanager.uapp.model.AalUapp.App;

/**
 * Compact representation of an uApp
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class UAPPCard implements Serializable {

    private static final long serialVersionUID = -3217977547051129449L;
    private String name;
    private String id;
    private String description;
    private String serviceId;
    private String partId;

    /**
     * 
     * @param serviceId
     *            The id of the uSrv containing the uAAP
     * @param id
     *            The id of the uAAP
     * @param partId
     *            The id of part within the uAAP
     * @param name
     *            The name of the uAAP
     * @param description
     *            The description of the uAAP
     */
    public UAPPCard(String serviceId, String id, String partId, String name,
	    String description) {
	this.serviceId = serviceId;
	this.name = name;
	this.id = id;
	this.description = description;
	this.partId = partId;
    }

    /**
     * 
     * @param serviceId
     *            The id of the uSrv containing the uAAP
     * @param partId
     *            The id of part within the uAAP
     * @param uApp
     *            The uAAP
     */
    public UAPPCard(String serviceId, String partId, App uApp) {
	this.serviceId = serviceId;
	this.name = uApp.getName();
	this.id = uApp.getAppId();
	this.description = uApp.getDescription();
	this.partId = partId;
    }

    public String getName() {
	return name;
    }

    public String getId() {
	return id;
    }

    public String getDescription() {
	return description;
    }

    public String getServiceId() {
	return serviceId;
    }

    public String getPartId() {
	return partId;
    }

    public String toString() {
	return serviceId + " - " + id + " - " + partId + " - " + name + " - "
		+ description;
    }

}
