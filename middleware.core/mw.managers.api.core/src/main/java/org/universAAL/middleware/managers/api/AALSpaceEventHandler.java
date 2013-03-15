package org.universAAL.middleware.managers.api;

import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;

/**
 * AALSpaceEventHandler interface. These methods are called by the lower layer
 * in order to manage events.
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public interface AALSpaceEventHandler {
    /**
     * The method is called asynchronously from the bottom layer of the MW after
     * a previous join(...) request.
     * 
     * @param descriptor
     *            all the information about the joinined AALSpace
     */
    public void aalSpaceJoined(AALSpaceDescriptor descriptor);

    /**
     * This method allows the AALSpaceManager to manage a join request from a
     * remote peer. In this case this AALSpaceManager is the AALSpace
     * coordinator
     * 
     * @param spaceCard
     * @param sender
     */
    public void joinRequest(AALSpaceCard spaceCard, PeerCard sender);

    /**
     * This method forces the peer to leace an AALSpace
     * 
     * @param spaceCard
     */
    public void leaveRequest(AALSpaceDescriptor spaceCard);

    /**
     * This method notifies to the coordinator that a peer wants to leave to the
     * AALSpace
     * 
     * @param sender
     */
    public void peerLost(PeerCard peer);

    /**
     * Method called when a peer joins the AALSpace managed by the
     * AALSpaceManager
     * 
     * @param peer
     */
    public void peerFound(PeerCard peer);

    /**
     * A new AALSpace has been found
     * 
     * @param spaceCard
     */
    public void newAALSpacesFound(Set<AALSpaceCard> spaceCards);

    public void aalSpaceEvent(AALSpaceStatus newStatus);

    /**
     * This method allows to configure the set of peers that actually join the
     * AAL Space managed or joined by this AALSpaceManager
     * 
     * @param peer
     *            Map of peers
     */
    public void setListOfPeers(Map<String, PeerCard> peer);

    /**
     * Called in order to alert the AALSpace about the installation of a new MPA
     */
    public void mpaInstalling(AALSpaceDescriptor spaceDescriptor);

    /**
     * Called in order to alert the AALSpace about the installation of a new MPA
     */
    public void mpaInstalled(AALSpaceDescriptor spaceDescriptor);

}
