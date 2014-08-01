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
package org.universAAL.middleware.interfaces.aalspace.model;

import org.universAAL.middleware.interfaces.aalspace.xml.model.Aalspace;

/**
 *
 *
 * @author <a href="mailto:sterfano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * @since 2.0.1
 */
public interface IAALSpace {

    /**
     * Gets the value of the spaceDescriptor property.
     *
     * @return
     *     possible object is
     *     {@link Aalspace.SpaceDescriptor }
     *
     */
    public abstract ISpaceDescriptor getSpaceDescriptor();

    /**
     * Sets the value of the spaceDescriptor property.
     *
     * @param value
     *     allowed object is
     *     {@link Aalspace.SpaceDescriptor }
     *
     */
    public abstract void setSpaceDescriptor(ISpaceDescriptor value);

    public abstract boolean isSetSpaceDescriptor();

    /**
     * Gets the value of the peeringChannel property.
     *
     * @return
     *     possible object is
     *     {@link Aalspace.PeeringChannel }
     *
     */
    public abstract IPeeringChannel getPeeringChannel();
    
    

    /**
     * Gets the value of the discoveryChannel property.
     *
     * @return
     *     possible object is
     *     {@link Aalspace.PeeringChannel }
     *
     */
    public abstract Aalspace.DiscoveryChannel getDiscoveryChannel();
    
    

    /**
     * Sets the value of the peeringChannel property.
     *
     * @param value
     *     allowed object is
     *     {@link Aalspace.PeeringChannel }
     *
     */
    public abstract void setPeeringChannel(IPeeringChannel value);

    public abstract boolean isSetPeeringChannel();

    /**
     * Gets the value of the communicationChannels property.
     *
     * @return
     *     possible object is
     *     {@link Aalspace.CommunicationChannels }
     *
     */
    public abstract ICommunicationChannels getCommunicationChannels();

    /**
     * Sets the value of the communicationChannels property.
     *
     * @param value
     *     allowed object is
     *     {@link Aalspace.CommunicationChannels }
     *
     */
    public abstract void setCommunicationChannels(
            ICommunicationChannels value);

    public abstract boolean isSetCommunicationChannels();

    /**
     * Gets the value of the security property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public abstract String getSecurity();

    /**
     * Sets the value of the security property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public abstract void setSecurity(String value);

    public abstract boolean isSetSecurity();
    

    

}
