/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.util;

import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.Resource;

/**
 * Management of some basic middleware constants.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public final class Constants {

    private Constants() {
    }

    // URIs of standard variables managed by the uAAL middleware
    /**
     * The URI of a standard variable managed by the uAAL middleware indicating
     * the current time.
     */
    public static final String VAR_uAAL_CURRENT_DATETIME = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "currentDatetime";

    /**
     * The URI of a standard variable managed by the uAAL middleware indicating
     * the software component currently accessing the middleware.
     */
    public static final String VAR_uAAL_ACCESSING_BUS_MEMBER = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theAccessingBusMember";

    /**
     * The URI of a standard variable managed by the uAAL middleware indicating
     * the current human user as claimed by
     * {@link #VAR_uAAL_ACCESSING_BUS_MEMBER}.
     */
    public static final String VAR_uAAL_ACCESSING_HUMAN_USER = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theAccessingHumanUser";

    /**
     * The URI of a standard variable managed by the uAAL middleware indicating
     * the profile of a service that is estimated to be appropriate for
     * responding the current service request.
     */
    public static final String VAR_uAAL_SERVICE_TO_SELECT = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theServiceToSelect";

    /**
     * The URI prefix for the middleware.
     */
    public static final String uAAL_MIDDLEWARE_LOCAL_ID_PREFIX;
    static {
	uAAL_MIDDLEWARE_LOCAL_ID_PREFIX = SharedResources
		.getMiddlewareProp(SharedResources.uAAL_AAL_SPACE_ID) + "#";
    }

    /**
     * Return true, if debug mode is turned on.
     * 
     * @see org.universAAL.middleware.datarep.SharedResources#uAAL_IS_DEBUG_MODE
     */
    public static boolean debugMode() {
	return "true".equals(SharedResources
		.getMiddlewareProp(SharedResources.uAAL_IS_DEBUG_MODE));
    }

    /**
     * Return true, if this peer is the coordinator.
     * 
     * @see org.universAAL.middleware.datarep.SharedResources#uAAL_IS_COORDINATING_PEER
     */
    public static boolean isCoordinatorInstance() {
	return "true".equals(SharedResources
		.getMiddlewareProp(SharedResources.uAAL_IS_COORDINATING_PEER));
    }
}
