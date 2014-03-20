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

package org.universAAL.middleware.managers.configuration.core.impl;

import java.util.List;

import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditorListener;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;
import org.universAAL.middleware.managers.configuration.core.impl.factories.ScopeFactory;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;

/**
 * The top Internal implementation of {@link ConfigurableEntityEditor}.
 * Has typical and utility methods for managing Listeners and URI.
 * @author amedrano
 *
 */
public abstract class GenericConfigurationEntity implements ConfigurableEntityEditor{

    /**
     * 
     */
    protected final ConfigurationManagerImpl confManager;
    protected String uri;
    protected List<ConfigurableEntityEditorListener> listeners;

    /**
     * 
     */
    public GenericConfigurationEntity(ConfigurationManagerImpl configurationManagerImpl, String uri){
        confManager = configurationManagerImpl;
        this.uri = uri;
    }
    
    public String getURI(){
	return uri;
    }

    /** {@ inheritDoc}	 */
    public void subscribe2Changes(ConfigurableEntityEditorListener listener) {
        listeners.add(listener);
        
    }

    /** {@ inheritDoc}	 */
    public void unsubscribe2Changes(ConfigurableEntityEditorListener listener) {
        listeners.remove(listener);
    }

    /** {@ inheritDoc}	 */
    public Scope getScope() {
        return ScopeFactory.getScope(uri);
    }

    public void updated(Entity e){
	for (ConfigurableEntityEditorListener l : listeners) {
	    l.ConfigurationChanged(this);
	}
    }
    
}