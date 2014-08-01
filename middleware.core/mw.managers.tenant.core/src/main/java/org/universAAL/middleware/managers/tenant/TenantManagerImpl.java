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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.managers.api.TenantManager;
/**
 * The implementation of the TenantManager
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class TenantManagerImpl implements TenantManager 
{

	private Map<String, String> tenants = new HashMap<String, String>();
	public TenantManagerImpl(ModuleContext module){
		
	}
	public void loadConfigurations(Dictionary configurations) {
		// TODO Auto-generated method stub

	}

	public boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}
	public void registerTenant(String tenantID, String tenantDescription) {
		if(tenantID != null && tenantDescription != null)
			tenants.put(tenantID, tenantDescription);
		
	}
	public void unregisterTenant(String tenantID) {
		if(tenantID != null )
			tenants.remove(tenantID);
	}
	public Map<String, String> getTenants() {
			return tenants;
	}

}
