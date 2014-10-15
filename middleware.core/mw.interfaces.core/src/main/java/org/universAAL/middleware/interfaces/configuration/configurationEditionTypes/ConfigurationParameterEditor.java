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

import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationParameter;

/**
 * Editor for a given {@link ConfigurationParameter}.
 * 
 * @author amedrano
 * 
 */
public interface ConfigurationParameterEditor extends ConfigurationParameter,
	ConfigurableEntityEditor {

    /**
     * Set a specific value as the configuration parameter.
     * 
     * @param value
     *            the value to set
     * @return true iff the value is valid and accepted by the
     *         {@link ConfigurableModule} managing the entity.
     */
    boolean setValue(Object value);

    /**
     * Get the current configured value.
     * 
     * @return the current working value.
     */
    Object getConfiguredValue();

}
