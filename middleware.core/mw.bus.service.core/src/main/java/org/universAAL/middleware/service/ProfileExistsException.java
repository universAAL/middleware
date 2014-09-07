/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.service;

import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * Indicates that a profile that is trying to be registered is already
 * registered. The key for equality is the process URI of the profile.
 * 
 * @author Carsten Stockloew
 * 
 */
public class ProfileExistsException extends Exception {

    private static final long serialVersionUID = -3680652227316685556L;

    private ServiceProfile profile;
    private int index;

    public ProfileExistsException(ServiceProfile profile, int index) {
	this.profile = profile;
	this.index = index;
    }

    /**
     * Gets the {@link ServiceProfile}.
     * 
     * @return the {@link ServiceProfile}.
     */
    public ServiceProfile getProfile() {
	return profile;
    }

    /**
     * Gets the index of the {@link ServiceProfile}. When registering a set of
     * {@link ServiceProfile}, the {@link ServiceCallee} has to provide an array
     * of {@link ServiceProfile}. The index is the position in that array for
     * the profile that was rrgistered before.
     * 
     * @return the index in the array that was used during registration.
     */
    public int getIndex() {
	return index;
    }
}
