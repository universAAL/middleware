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
import org.universAAL.middleware.datarep.SharedResources;

/**
 * @author amedrano
 *
 */
public class DataRepresentation implements ModuleActivator {

	/** {@ inheritDoc} */
	public void start(ModuleContext mc) throws Exception {
		SharedResources.moduleContext = mc;
		SharedResources.loadReasoningEngine();
		SharedResources.setDefaults();
		SharedResources.setMiddlewareProp(SharedResources.IS_COORDINATING_PEER,
				"true");
	}

	/** {@ inheritDoc} */
	public void stop(ModuleContext mc) throws Exception {
		SharedResources.unloadReasoningEngine();
	}

}
