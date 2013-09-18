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

package org.universAAL.middleware.aalspace.json.model;

import java.io.Serializable;

import org.universAAL.middleware.interfaces.aalspace.model.IAALSpace;
import org.universAAL.middleware.interfaces.aalspace.model.ICommunicationChannels;
import org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel;
import org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor;

/**
 * 
 * 
 * @author <a href="mailto:sterfano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * @since 2.0.1
 */
public class AALSpace implements Serializable, IAALSpace {

    /**
     * 
     */
    private static final long serialVersionUID = -1679644587451410869L;
    protected ISpaceDescriptor spaceDescriptor;
    protected IPeeringChannel peeringChannel;
    protected ICommunicationChannels communicationChannels;
    protected String owner;
    protected String admin;
    protected String security;

    /**
     * Gets the value of the spaceDescriptor property.
     * 
     * @return possible object is {@link AALSpace.SpaceDescriptor }
     * 
     */
    public ISpaceDescriptor getSpaceDescriptor() {
	return spaceDescriptor;
    }

    /**
     * Sets the value of the spaceDescriptor property.
     * 
     * @param value
     *            allowed object is {@link AALSpace.SpaceDescriptor }
     * 
     */
    public void setSpaceDescriptor(ISpaceDescriptor value) {
	this.spaceDescriptor = value;
    }

    public boolean isSetSpaceDescriptor() {
	return (this.spaceDescriptor != null);
    }

    /**
     * Gets the value of the peeringChannel property.
     * 
     * @return possible object is {@link AALSpace.PeeringChannel }
     * 
     */
    public IPeeringChannel getPeeringChannel() {
	return peeringChannel;
    }

    /**
     * Sets the value of the peeringChannel property.
     * 
     * @param value
     *            allowed object is {@link AALSpace.PeeringChannel }
     * 
     */
    public void setPeeringChannel(IPeeringChannel value) {
	this.peeringChannel = value;
    }

    public boolean isSetPeeringChannel() {
	return (this.peeringChannel != null);
    }

    /**
     * Gets the value of the communicationChannels property.
     * 
     * @return possible object is {@link AALSpace.CommunicationChannels }
     * 
     */
    public ICommunicationChannels getCommunicationChannels() {
	return communicationChannels;
    }

    /**
     * Sets the value of the communicationChannels property.
     * 
     * @param value
     *            allowed object is {@link AALSpace.CommunicationChannels }
     * 
     */
    public void setCommunicationChannels(ICommunicationChannels value) {
	this.communicationChannels = value;
    }

    public boolean isSetCommunicationChannels() {
	return (this.communicationChannels != null);
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOwner() {
	return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOwner(String value) {
	this.owner = value;
    }

    public boolean isSetOwner() {
	return (this.owner != null);
    }

    /**
     * Gets the value of the admin property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getAdmin() {
	return admin;
    }

    /**
     * Sets the value of the admin property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setAdmin(String value) {
	this.admin = value;
    }

    public boolean isSetAdmin() {
	return (this.admin != null);
    }

    /**
     * Gets the value of the security property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSecurity() {
	return security;
    }

    /**
     * Sets the value of the security property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSecurity(String value) {
	this.security = value;
    }

    public boolean isSetSecurity() {
	return (this.security != null);
    }

}
