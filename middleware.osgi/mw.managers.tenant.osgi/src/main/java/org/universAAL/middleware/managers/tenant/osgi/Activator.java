package org.universAAL.middleware.managers.tenant.osgi;

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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.TenantManager;
import org.universAAL.middleware.managers.tenant.TenantManagerImpl;

/**
 * Activator for the Tenant Manager
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * 
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Activator implements BundleActivator {

    private TenantManager tenantManager;
    private ModuleContext moduleContext;

    public void start(BundleContext context) throws Exception {
	moduleContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	LogUtils.logDebug(moduleContext, Activator.class, "start",
		new Object[] { "Starting the TenantManager..." }, null);

	tenantManager = new TenantManagerImpl(moduleContext);

	tenantManager.init();
	uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
		tenantManager, new Object[] { TenantManager.class.getName() });
    }

    public void stop(BundleContext context) throws Exception {
	tenantManager.dispose();

    }

}
