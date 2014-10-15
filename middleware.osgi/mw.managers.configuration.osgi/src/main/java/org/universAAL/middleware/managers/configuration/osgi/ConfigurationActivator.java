/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.managers.configuration.osgi;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.ConfigurationEditor;
import org.universAAL.middleware.managers.api.ConfigurationManager;
import org.universAAL.middleware.managers.api.ConfigurationManagerConnector;
import org.universAAL.middleware.managers.configuration.core.impl.ConfigurationManagerImpl;

public class ConfigurationActivator implements BundleActivator {

    ModuleContext context;

    ConfigurationManagerImpl cm;

    public void start(BundleContext arg0) throws Exception {
	context = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { arg0 });
	LogUtils.logDebug(context, getClass(), "start",
		"Starting Configuration Manager.");
	/*
	 * uAAL stuff
	 */
	BundleConfigHome folder = new BundleConfigHome(
		"mw.managers.configuration");
	cm = new ConfigurationManagerImpl(context, new FileProvider(new File(
		folder.getAbsolutePath())));

	context.getContainer().shareObject(
		context,
		cm,
		new String[] { ConfigurationManager.class.getName(),
			ConfigurationEditor.class.getName(),
			ConfigurationManagerConnector.class.getName(), });

	LogUtils.logDebug(context, getClass(), "start", "Started.");
    }

    public void stop(BundleContext arg0) throws Exception {
	LogUtils.logDebug(context, getClass(), "stop", "Stopping.");
	/*
	 * close uAAL stuff
	 */
	cm.finish();
	cm = null;
	LogUtils.logDebug(context, getClass(), "stop", "Stopped.");

    }

}
