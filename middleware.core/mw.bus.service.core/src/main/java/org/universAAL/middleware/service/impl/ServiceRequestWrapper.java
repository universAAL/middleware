/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.service.impl;

import java.util.Hashtable;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.aapi.AapiServiceRequest;
import org.universAAL.middleware.service.owl.Service;

/**
 * A wrapper class that provides a unified interface to get properties from
 * {@link ServiceRequest}s.
 * 
 * @author Carsten Stockloew
 * 
 */
public class ServiceRequestWrapper extends ServiceWrapper {

    private ServiceRequest r;

    public ServiceRequestWrapper(ServiceRequest r) {
	if (r == null)
	    throw new IllegalArgumentException();
	this.r = r;
    }

    @Override
    public Service getService() {
	return r.getRequestedService();
    }

    @Override
    public Object getProperty(String propURI) {
	return r.getProperty(propURI);
    }

    @Override
    public Resource[] getEffects() {
	return r.getRequiredEffects();
    }

    @Override
    public Resource[] getOutputs() {
	return r.getRequiredOutputs();
    }

    @Override
    public Hashtable getNonSemanticInput() {
	if (r instanceof AapiServiceRequest) {
	    return ((AapiServiceRequest) r).getInput();
	}
	return null;
    }

    @Override
    public Object getInitialServiceDialogProperty(String propURI) {
	return getService().getInstanceLevelFixedValueOnProp(propURI);
    }
}