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

import org.universAAL.middleware.interfaces.ChannelDescriptor;

/**
 * Methods for configure the communication module
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public interface ConfigurableCommunicationModule extends CommunicationModule {

    /**
     * This method allows to configure the CommunicationModule with a List of
     * channels
     * 
     * @param communicationChannels
     */
    public void configureChannels(
	    List<ChannelDescriptor> communicationChannels, String peerName);

    /**
     * This method allows to reset the Communication module
     * 
     * @param communicationChannels
     */
    public void dispose(List<ChannelDescriptor> communicationChannels);

    /**
     * * This method fetches the set of members that join to the group
     * 
     * @param groupName
     * @return List of addresses of the members
     */
    public List<String> getGroupMembers(String groupName);

}
