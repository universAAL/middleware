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
package org.universAAL.middleware.shell.universAAL.osgi;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.managers.api.TenantManager;

@Command(scope = "universAAL", name = "tenants", description = "Print the tenants connected to uAAL instance")
public class TenantCommand extends OsgiCommandSupport {

	private TenantManager tenantManager;

	@Override
	protected Object doExecute() throws Exception {
		// TODO Auto-generated method stub
		ServiceReference ref = bundleContext
				.getServiceReference(TenantManager.class.getName());
		if (ref != null) {
			tenantManager = (TenantManager) bundleContext.getService(ref);
			System.out
					.println("--------Tenants connected to this instance----------");
			for (String tenantID : tenantManager.getTenants().keySet()) {
				System.out.println("Tenant ID: " + tenantID
						+ " - Tenant Description:"
						+ tenantManager.getTenants().get(tenantID));
			}
		}
		return null;
	}
}
