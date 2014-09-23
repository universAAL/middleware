/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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
package org.universAAL.middleware.service;

/**
 * This interface is implemented whether components that would like to be
 * notified whenever a new service realization is registered or deregistered. An
 * availability notification answers whether if realization of a service is
 * available.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public interface AvailabilitySubscriber {

    /**
     * Whenever a service is registered we need to pass two parameters for
     * mapping the notification to a specific subscription.
     * 
     * @param requestURI
     *            the URI of the original ServiceRequest object
     * @param realizationID
     *            a unique ID for each matched offer because of possible
     *            multiple matches to a certain request
     */
    public void serviceRegistered(String requestURI, String realizationID);

    /**
     * Whenever a service is deregistered we need to pass two parameters for
     * mapping the notification to a specific subscription.
     * 
     * @param requestURI
     *            the URI of the original ServiceRequest object
     * @param realizationID
     *            a unique ID for each matched offer because of possible
     *            multiple matches to a certain request
     */
    public void serviceUnregistered(String requestURI, String realizationID);
}
