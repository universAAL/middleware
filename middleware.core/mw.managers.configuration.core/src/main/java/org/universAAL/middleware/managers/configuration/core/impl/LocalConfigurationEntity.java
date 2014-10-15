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

import java.util.Locale;

import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DescribedEntity;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.managers.configuration.core.impl.factories.EntityFactory;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;

/**
 * Abstract class for Local {@link ConfigurableEntityEditor}s. implement method
 * for getDescription (from {@link DescribedEntity} compared with file to
 * maintain it updated), utility method to get the entity (from file).
 * 
 * @author amedrano
 */
public abstract class LocalConfigurationEntity extends
	GenericConfigurationEntity implements ConfigurableEntityEditor {

    /**
     * 
     */
    public LocalConfigurationEntity(
	    ConfigurationManagerImpl configurationManagerImpl, String uri) {
	super(configurationManagerImpl, uri);
    }

    /** {@ inheritDoc} */
    public String getDescription(Locale loc) {
	DescribedEntity s = confManager.entitiesSources.get(uri);
	Entity old = confManager.manager.find(uri);
	Entity ne = EntityFactory.updateEntity(old, s, loc);
	confManager.manager.addEntity(ne);
	return ne.getDescription(loc);
    }

    protected Entity getEntity() {
	Entity e = confManager.manager.find(uri);
	return e;
    }

}