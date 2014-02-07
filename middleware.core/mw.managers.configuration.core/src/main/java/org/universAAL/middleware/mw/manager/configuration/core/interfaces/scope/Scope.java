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

package org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope;

import org.universAAL.middleware.container.ModuleContext;

/**
 * Abstract definition of a Scope.
 * A Scope, not only defines where it is applicable, it defines a unique identifier for an entity within the scope.
 * @author amedrano
 *
 */
public abstract class Scope{

    protected static final String FORBIDDEN = ".*(?:\\:|\\s).*";
    /**
     * The unique identifier for an entity.
     */
    private String id;
    
    /**
     * Constructor with a given identifier.
     * @param id the unique identifier for an entity within the scope.
     */
    public Scope (String id){
        if (id == null || id.isEmpty())
    		throw new IllegalArgumentException("ID cannot be null or empty");
        if (id.matches(FORBIDDEN)){
            throw new IllegalArgumentException("ID contains forbiden format");
        }
        this.id = id;
    }
    
    /**
     * Get the unique identifier for the entity with in the scope.
     * @return
     */
    public String getId(){
        return id;
    }
    
    public static Scope aalScope(String id){
	return new AALSpaceScope(id);
    }
    
    public static Scope instanceScope(String id, ModuleContext context) {
	return new InstanceScope(id, context);
    }
    public static Scope moduleScope(String id, ModuleContext mc){
	return new ModuleScope(id, mc);
    }
    
    public static Scope applicationScope(String id, String appId){
	return new ApplicationScope(id, appId);
    }
    public static Scope applicationPartScope(String id, String appId, String partId){
	return new AppPartScope(id, appId, partId);
    }

}