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

package org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes;

import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;

/**
 * Additonally to {@link DescribedEntity DescribedEntities}, this interfaced may
 * be added. This should only be done for configurations that may change
 * locally, ie not only through
 * {@link ConfigurableModule#configurationChanged(Scope, Object)}. <br>
 * when the listener is called
 * {@link ConfigurableModule#configurationChanged(Scope, Object)} will be
 * called, and local and remote {@link ConfigurationEditor ConfigurationEditors}
 * will be notified.
 * 
 * @author amedrano
 * 
 */
public interface DynamicDescribedEntity {

    /**
     * Add a listener that should be called when the entity is changed.
     */
    public void registerListener(DynamicDescribedEntityListener listener);

    /**
     * Remove a listener.
     */
    public void unRegisterListener(DynamicDescribedEntityListener listener);
}
