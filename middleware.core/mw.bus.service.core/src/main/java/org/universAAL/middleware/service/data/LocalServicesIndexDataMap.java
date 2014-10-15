/**
 * 
 *  OCO Source Materials 
 *      Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.middleware.service.data;

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.service.impl.ServiceRealization;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 20, 2012
 * 
 */
public class LocalServicesIndexDataMap implements ILocalServicesIndexData {

    Map map = new HashMap();

    public void addServiceRealization(String id,
	    ServiceRealization serviceRealization) {
	map.put(id, serviceRealization);
    }

    public ServiceRealization removeServiceRealization(String id) {
	return (ServiceRealization) map.remove(id);
    }

    public ServiceRealization getServiceRealizationByID(String id) {
	return (ServiceRealization) map.get(id);
    }

    public ServiceRealization[] getAllServiceRealizations() {
	return (ServiceRealization[]) map.values().toArray(
		new ServiceRealization[0]);
    }

    public String[] getServiceRealizationIds() {
	return (String[]) map.keySet().toArray(new String[0]);
    }
}
