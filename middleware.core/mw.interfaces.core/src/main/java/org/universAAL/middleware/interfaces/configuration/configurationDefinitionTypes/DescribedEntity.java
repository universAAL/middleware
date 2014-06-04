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

import java.util.Locale;

import org.universAAL.middleware.interfaces.configuration.scope.AALSpaceScope;
import org.universAAL.middleware.interfaces.configuration.scope.AppPartScope;
import org.universAAL.middleware.interfaces.configuration.scope.ApplicationScope;
import org.universAAL.middleware.interfaces.configuration.scope.InstanceScope;
import org.universAAL.middleware.interfaces.configuration.scope.ModuleScope;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;

/**
 * A {@link DescribedEntity} is an Entity that is restricted to certain {@link Scope} and has a description.
 * @author amedrano
 * @See Scope
 */
public interface DescribedEntity {

    /**
     * Define the ID and Scope for this Entity.
     * <ul>
     * <li> {@link AALSpaceScope} defines that this entity is applicable over the whole AALSpace, so when managed its value will be the same on the whole AALSpace.
     * <li> {@link InstanceScope} defines that this entity is only applicable for the instance given in the scope.
     * <li> {@link ModuleScope} the entity is only applicable for the given module and instance.
     * <li> {@link ApplicationScope} defines that this entity is applicable over a certain application, this will be shared over the whole AALSpace for the same application.
     * <li> {@link AppPartScope} same as {@link ApplicationScope} but with finer grain, for application parts.
     * </ul>
     * @return a valid scope.
     */
    public Scope getScope();

    /**
     * Description of the entity. It can be localized so the description can be given in different languages.
     * @param loc the preferred locale for the description.
     * @return the Description of the entity.
     */
    public String getDescription(Locale loc);

}