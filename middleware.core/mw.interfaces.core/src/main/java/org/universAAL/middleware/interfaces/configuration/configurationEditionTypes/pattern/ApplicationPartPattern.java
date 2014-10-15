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
 * A pattern that matches entities with an application part type scope.
 * Additionally, it can match partId in scopes with the provided pattern.
 * 
 * @author amedrano
 * 
 */
public class ApplicationPartPattern implements EntityPattern {

    private String id;

    /**
     * Match any entity with ApplicationPart type scope.
     */
    public ApplicationPartPattern() {
	id = ".*";
    }

    /**
     * Match any Entity with an ApplicationPart Type scope where the partID also
     * matches the appPartID
     * 
     * @param appPartId
     */
    public ApplicationPartPattern(String appPartId) {
	id = appPartId;
    }

    /** {@ inheritDoc} */
    public TypeExpression getRestriction() {
	URIRestriction ur = new URIRestriction();
	ur.setPattern(".*part\\:" + id + ".*");
	return ur;
    }

}
