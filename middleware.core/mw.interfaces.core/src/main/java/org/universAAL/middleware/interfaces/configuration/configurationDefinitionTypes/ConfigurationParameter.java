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

package org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes;

import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.owl.MergedRestriction;

/**
 * An Configuration entity that refers to a configuration parameter.
 * 
 * @author amedrano
 * 
 */
public interface ConfigurationParameter extends DescribedEntity {

    /**
     * The default value, for when a value is not set.
     * 
     * @return default vaule to use.
     */
    public Object getDefaultValue();

    /**
     * The type that the configuration parameter is allowed to take. The
     * {@link MergedRestriction} has to be bounded to the property
     * {@link ConfigurationParameter#PROP_CONFIG_VALUE}, otherwise the
     * {@link ConfigurationParameter} will not register properly.
     * 
     * @return a {@link MergedRestriction} over the property that defines the
     *         restrictions on the object received in
     *         {@link ConfigurableModule#configurationChanged(org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.Scope, Object)}
     *         for the scope of this Configuration Parameter Entity.
     */
    public MergedRestriction getType();

    public static String PROP_CONFIG_VALUE = "http://ontology.universAAL.org/AALConfigurationOntology#"
	    + "hasValue";
}
