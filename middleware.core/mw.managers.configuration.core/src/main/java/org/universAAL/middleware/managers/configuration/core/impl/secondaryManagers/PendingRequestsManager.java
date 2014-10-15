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

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.universAAL.middleware.managers.api.ConfigurationEditor;
import org.universAAL.middleware.managers.api.ConfigurationEditor.ConfigurableEntityManager;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;
import org.universAAL.middleware.owl.TypeExpression;

/**
 * Manage the Requests, waiting for responses. Specifically for remote
 * configuration editors, that need to be created when the response arrives, and
 * then added to the {@link ConfigurableEntityManager}.
 * 
 * @author amedrano
 * 
 */
public class PendingRequestsManager {

    private Map<ConfigurableEntityManager, List<TypeExpression>> map;
    private ConfigurationEditorPool editorPool;

    /**
     * Create the manager and link to the editor pool.
     */
    public PendingRequestsManager(ConfigurationEditorPool editorPool) {
	map = new WeakHashMap<ConfigurationEditor.ConfigurableEntityManager, List<TypeExpression>>();
	this.editorPool = editorPool;
    }

    /**
     * add a {@link ConfigurableEntityManager} that has issued a request.
     * 
     * @param mngr
     * @param filter
     */
    public void add(ConfigurableEntityManager mngr, List<TypeExpression> filter) {
	if (map.containsKey(mngr)) {
	    map.get(mngr).addAll(filter);
	} else {
	    map.put(mngr, filter);
	}
    }

    /**
     * remove a {@link ConfigurableEntityManager}, no longer interested in
     * responses.
     * 
     * @param manager
     */
    public void remove(ConfigurableEntityManager manager) {
	map.remove(manager);
    }

    /**
     * Called when a response is received. for all the
     * {@link ConfigurableEntityManager} match the possible new entities, and
     * add them.
     * 
     * @param ents
     */
    public void processResponse(List<Entity> ents) {
	for (Map.Entry<ConfigurableEntityManager, List<TypeExpression>> entry : map
		.entrySet()) {
	    List<Entity> toBeAdded = EntityManager.filter(ents,
		    entry.getValue());
	    for (Entity e : toBeAdded) {
		entry.getKey().addConfigurableEntity(editorPool.get(e));
	    }
	}
    }

    /**
     * When finished.
     */
    public void clear() {
	map.clear();
    }
}
