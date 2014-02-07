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

package org.universAAL.middleware.mw.manager.configuration.core.impl.secondaryManagers;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.mw.manager.configuration.core.impl.ConfigurationManagerImpl;
import org.universAAL.middleware.mw.manager.configuration.core.impl.GenericConfigurationEntity;
import org.universAAL.middleware.mw.manager.configuration.core.impl.LocalConfigurationFileEditor;
import org.universAAL.middleware.mw.manager.configuration.core.impl.LocalConfigurationParameterEditor;
import org.universAAL.middleware.mw.manager.configuration.core.impl.RemoteConfigurationFileEditor;
import org.universAAL.middleware.mw.manager.configuration.core.impl.RemoteConfigurationParamaterEditor;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.mw.manager.configuration.core.owl.ConfigurationFile;
import org.universAAL.middleware.mw.manager.configuration.core.owl.ConfigurationParameter;
import org.universAAL.middleware.mw.manager.configuration.core.owl.Entity;

/**
 * This secondary manager is used to create, and manage all the {@link ConfigurableEntityEditor ConfigurableEntityEditors} 
 * that need to be created. 
 * {@link WeakReference} is used, so when the editors are no longer used they automatically  de register.
 * If a {@link ConfigurableEntityEditor} is not yet referenced, then it is created (depending on the type of the Editor needed
 * file VS parameter and Local VS remote).
 * @author amedrano
 *
 */
public class ConfigurationEditorPool {

    private Map<String, WeakReference<GenericConfigurationEntity>> map;
    private ConfigurationManagerImpl confManager;
    /**
     * @param confManager The configuration Manager to link editors.
     * 
     */
    public ConfigurationEditorPool(ConfigurationManagerImpl confManager) {
	map = new HashMap<String, WeakReference<GenericConfigurationEntity>>();
	this.confManager = confManager;
    }

    /**
     * Get a {@link ConfigurableEntityEditor} or create one if needed.
     * @param e the entity associated to the editor.
     * @return the editor.
     */
    public GenericConfigurationEntity get(Entity e){
	String uri = e.getURI();
	if (map.containsKey(uri)){
	    return map.get(uri).get();
	}
	else {
	    GenericConfigurationEntity editor = createEditorFor(e);
	    add(editor);
	    return editor;
	}
    }
    
    private void add(GenericConfigurationEntity configEditor){
	WeakReference<GenericConfigurationEntity> ref 
		= new WeakReference<GenericConfigurationEntity>(configEditor);
	String uri = configEditor.getURI();
	map.put(uri, ref);
    }
    
    private void remove(GenericConfigurationEntity cEditor){
	    String uri = cEditor.getURI();
	    map.remove(uri);
    }
    
    /**
     * This method is called to update any existing Editor for the {@link Entity}.
     * works only for existing editors.
     * @param e the entity updated.
     */
    public void entityUpdated(Entity e){
	String uri = e.getURI();
	if (map.containsKey(uri)){
	    GenericConfigurationEntity editor = map.get(uri).get();
	    if (editor != null) {
		editor.updated(e);
	    }else{
		map.remove(uri);
	    }
	}
    }
    
    private GenericConfigurationEntity createEditorFor(Entity e){
	if (isLocal(e)){
	    if (e instanceof ConfigurationFile){
		return new LocalConfigurationFileEditor(confManager, e.getURI());
	    }else if (e instanceof ConfigurationParameter){
		return new LocalConfigurationParameterEditor(confManager, e.getURI());
	    }
	}else {
	    if (e instanceof ConfigurationFile){
		return new RemoteConfigurationFileEditor(confManager, e);
	    }else if (e instanceof ConfigurationParameter){
		return new RemoteConfigurationParamaterEditor(confManager, e);
	    }	    
	}
	return null;
    }

    /**	
     * Find if e is a local-only entity.	
     * @param e the entity to test
     * @return true iff e is local.
     */
    private boolean isLocal(Entity e) {
	return confManager.localOnlyExpression().hasMember(e);
    }
}
