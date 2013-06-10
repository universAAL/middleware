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
package org.universAAL.middleware.modules;

import java.util.List;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.exception.CommunicationModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;

/**
 * The interface for the Communication Module. This interface is invoked from
 * the brokerage layer
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public interface CommunicationModule extends Module {

    /**
     * This method is invoked by the Connector layer as soon as a message
     * arrives.
     * 
     * @param channelMessage
     */
    public void messageReceived(ChannelMessage channelMessage);

    /**
     * Multicast send
     * 
     * @param message
     *            Message to send
     * @param receivers
     *            A list of receivers
     * @param listener
     *            The listener manages failures during the message send
     */
    public void sendAll(ChannelMessage message, List<PeerCard> receivers,
	    MessageListener listener) throws CommunicationModuleException;

    /**
     * Multicast implementation of the send. This signature does not require any
     * MessageListener
     * 
     * @param message
     * @param receivers
     */
    public void sendAll(ChannelMessage message, List<PeerCard> receivers)
	    throws CommunicationModuleException;

    /**
     * Broadcast implementation of the send
     * 
     * @param message
     * @param listener
     *            The listener that will manage failures during the message send
     */
    public void sendAll(ChannelMessage message, MessageListener listener)
	    throws CommunicationModuleException;

    /**
     * Broadcast implementation of the send
     * 
     * @param message
     */
    public void sendAll(ChannelMessage message)
	    throws CommunicationModuleException;

    /**
     * Unicast send
     * 
     * @param message
     * @param recipient
     * @param listener
     *            that will manage message failures
     */
    public void send(ChannelMessage message, MessageListener listener,
	    PeerCard receiver) throws CommunicationModuleException;

    /**
     * Unicast send to the AALSpace Coordinator for joining operation
     * 
     * @param message
     * @param coordinatorID
     * @param listener
     */
    public void send(ChannelMessage message, PeerCard receiver)
	    throws CommunicationModuleException;

    /**
     * Method used by the brokerage layer to add message listener
     * 
     * @param listener
     * @param ChannelMessage
     */
    public void addMessageListener(MessageListener listener, String channelName);

    /**
     * Method used by the upper layer to un-register themselves as message
     * listener
     * 
     * @param listener
     * @param ChannelMessage
     */
    public void removeMessageListener(MessageListener listener,
	    String channelName);

    /**
     * Returns the first MessageListener that is an instance of the given clz
     * and has been registered under the given name.
     * 
     * @param name
     * @return
     */
    public MessageListener getListenerByNameAndType(String name, Class clz);

    /**
     * Determines whether a channel with the specified name exists.
     * 
     * @param channelName
     *            name of the channel.
     * @return true, iff the channel exists.
     */
    public boolean hasChannel(String channelName);
}
