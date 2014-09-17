/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;
import org.universAAL.middleware.managers.configuration.core.impl.factories.ScopeFactory;

/**
 * @author amedrano
 * 
 */
public class ModuleRegistry {

    Map<String, Set<WeakReference<ConfigurableModule>>> moduleRegistry;

    /**
     * 
     */
    public ModuleRegistry() {
	moduleRegistry = new HashMap<String, Set<WeakReference<ConfigurableModule>>>();
    }

    public void put(String uri, ConfigurableModule module) {
	WeakReference<ConfigurableModule> ref = new WeakReference<ConfigurableModule>(
		module);
	if (moduleRegistry.containsKey(uri)) {
	    moduleRegistry.get(uri).add(ref);
	} else {
	    Set<WeakReference<ConfigurableModule>> s = new HashSet<WeakReference<ConfigurableModule>>();
	    s.add(ref);
	    moduleRegistry.put(uri, s);
	}
    }

    /**
     * 
     */
    public void clear() {
	moduleRegistry.clear();
    }

    public void remove(ConfigurableModule module) {
	ArrayList<String> tbr = new ArrayList<String>();
	for (Entry<String, Set<WeakReference<ConfigurableModule>>> ent : moduleRegistry
		.entrySet()) {
	    Set<WeakReference<ConfigurableModule>> s = ent.getValue();
	    for (Iterator<WeakReference<ConfigurableModule>> i = s.iterator(); i
		    .hasNext();) {
		WeakReference<ConfigurableModule> ref = (WeakReference<ConfigurableModule>) i
			.next();
		if (ref.get() == null || ref.get().equals(module)) {
		    i.remove();
		}
	    }
	    if (s.isEmpty()) {
		tbr.add(ent.getKey());
	    }
	}
	// delete all entries with empty sets
	for (String urn : tbr) {
	    moduleRegistry.remove(urn);
	}
    }

    /**
     * @param uri
     * @return
     */
    public boolean contains(String uri) {
	return moduleRegistry.containsKey(uri);
    }

    /**
     * @param scope
     * @param value
     * @return
     */
    public boolean configurationChanged(Scope scope, Object value) {
	boolean aggregator = true;
	String urn = ScopeFactory.getScopeURN(scope);
	Set<WeakReference<ConfigurableModule>> s = moduleRegistry.get(urn);
	if (s == null) {
	    return false;
	}
	ArrayList<WeakReference<ConfigurableModule>> tbr = new ArrayList<WeakReference<ConfigurableModule>>();
	for (WeakReference<ConfigurableModule> ref : s) {
	    if (ref.get() != null) {
		aggregator &= ref.get().configurationChanged(scope, value);
	    } else {
		tbr.add(ref);
	    }
	}
	s.removeAll(tbr);
	if (s.isEmpty()) {
	    moduleRegistry.remove(urn);
	}
	return aggregator;
    }
}
