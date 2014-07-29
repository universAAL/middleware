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
 * Defines an Entity same as {@link ApplicationScope} but with finer grain, for application parts.
 * @author amedrano
 *
 */
public final class AppPartScope extends ApplicationScope {

    /**
     * The application part unique ID.
     */
    private String partID;

    /**
     * Constructor for an entity with unique Id, associated to an application with a part.
     * @param id the entity id.
     * @param appID the application id.
     * @param partID the id for the application part.
     */
    public AppPartScope(String id, String appID, String partID) {
	super(id, appID);
	if (partID == null || partID.isEmpty())
	    throw new IllegalArgumentException("partID cannot be null or empty");
        if (partID.matches(FORBIDDEN)){
            throw new IllegalArgumentException("partID contains forbiden format");
        }
	this.partID = partID;
    }
    
    /**
     * Get the application part ID.
     * @return
     */
    public String getPartID(){
	return partID;
    }

}
