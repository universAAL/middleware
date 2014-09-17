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

package org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern;

import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.owl.URIRestriction;

/**
 * A pattern that matches entities with an Module type scope. Additionally, it
 * can match moduleId in scopes with the provided pattern.
 * 
 * @author amedrano
 * 
 */
public class ModulePattern implements EntityPattern {

    private String id;

    /**
     * Match any entity with Module type scope.
     */
    public ModulePattern() {
	id = ".*";
    }

    /**
     * Match Module type scope entities whose moduleId matches moduleIdPattern.
     * 
     * @param moduleIdPattern
     *            the pattern to match the moduleId in entities.
     */
    public ModulePattern(String moduleIdPattern) {
	id = moduleIdPattern;
    }

    /** {@ inheritDoc} */
    public TypeExpression getRestriction() {
	URIRestriction ur = new URIRestriction();
	ur.setPattern(".*mod\\:" + id + ".*");
	return ur;
    }

}
