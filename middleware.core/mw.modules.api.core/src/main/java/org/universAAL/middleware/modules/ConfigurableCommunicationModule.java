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
