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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * This class provides compact information about an AAL space
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class AALSpaceCard {

	private String spaceName;
	private String spaceID;
	private String description;
	private String peerCoordinatorID;
	private String peeringChannel;
	private String peeringChannelName;
	// TODO: Profile of the Space. Currently this is a string but a more
	// constrained type should be used
	private String profile;
	private int retry = 3;
	

	public String getPeerCoordinatorID() {
		return peerCoordinatorID;
	}

	public void setPeerCoordinatorID(String peerCoordinatorID) {
		this.peerCoordinatorID = peerCoordinatorID;
	}

	private int aalSpaceLifeTime;

	public int getAalSpaceLifeTime() {
		return aalSpaceLifeTime;
	}

	public void setAalSpaceLifeTime(int aalSpaceLifeTime) {
		this.aalSpaceLifeTime = aalSpaceLifeTime;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public String getPeeringChannelName() {
		return peeringChannelName;
	}

	public void setPeeringChannelName(String peeringChannelName) {
		this.peeringChannelName = peeringChannelName;
	}

	public String getPeeringChannel() {
		return peeringChannel;
	}

	public void setPeeringChannel(String peeringChannel) {
		this.peeringChannel = peeringChannel;
	}

	/**
	 * Instantiates an AALSpaceCard by using the Dictionary specified
	 * 
	 * @param prop
	 */
	public AALSpaceCard(Dictionary<String, String> prop) {
		this.spaceName = prop.get(Consts.AALSPaceName);
		this.spaceID = prop.get(Consts.AALSPaceID);
		this.description = prop.get(Consts.AALSPaceDescription);
		this.peerCoordinatorID = prop.get(Consts.AALSpaceCoordinator);
		this.peeringChannel = prop.get(Consts.AALSpacePeeringChannelURL);
		this.peeringChannelName = prop.get(Consts.AALSpacePeeringChannelName);
		this.profile = prop.get(Consts.AALSPaceProfile);
	}

	public AALSpaceCard() {
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * Return the peerID of the coordinator
	 * 
	 * @return
	 */
	public String getCoordinatorID() {
		return peerCoordinatorID;
	}

	public void setCoordinatorID(String coordinatorID) {
		this.peerCoordinatorID = coordinatorID;
	}

	public String getSpaceName() {
		return spaceName;
	}

	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	public String getSpaceID() {
		return spaceID;
	}

	public void setSpaceID(String spaceID) {
		this.spaceID = spaceID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Stores the AALSpaceCard as a Dictionary
	 * 
	 * @return
	 */
	public Dictionary<String, String> serializeCard() {
		Dictionary<String, String> prop = new Hashtable<String, String>();
		prop.put(Consts.AALSPaceName, spaceName);
		prop.put(Consts.AALSPaceDescription, description);
		prop.put(Consts.AALSPaceID, spaceID);
		// prop.put(Consts.AALSPaceType, type.toString());
		prop.put(Consts.AALSpaceCoordinator, peerCoordinatorID);
		prop.put(Consts.AALSpacePeeringChannelURL, peeringChannel);
		prop.put(Consts.AALSpacePeeringChannelName, peeringChannelName);
		return prop;
	}

	/**
	 * 
	 * @return a list of String whose name identifies the space card attributes
	 */
	public static List<String> getSpaceAttributes() {
		List<String> attrib = new ArrayList<String>();
		attrib.add(Consts.AALSPaceName);
		attrib.add(Consts.AALSPaceDescription);
		attrib.add(Consts.AALSPaceID);
		attrib.add(Consts.AALSPaceType);
		attrib.add(Consts.AALSpaceCoordinator);
		attrib.add(Consts.AALSpacePeeringChannelURL);
		return attrib;

	}

	/**
	 * This method provides a text-based representation of an AALSpaceCard
	 */
	public String toString() {
		return spaceName + " - " + description + " - " + spaceID + " - "
				+ peerCoordinatorID + " - " + peeringChannelName + " - "
				+ peeringChannel;

	}

	/**
	 * Two AALSpaceCard are equals iff the ID and the peerCoordinator ID are the
	 * same
	 */
	public boolean equals(Object o) {
		if (o != null && o instanceof AALSpaceCard) {
			AALSpaceCard toCompare = (AALSpaceCard) o;
			if (toCompare.getSpaceID().equals(this.spaceID)
					&& toCompare.peerCoordinatorID
							.equals(this.peerCoordinatorID))
				return true;
			else
				return false;
		} else
			return false;

	}

	public int hashCode() {
		return Integer.valueOf(this.spaceID);
	}

}
