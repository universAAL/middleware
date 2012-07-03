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
package org.universAAL.middleware.context.data.factory;

import org.universAAL.middleware.context.data.AllProvisionDataVector;
import org.universAAL.middleware.context.data.CalledPeers;
import org.universAAL.middleware.context.data.FiltererContainerData;
import org.universAAL.middleware.context.data.IAllProvisionData;
import org.universAAL.middleware.context.data.ICalledPeers;
import org.universAAL.middleware.context.data.IFiltererContainer;
import org.universAAL.middleware.context.data.INumCalledPeersData;
import org.universAAL.middleware.context.data.IPropsData;
import org.universAAL.middleware.context.data.IProvisionsData;
import org.universAAL.middleware.context.data.NumCalledPeersDataMap;
import org.universAAL.middleware.context.data.PropsDataMap;
import org.universAAL.middleware.context.data.ProvisionsDataList;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 20, 2012
 * 
 */
public class ContextStrategyDataFactory extends
	AbstractContextStrategyDataFactory {

    public IProvisionsData createProvisionsData() {
	return new ProvisionsDataList();
    }

    public INumCalledPeersData createNumCalledPeersData() {
	return new NumCalledPeersDataMap();
    }

    public ICalledPeers createCalledPeers() {
	return new CalledPeers();
    }

    public IPropsData createAllPropsOfDomain() {
	return new PropsDataMap();
    }

    public IPropsData createAllPropsOfSubject() {
	return new PropsDataMap();
    }

    public IPropsData createAllSubjectsWithProp() {
	return new PropsDataMap();
    }

    public IPropsData createSpecificDomainAndProp() {
	return new PropsDataMap();
    }

    public IPropsData createSpecificSubjectAndProp() {
	return new PropsDataMap();
    }

    public IPropsData createNonIndexedProps() {
	return new PropsDataMap();
    }

    public IFiltererContainer createFiltererContainer(String containerKey) {
	return new FiltererContainerData(containerKey);
    }

    public IAllProvisionData createAllProvisions() {
	return new AllProvisionDataVector();
    }
}
