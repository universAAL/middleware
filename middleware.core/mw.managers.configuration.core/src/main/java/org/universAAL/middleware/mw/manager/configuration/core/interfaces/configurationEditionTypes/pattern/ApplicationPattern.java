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

package org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationEditionTypes.pattern;

import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.owl.URIRestriction;

/**
 * A pattern that matches entities with an application type scope. 
 * Additionally, it can match applicationId in scopes with the provided pattern.
 * @author amedrano
 *
 */
public class ApplicationPattern implements EntityPattern{

    private String id; 
    
    /**
     * Math any entity with an Application type scope
     */
    public ApplicationPattern() {
	id = ".*";
    }
    
    /**
     * Match Application type entities whose applicationID matches appIdPattern.
     * @param appIdPattern the pattern to match applicationIDs.
     */
    public ApplicationPattern(String appIdPattern) {
	id = appIdPattern;
    }

    /** {@ inheritDoc}	 */
    public TypeExpression getRestriction() {
	URIRestriction ur = new URIRestriction();
	ur.setPattern(".*app\\:"+id+".*");
	return ur;
    }
    
}
