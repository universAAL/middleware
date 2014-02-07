/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

package org.universAAL.middleware.mw.manager.configuration.core.interfaces;

import java.util.List;

import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes.DescribedEntity;

/**
 * Hub to register {@link ConfigurableModule}s, and configurations (as {@link DescribedEntity}s) against.
 * @author amedrano
 *
 */
public interface ConfigurationManager {

    /**
     * Register a {@link List} of {@link DescribedEntity}s who's changes will be performed by the given {@link ConfigurableModule}.
     * After registration, the {@link ConfigurableModule} will be called with the stored configuration 
     * or the default value of the {@link DescribedEntity} (if not null)
     * @param confEntities list of {@link DescribedEntity DescribedEntities} that the {@link ConfigurableModule} will manage.
     * @param listener the {@link ConfigurableModule} that manages confEntities.
     */
    public void register(List<DescribedEntity> confEntities, ConfigurableModule listener);
    
    /**
     * Register an array of {@link DescribedEntity}s who's changes will be performed by the given {@link ConfigurableModule}.
     * After registration, the {@link ConfigurableModule} will be called with the stored configuration 
     * or the default value of the {@link DescribedEntity} (if not null)
     * @param confEntities Array of {@link DescribedEntity DescribedEntities} that the {@link ConfigurableModule} will manage.
     * @param listener the {@link ConfigurableModule} that manages confEntities.
     */
    public void register(DescribedEntity[] confEntities, ConfigurableModule listener);
    
    /**
     * Unregister a particular {@link ConfigurableModule}.
     * @param listener the {@link ConfigurableModule} to unregister.
     */
    public void unregister(ConfigurableModule listener);
    
}
