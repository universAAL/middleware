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
package org.universAAL.middleware.managers.tenant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.managers.api.TenantListener;
import org.universAAL.middleware.managers.api.TenantManager;

/**
 * The implementation of the TenantManager
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @since 3.2.0
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 *
 */
public class TenantManagerImpl implements TenantManager {

        private Map<String, String> tenants = new HashMap<String, String>();
        private List<TenantListener> listeners = new ArrayList<TenantListener>();

        public TenantManagerImpl(ModuleContext module) {
        }

        public void loadConfigurations(Dictionary configurations) {
            /*
             * No configuration for this manager
             */
        }

        public boolean init() {
            /*
             * No initialization required
             */
            return true;
        }

        public void dispose() {
            /*
             * No disposing phase
             */
        }

        public void registerTenant(String tenantID, String tenantDescription) {
                if (tenantID != null) {
                        if (tenantDescription == null) {
                                tenantDescription = "Missing description for " + tenantID;
                        }
                        tenants.put(tenantID, tenantDescription);
                        fireNewTenantRegisteredEvent(tenantID, tenantDescription);
                } else {
                        throw new NullPointerException("TennantID cannot be null");
                }

        }

        private void fireTenantRemovedEvent(String tenantID) {
                ArrayList<TenantListener> localCopy = null;
                synchronized (listeners) {
                        localCopy = new ArrayList<TenantListener>(listeners);
                }
                for (TenantListener listener : localCopy) {
                        try {
                                listener.tenantRemoved(tenantID);
                        } catch (Throwable t) {
                                t.printStackTrace(); // TODO log me
                        }
                }

        }

        private void fireNewTenantRegisteredEvent(String tenantID,
                        String tenantDescription) {
                ArrayList<TenantListener> localCopy = null;
                synchronized (listeners) {
                        localCopy = new ArrayList<TenantListener>(listeners);
                }
                // TODO: optimization instead of running through all the listeners and
                // invoking newTenantRegistered, a thread can do this on the background
                for (TenantListener listener : localCopy) {
                        try {
                                listener.newTenantRegistered(tenantID, tenantDescription);
                        } catch (Throwable t) {
                                t.printStackTrace(); // TODO log me
                        }
                }

        }

        public void unregisterTenant(String tenantID) {
                if (tenantID != null) {
                        tenants.remove(tenantID);
                        fireTenantRemovedEvent(tenantID);
                } else {
                        throw new NullPointerException("TennantID cannot be null");
                }
        }

        public Map<String, String> getTenants() {
                return tenants;
        }

        public void addTenantListener(TenantListener tenantListener) {
                if (tenantListener == null) {
                        throw new NullPointerException("Cannot add a null listener");
                }
                synchronized (listeners) {
                        if (listeners.contains(tenantListener))
                                return;
                        listeners.add(tenantListener);
                }
        }

        public void removeTenantListener(TenantListener tenantListener) {
                if (tenantListener == null) {
                        throw new NullPointerException("Cannot remove a null listener");
                }
                synchronized (listeners) {
                        listeners.remove(tenantListener);
                }
        }

}
