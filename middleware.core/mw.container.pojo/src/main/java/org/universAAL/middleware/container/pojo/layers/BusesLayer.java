/*******************************************************************************
 * Copyright 2017 Universidad Politécnica de Madrid
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

package org.universAAL.middleware.container.pojo.layers;

import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceBus.CallInjector;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.ui.IUIBus;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * @author amedrano
 *
 */
public class BusesLayer implements ModuleActivator {

	/** {@ inheritDoc} */
	public void start(ModuleContext mc) throws Exception {
		// init buses
		Object[] busFetchParams;

		busFetchParams = new Object[] { ContextBus.class.getName() };
		ContextBusImpl.startModule(mc.getContainer(), mc, busFetchParams,
				busFetchParams);

		busFetchParams = new Object[] { ServiceBus.class.getName() };
		Object[] busInjectFetchParams = new Object[] { CallInjector.class
				.getName() };
		ServiceBusImpl.startModule(mc, busFetchParams, busFetchParams,
				busInjectFetchParams, busInjectFetchParams);

		busFetchParams = new Object[] { IUIBus.class.getName() };
		UIBusImpl.startModule(mc.getContainer(), mc, busFetchParams,
				busFetchParams);

		mc.logInfo("BusesLayer", "Buses Loaded.", null);

	}

	/** {@ inheritDoc} */
	public void stop(ModuleContext mc) throws Exception {
		UIBusImpl.stopModule();

		ServiceBusImpl.stopModule();

		ContextBusImpl.stopModule();
	}

}
