/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

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
package org.universAAL.middleware.tracker;

import org.universAAL.middleware.tracker.impl.Activator;

/**
 * Interface used for plugging into the registry of BusMembers in MW nodes.
 * 
 * @author dzmuda
 * 
 */
public interface IBusMemberRegistry {

    public final static Object[] busRegistryShareParams = Activator.fetchParams;

    /**
     * Enumeration used in notifications.
     * 
     * @author dzmuda
     * 
     */
    public enum BusType {
	Service, Context, UI
    }

    /**
     * Method used for adding listener for notifications about changes in
     * BusMember registry.
     * 
     * @param listener
     *            - listener to be added
     * @param notifyAboutPreviouslyRegisteredMembers
     *            - if true then the listener is automatically notified about
     *            all BusMembers currently available in registry.
     */
    public void addListener(IBusMemberRegistryListener listener,
	    boolean notifyAboutPreviouslyRegisteredMembers);

    /**
     * Method used for removal of listeners in BusMember registry
     * 
     * @param listener
     *            - listener to be removed
     */
    public void removeListener(IBusMemberRegistryListener listener);
}
