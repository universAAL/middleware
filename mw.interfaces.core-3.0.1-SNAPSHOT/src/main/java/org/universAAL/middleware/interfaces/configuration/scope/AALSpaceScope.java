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

package org.universAAL.middleware.interfaces.configuration.scope;


/**
 * Defines Scopes for entities that are applicable over the whole AALSpace, so when managed its value will be the same on the whole AALSpace.
 * @author amedrano
 *
 */
public class AALSpaceScope extends Scope{
    /**
     * Constructor for a entity with unique identifier.
     * @param id a unique identifier.
     */
    public AALSpaceScope(String id) {
        super(id);
    }
}