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

package org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.managers.api.ConfigurationEditor.ConfigurableEntityManager;

/**
 * The Default implementation of {@link ConfigurableEntityManager} used to transform
 * the asynchronous calls of remote requests into a synchronous call.
 * @author amedrano
 *
 */
public class SynchronousConfEntityManager implements ConfigurableEntityManager {

    private List<ConfigurableEntityEditor> entities;
    private static long WAIT_TIME = 1000;
    
    /**
     * 
     */
    public SynchronousConfEntityManager() {
	entities = new ArrayList<ConfigurableEntityEditor>();
    }

    /** {@ inheritDoc}	 */
    public void addConfigurableEntity(ConfigurableEntityEditor cent) {
	entities.add(cent);
    }
    
    public List<ConfigurableEntityEditor> getList(){
	try {
	    Thread.sleep(WAIT_TIME);
	} catch (InterruptedException e) {}
	return entities;
    }

}
