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
 * Defines an entity that is applicable over a certain application, this will be shared over the whole AALSpace for the same application.
 * @author amedrano
 */
public class ApplicationScope extends AALSpaceScope{

    /**
     * The application the entity is meant for.
     */
    private String appID;
    
    /**
     * Constructor for a given entity identifier and application.
     * @param id
     */
    public ApplicationScope(String id, String appID) {
        super(id);
        if (appID == null || appID.isEmpty())
    		throw new IllegalArgumentException("appID cannot be null or empty");
        if (appID.matches(FORBIDDEN)){
            throw new IllegalArgumentException("appID contains forbiden format");
        }
        this.appID = appID;
    }
    
    /**
     * Get the application ID.
     * @return
     */
    public String getAppID(){
        return appID;
    }
}