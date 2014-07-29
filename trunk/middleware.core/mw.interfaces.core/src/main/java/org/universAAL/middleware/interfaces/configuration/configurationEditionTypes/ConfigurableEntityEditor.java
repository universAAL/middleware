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

package org.universAAL.middleware.interfaces.configuration.configurationEditionTypes;

import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DescribedEntity;

/**
 * An entity that can be configured.
 * Top interface for Configuration Editors.
 * Implementations can edit local or remote configuration entities.
 * @author amedrano
 *
 */
public interface ConfigurableEntityEditor extends DescribedEntity{

    /**
     * Ask whether the entity has default value or not.
     * @return true if it is default value.
     */
    boolean isDefaultValue();
    
    /**
     * Set the default value for the entity.
     * @return true if it could be set.
     */
    boolean setDefaultValue();
    
    /**
     * Add a {@link ConfigurableEntityEditorListener}.
     * @param listener
     */
    void subscribe2Changes(ConfigurableEntityEditorListener listener);
    
    /**
     * remove a {@link ConfigurableEntityEditorListener}.
     * @param listener
     */
    void unsubscribe2Changes(ConfigurableEntityEditorListener listener);
    
}
