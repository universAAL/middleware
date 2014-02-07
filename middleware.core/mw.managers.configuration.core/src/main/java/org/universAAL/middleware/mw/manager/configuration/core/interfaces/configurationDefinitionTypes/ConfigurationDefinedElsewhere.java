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

package org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes;

import java.util.Locale;

import org.universAAL.middleware.mw.manager.configuration.core.interfaces.ConfigurableModule;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.Scope;

/**
 * Use this implementation when the configuration Entity is described elsewhere.
 * Just provide the scope of the entity to listen for changes on it.
 * {@link ConfigurableModule#configurationChanged(Scope, Object)} may not be called until the
 * Entity is registered elsewhere (or found in some configuration repository), thus don't expect
 * Immediate response.
 * @author amedrano
 *
 */
public class ConfigurationDefinedElsewhere implements DescribedEntity {

    private Scope scope;

    /**
     * Provide a {@link Scope} for an entity that is fully defined somewhere else.
     */
    public ConfigurationDefinedElsewhere(Scope s) {
	scope = s;
    }

    /** {@ inheritDoc}	 */
    public final Scope getScope() {
	return scope;
    }

    /** {@ inheritDoc}	 */
    public final String getDescription(Locale loc) {
	return null;
    }

}
