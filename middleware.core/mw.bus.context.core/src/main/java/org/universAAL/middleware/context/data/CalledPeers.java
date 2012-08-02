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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 14, 2012
 * 
 */
public class CalledPeers implements ICalledPeers {

    private String messageID;

    private int numOfCalledPeers;

    private List provisions = new ArrayList();

    public void setMessageID(String messageID) {
	this.messageID = messageID;
    }

    public String getMessageID() {
	return messageID;
    }

    public int getNumOfCalledPeers() {
	return numOfCalledPeers;
    }

    public void setNumOfCalledPeers(int numOfCalledPeers) {
	if (numOfCalledPeers >= 0) {
	    this.numOfCalledPeers = numOfCalledPeers;
	}
    }

    public boolean gotResponsesFromAllPeers() {
	return numOfCalledPeers == 0;
    }

    public void reduceNumOfCalledPeers() {
	setNumOfCalledPeers(numOfCalledPeers - 1);
    }

    public void resetCalledPeers() {
	numOfCalledPeers = 0;
    }

    public void addProvisions(List contextEventPatterns) {
	provisions.addAll(contextEventPatterns);
    }

    public List getProvisions() {
	return provisions;
    }
}
