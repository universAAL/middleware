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

	/**
	 * The URI prefix for the middleware.
	 */
	public static final String MIDDLEWARE_LOCAL_ID_PREFIX;
	static {
		MIDDLEWARE_LOCAL_ID_PREFIX = SharedResources.getMiddlewareProp(SharedResources.SPACE_URI) + "#";
	}

	/**
	 * Return true, if debug mode is turned on.
	 *
	 * @see org.universAAL.middleware.datarep.SharedResources#IS_DEBUG_MODE
	 */
	public static boolean debugMode() {
		return "true".equals(SharedResources.getMiddlewareProp(SharedResources.IS_DEBUG_MODE));
	}

	/**
	 * Return true, if this peer is the coordinator.
	 *
	 * @see org.universAAL.middleware.datarep.SharedResources#IS_COORDINATING_PEER
	 */
	public static boolean isCoordinatorInstance() {
		return "true".equals(SharedResources.getMiddlewareProp(SharedResources.IS_COORDINATING_PEER));
	}
}
