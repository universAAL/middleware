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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(name = "space-descriptor", required = true)
    protected Aalspace.SpaceDescriptor spaceDescriptor;
    @XmlElement(required = true)
    protected Aalspace.PeeringChannel peeringChannel;
    @XmlElement(required = true)
    protected Aalspace.CommunicationChannels communicationChannels;
    @XmlElement(required = true)
    protected String owner;
    @XmlElement(required = true)
    protected String admin;
    @XmlElement(required = true)
    protected String security;

    /**
     * Gets the value of the spaceDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link Aalspace.SpaceDescriptor }
     *     
     */
    public Aalspace.SpaceDescriptor getSpaceDescriptor() {
        return spaceDescriptor;
    }

    /**
     * Sets the value of the spaceDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Aalspace.SpaceDescriptor }
     *     
     */
    public void setSpaceDescriptor(Aalspace.SpaceDescriptor value) {
        this.spaceDescriptor = value;
    }

    public boolean isSetSpaceDescriptor() {
        return (this.spaceDescriptor!= null);
    }

    /**
     * Gets the value of the peeringChannel property.
     * 
     * @return
     *     possible object is
     *     {@link Aalspace.PeeringChannel }
     *     
     */
    public Aalspace.PeeringChannel getPeeringChannel() {
        return peeringChannel;
    }

    /**
     * Sets the value of the peeringChannel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Aalspace.PeeringChannel }
     *     
     */
    public void setPeeringChannel(Aalspace.PeeringChannel value) {
        this.peeringChannel = value;
    }

    public boolean isSetPeeringChannel() {
        return (this.peeringChannel!= null);
    }

    /**
     * Gets the value of the communicationChannels property.
     * 
     * @return
     *     possible object is
     *     {@link Aalspace.CommunicationChannels }
     *     
     */
    public Aalspace.CommunicationChannels getCommunicationChannels() {
        return communicationChannels;
    }

    /**
     * Sets the value of the communicationChannels property.
     * 
     * @param value
     *     allowed object is
     *     {@link Aalspace.CommunicationChannels }
     *     
     */
    public void setCommunicationChannels(Aalspace.CommunicationChannels value) {
        this.communicationChannels = value;
    }

    public boolean isSetCommunicationChannels() {
        return (this.communicationChannels!= null);
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    public boolean isSetOwner() {
        return (this.owner!= null);
    }

    /**
     * Gets the value of the admin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdmin() {
        return admin;
    }

    /**
     * Sets the value of the admin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdmin(String value) {
        this.admin = value;
    }

    public boolean isSetAdmin() {
        return (this.admin!= null);
    }

    /**
     * Gets the value of the security property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurity() {
        return security;
    }

    /**
     * Sets the value of the security property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurity(String value) {
        this.security = value;
    }

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
        implements Serializable
    {

        private final static long serialVersionUID = 12343L;
        @XmlElement(name = "channel-descriptor", required = true)
        protected List<ChannelDescriptor> channelDescriptor;

        /**
         * Gets the value of the channelDescriptor property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the channelDescriptor property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getChannelDescriptor().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ChannelDescriptor }
         * 
         * 
         */
        public List<ChannelDescriptor> getChannelDescriptor() {
            if (channelDescriptor == null) {
                channelDescriptor = new ArrayList<ChannelDescriptor>();
            }
            return this.channelDescriptor;
        }

        public boolean isSetChannelDescriptor() {
            return ((this.channelDescriptor!= null)&&(!this.channelDescriptor.isEmpty()));
        }

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
        implements Serializable
    {

        private final static long serialVersionUID = 12343L;
        @XmlElement(name = "channel-descriptor", required = true)
        protected ChannelDescriptor channelDescriptor;

        /**
         * Gets the value of the channelDescriptor property.
         * 
         * @return
         *     possible object is
         *     {@link ChannelDescriptor }
         *     
         */
        public ChannelDescriptor getChannelDescriptor() {
            return channelDescriptor;
        }

        /**
         * Sets the value of the channelDescriptor property.
         * 
         * @param value
         *     allowed object is
         *     {@link ChannelDescriptor }
         *     
         */
        public void setChannelDescriptor(ChannelDescriptor value) {
            this.channelDescriptor = value;
        }

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
        implements Serializable
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

        /**
         * Gets the value of the profile property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getProfile() {
            return profile;
        }

        /**
         * Sets the value of the profile property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setProfile(String value) {
            this.profile = value;
        }

        public boolean isSetProfile() {
            return (this.profile!= null);
        }

        /**
         * Gets the value of the spaceId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSpaceId() {
            return spaceId;
        }

        /**
         * Sets the value of the spaceId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSpaceId(String value) {
            this.spaceId = value;
        }

        public boolean isSetSpaceId() {
            return (this.spaceId!= null);
        }

        /**
         * Gets the value of the spaceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSpaceName() {
            return spaceName;
        }

        /**
         * Sets the value of the spaceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSpaceName(String value) {
            this.spaceName = value;
        }

        public boolean isSetSpaceName() {
            return (this.spaceName!= null);
        }

        /**
         * Gets the value of the spaceDescription property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSpaceDescription() {
            return spaceDescription;
        }

        /**
         * Sets the value of the spaceDescription property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSpaceDescription(String value) {
            this.spaceDescription = value;
        }

        public boolean isSetSpaceDescription() {
            return (this.spaceDescription!= null);
        }

    }

}
