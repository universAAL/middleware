/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
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
package org.universAAL.middleware.context.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.context.data.factory.ContextStrategyDataFactory;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 17, 2012
 * 
 */
public class PropsDataMap implements IPropsData {

    private Map map = new HashMap();

    public IFiltererContainer getFiltererContainer(String key) {
	IFiltererContainer container = null;
	if (!map.containsKey(key)) {
	    container = new ContextStrategyDataFactory()
		    .createFiltererContainer(key);
	    map.put(key, container);
	} else {
	    container = (IFiltererContainer) map.get(key);
	}

	return container;
    }

    public Collection getAllFiltererContainers() {
	return map.values();
    }
}
