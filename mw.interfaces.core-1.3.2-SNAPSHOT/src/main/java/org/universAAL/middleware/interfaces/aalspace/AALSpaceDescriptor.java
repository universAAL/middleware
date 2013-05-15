/*	
	Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
	Institute of Information Science and Technologies 
	of the Italian National Research Council 

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
package org.universAAL.middleware.interfaces.aalspace;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;

/**
 * This class fully describes an AALSpace.
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class AALSpaceDescriptor implements Serializable {

    /**
     *  
     */
    private static final long serialVersionUID = -3522163669123414699L;
    private AALSpaceCard spaceCard;
    private List<ChannelDescriptor> brokerChannels;
    /**
     * ID of the peer configured as deploy manager
     */
    private PeerCard deployManager;

    public PeerCard getDeployManager() {
	return deployManager;
    }

    public void setDeployManager(PeerCard deployManager) {
	this.deployManager = deployManager;
    }

    public List<ChannelDescriptor> getBrokerChannels() {
	return brokerChannels;
    }

    public void setBrokerChannels(List<ChannelDescriptor> brokerChannels) {
	this.brokerChannels = brokerChannels;
    }

    public AALSpaceCard getSpaceCard() {
	return spaceCard;
    }

    public void setSpaceCard(AALSpaceCard spaceCard) {
	this.spaceCard = spaceCard;
    }

    public AALSpaceDescriptor(AALSpaceCard spaceCard,
	    List<ChannelDescriptor> brokerChannels) {
	this.spaceCard = spaceCard;
	this.brokerChannels = brokerChannels;
    }

    public AALSpaceDescriptor() {

    }

}
