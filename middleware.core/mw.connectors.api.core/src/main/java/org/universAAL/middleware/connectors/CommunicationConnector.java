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
package org.universAAL.middleware.connectors;

import java.util.List;

import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;

/**
 * Interface for the communication connector.
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:filippo.palumbo@isti.cnr.it">Filippo Palumbo</a>
 */
public interface CommunicationConnector extends Connector {

    /**
     * Send multicast message to a list of recipients in the same group
     */
    public void multicast(ChannelMessage message, List<PeerCard> receivers)
	    throws CommunicationConnectorException;

    /**
     * Send multicast message to a group (Broadcast)
     */
    public void multicast(ChannelMessage message)
	    throws CommunicationConnectorException;

    /**
     * Send unicast message to one recipient
     */
    public void unicast(ChannelMessage message, String receiver);

    /**
     * Configure the connector
     * 
     * @throws CommunicationModuleException
     */
    public void configureConnector(List<ChannelDescriptor> channels,
	    String peerName) throws CommunicationConnectorException;

    /**
     * Reset the CommunicationConnector
     * 
     * @param channels
     *            Channels to reset
     */
    public void dispose(List<ChannelDescriptor> channels);

    public String toString();

    public List<String> getGroupMembers(String groupName);
    
    public boolean hasChannel(String channelName);
}
