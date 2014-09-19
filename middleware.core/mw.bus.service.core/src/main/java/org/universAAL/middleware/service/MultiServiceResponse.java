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

import java.util.List;
import java.util.Map;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

public class MultiServiceResponse extends ServiceResponse {

    @Override
    public void addOutput(ProcessOutput output) {
	// TODO Auto-generated method stub
	super.addOutput(output);
    }

    @Override
    public CallStatus getCallStatus() {
	// TODO Auto-generated method stub
	return super.getCallStatus();
    }

    @Override
    public List getOutput(String paramURI) {
	// TODO Auto-generated method stub
	return super.getOutput(paramURI);
    }

    @Override
    public List getOutput(String paramURI, boolean asMergedList) {
	// TODO Auto-generated method stub
	return super.getOutput(paramURI, asMergedList);
    }

    @Override
    public Map<String, List<Object>> getOutputsMap() {
	// TODO Auto-generated method stub
	return super.getOutputsMap();
    }

    @Override
    public List<ProcessOutput> getOutputs() {
	// TODO Auto-generated method stub
	return super.getOutputs();
    }

    @Override
    public Resource getProvider() {
	// TODO Auto-generated method stub
	return super.getProvider();
    }

    @Override
    public boolean setProperty(String propURI, Object value) {
	// TODO Auto-generated method stub
	return super.setProperty(propURI, value);
    }

}
