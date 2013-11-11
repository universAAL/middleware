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

/**
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public enum InstallationResults {
    /**
     * UAAPlication or Part install or uninstall with success
     */
    SUCCESS,
    /**
     * Failed install or uninstall an UAAPlication or Part due to a generic
     * issue
     */
    FAILURE,
    /**
     * Failed install or uninstall an UAAPlication or Part because we were not
     * joing to any AAL Space
     */
    NO_AALSPACE_JOINED, UAPP_URI_INVALID, DELEGATED, LOCALLY_DELEGATED,
    /**
     * The request to install or uninstall the UAAPlication has reached to a
     * peer that has DeployManager rights, role, or capability
     */
    NOT_A_DEPLOYMANAGER,
    /**
     * The UAAPlication package give does not contain XML content with the right
     * format
     */
    MPA_FILE_NOT_VALID, UNKNOWN, INVALID_DEPLOY_LAYOUT, MISSING_PEER, APPLICATION_NOT_INSTALLED, APPLICATION_ALREADY_INSTALLED, OPERATION_TIMEOUT;
}
