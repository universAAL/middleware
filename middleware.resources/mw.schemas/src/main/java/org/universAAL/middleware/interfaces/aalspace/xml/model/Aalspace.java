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

package org.universAAL.middleware.interfaces.aalspace.xml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.universAAL.middleware.interfaces.aalspace.model.IAALSpace;
import org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor;
import org.universAAL.middleware.interfaces.aalspace.model.ICommunicationChannels;
import org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel;
import org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="space-descriptor">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="profile" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="spaceId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="spaceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="spaceDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="peeringChannel">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="channel-descriptor" type="{http://universaal.org/aalspace-channel/v1.0.0}channelDescriptor"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="communicationChannels">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence maxOccurs="unbounded">
 *                   &lt;element name="channel-descriptor" type="{http://universaal.org/aalspace-channel/v1.0.0}channelDescriptor"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="owner" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="admin" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="security" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "spaceDescriptor",
    "peeringChannel",
    "communicationChannels",
    "owner",
    "admin",
    "security"
})
@XmlRootElement(name = "aalspace", namespace = "")
public class Aalspace
    implements Serializable, IAALSpace
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(name = "space-descriptor", required = true)
    protected SpaceDescriptor spaceDescriptor;
    @XmlElement(required = true)
    protected PeeringChannel peeringChannel;
    @XmlElement(required = true)
    protected CommunicationChannels communicationChannels;
    @XmlElement(required = true)
    protected String owner;
    @XmlElement(required = true)
    protected String admin;
    @XmlElement(required = true)
    protected String security;

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#getSpaceDescriptor()
     */
    public ISpaceDescriptor getSpaceDescriptor() {
        return spaceDescriptor;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#setSpaceDescriptor(org.universAAL.middleware.interfaces.aalspace.model.Aalspace.SpaceDescriptor)
     */
    public void setSpaceDescriptor(ISpaceDescriptor value) {
        this.spaceDescriptor = (SpaceDescriptor) value;
    }

    public void setSpaceDescriptor(SpaceDescriptor value) {
        this.spaceDescriptor = (SpaceDescriptor) value;
    }
    
    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#isSetSpaceDescriptor()
     */
    public boolean isSetSpaceDescriptor() {
        return (this.spaceDescriptor!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#getPeeringChannel()
     */
    public IPeeringChannel getPeeringChannel() {
        return peeringChannel;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#setPeeringChannel(org.universAAL.middleware.interfaces.aalspace.model.Aalspace.PeeringChannel)
     */
    public void setPeeringChannel(PeeringChannel value) {
        this.peeringChannel = value;
    }

    public void setPeeringChannel(IPeeringChannel value) {
        this.peeringChannel = (PeeringChannel) value;
    }
    
    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#isSetPeeringChannel()
     */
    public boolean isSetPeeringChannel() {
        return (this.peeringChannel!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#getCommunicationChannels()
     */
    public ICommunicationChannels getCommunicationChannels() {
        return communicationChannels;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#setCommunicationChannels(org.universAAL.middleware.interfaces.aalspace.model.Aalspace.CommunicationChannels)
     */
    public void setCommunicationChannels(ICommunicationChannels value) {
        this.communicationChannels = (CommunicationChannels) value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#isSetCommunicationChannels()
     */
    public boolean isSetCommunicationChannels() {
        return (this.communicationChannels!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#getOwner()
     */
    public String getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#setOwner(java.lang.String)
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#isSetOwner()
     */
    public boolean isSetOwner() {
        return (this.owner!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#getAdmin()
     */
    public String getAdmin() {
        return admin;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#setAdmin(java.lang.String)
     */
    public void setAdmin(String value) {
        this.admin = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#isSetAdmin()
     */
    public boolean isSetAdmin() {
        return (this.admin!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#getSecurity()
     */
    public String getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#setSecurity(java.lang.String)
     */
    public void setSecurity(String value) {
        this.security = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IAALSpace#isSetSecurity()
     */
    public boolean isSetSecurity() {
        return (this.security!= null);
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence maxOccurs="unbounded">
     *         &lt;element name="channel-descriptor" type="{http://universaal.org/aalspace-channel/v1.0.0}channelDescriptor"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "channelDescriptor"
    })
    public static class CommunicationChannels
        implements Serializable, ICommunicationChannels
    {

        private final static long serialVersionUID = 12343L;
        @XmlElement(name = "channel-descriptor", required = true)
        protected List<ChannelDescriptor> channelDescriptor;

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ICommunicationChannels#getChannelDescriptor()
	 */
        public List<IChannelDescriptor> getChannelDescriptor() {
            if (channelDescriptor == null) {
                channelDescriptor = new ArrayList<ChannelDescriptor>();
            }
            return new ArrayList<IChannelDescriptor>(channelDescriptor);
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ICommunicationChannels#isSetChannelDescriptor()
	 */
        public boolean isSetChannelDescriptor() {
            return ((this.channelDescriptor!= null)&&(!this.channelDescriptor.isEmpty()));
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ICommunicationChannels#unsetChannelDescriptor()
	 */
        public void unsetChannelDescriptor() {
            this.channelDescriptor = null;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="channel-descriptor" type="{http://universaal.org/aalspace-channel/v1.0.0}channelDescriptor"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "channelDescriptor"
    })
    public static class PeeringChannel
        implements Serializable, IPeeringChannel
    {

        private final static long serialVersionUID = 12343L;
        @XmlElement(name = "channel-descriptor", required = true)
        protected ChannelDescriptor channelDescriptor;

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel#getChannelDescriptor()
	 */
        public IChannelDescriptor getChannelDescriptor() {
            return channelDescriptor;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel#setChannelDescriptor(org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor)
	 */
        public void setChannelDescriptor(IChannelDescriptor value) {
            this.channelDescriptor = (ChannelDescriptor) value;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel#isSetChannelDescriptor()
	 */
        public boolean isSetChannelDescriptor() {
            return (this.channelDescriptor!= null);
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="profile" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="spaceId" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="spaceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="spaceDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "profile",
        "spaceId",
        "spaceName",
        "spaceDescription"
    })
    public static class SpaceDescriptor
        implements Serializable, ISpaceDescriptor
    {

        private final static long serialVersionUID = 12343L;
        @XmlElement(required = true)
        protected String profile;
        @XmlElement(required = true)
        protected String spaceId;
        @XmlElement(required = true)
        protected String spaceName;
        @XmlElement(required = true)
        protected String spaceDescription;

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#getProfile()
	 */
        public String getProfile() {
            return profile;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#setProfile(java.lang.String)
	 */
        public void setProfile(String value) {
            this.profile = value;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#isSetProfile()
	 */
        public boolean isSetProfile() {
            return (this.profile!= null);
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#getSpaceId()
	 */
        public String getSpaceId() {
            return spaceId;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#setSpaceId(java.lang.String)
	 */
        public void setSpaceId(String value) {
            this.spaceId = value;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#isSetSpaceId()
	 */
        public boolean isSetSpaceId() {
            return (this.spaceId!= null);
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#getSpaceName()
	 */
        public String getSpaceName() {
            return spaceName;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#setSpaceName(java.lang.String)
	 */
        public void setSpaceName(String value) {
            this.spaceName = value;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#isSetSpaceName()
	 */
        public boolean isSetSpaceName() {
            return (this.spaceName!= null);
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#getSpaceDescription()
	 */
        public String getSpaceDescription() {
            return spaceDescription;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#setSpaceDescription(java.lang.String)
	 */
        public void setSpaceDescription(String value) {
            this.spaceDescription = value;
        }

        /* (non-Javadoc)
	 * @see org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor#isSetSpaceDescription()
	 */
        public boolean isSetSpaceDescription() {
            return (this.spaceDescription!= null);
        }

    }

}
