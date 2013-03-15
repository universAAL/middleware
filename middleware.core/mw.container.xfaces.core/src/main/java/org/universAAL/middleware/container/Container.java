/*
	Copyright 2011-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.container;

import java.util.Iterator;

/**
 * Represents the container <a
 * href="http://forge.universaal.org/wiki/middleware:Container">specified in the
 * context of the universAAL middleware</a>.
 * 
 * @author mtazari
 * 
 */
public interface Container {
    /**
     * Returns an object previously shared by another module for usage within
     * this container.
     * 
     * @param requester
     *            The module in the context of which the shared object is going
     *            to be used.
     * @param fetchParams
     *            Container-specific parameters for fetching shared objects.
     */
    public Object fetchSharedObject(ModuleContext requester,
	    Object[] fetchParams);

    /**
     * Returns an object previously shared by another module for usage within
     * this container.
     * 
     * @param requester
     *            The module in the context of which the shared object is going
     *            to be used.
     * @param fetchParams
     *            Container-specific parameters for fetching shared objects.
     * @param listener
     *            If not null, the listener will be notified asynchronously each
     *            time a new matching object is shared within this container.
     */
    public Object[] fetchSharedObject(ModuleContext requester,
	    Object[] fetchParams, SharedObjectListener listener);

    /**
     * This method allows a SharedObjectListener instance to be removed from the
     * list of listeners managed by this container
     * 
     * @param listener
     *            the SharedObjectListener to be removed
     */
    public void removeSharedObjectListener(SharedObjectListener listener);

    /**
     * Provides possibility for programmatically installing (downloaded)
     * modules.
     * 
     * @param requester
     *            The module requesting the installation; only an certain
     *            modules should be allowed to install modules on the fly
     * @param installParams
     *            Container-specific parameters for installing modules
     * @return The context of the newly installed module if the operation is
     *         successful, null otherwise.
     */
    public ModuleContext installModule(ModuleContext requester,
	    Object[] installParams);

    /**
     * Returns an {@link java.util.Iterator} object over all registered
     * instances of {@link LogListener}.
     */
    public Iterator logListeners();

    /**
     * Provides possibility for wrapping container-specific context for a module
     * in terms of an instance of the universAAL {@link ModuleContext}.
     * 
     * @param regParams
     *            The container-specific parameters for identifying the module.
     * @return The universAAL wrapper object as an instance of
     *         {@link ModuleContext}.
     */
    public ModuleContext registerModule(Object[] regParams);

    /**
     * Makes a given object accessible for the other modules hosted by this
     * container.
     * 
     * @param requester
     *            The module in the context of which the shared object is going
     *            to be used.
     * @param objToShare
     *            The actual object to be shared.
     * @param shareParams
     *            Container-specific parameters for sharing a specific object.
     */
    public void shareObject(ModuleContext requester, Object objToShare,
	    Object[] shareParams);
}
