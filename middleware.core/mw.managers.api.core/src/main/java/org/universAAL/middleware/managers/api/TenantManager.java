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

import java.util.Map;

/**
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * 
 */
public interface TenantManager extends Manager {
	
	/**
	 * Register a new tenant to the manager
	 * @param tenantID ID of the tenant
	 * @param tenantDescription description of the tenant
	 */
	public void registerTenant(String tenantID, String tenantDescription);
	
	/**
	 * Unregisters an existing tenant
	 * @param tenantID
	 */
	public void unregisterTenant(String tenantID);
	
	/**
	 * Retrieves the map of tenants
	 * @return
	 */
	public Map<String,String> getTenants();
	
	
	public void addTenantListener(TenantListener tenantListener);
	
	public void removeTenantListener(TenantListener tenantListener);	

}
