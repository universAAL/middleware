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
import ae.javax.xml.bind.annotation.XmlAccessType;
import ae.javax.xml.bind.annotation.XmlAccessorType;
import ae.javax.xml.bind.annotation.XmlElement;
import ae.javax.xml.bind.annotation.XmlType;


/**
 * This schema describes the meta-information of an
 *         AALSpace Channel
 *       
 * 
 * <p>Java class for channelDescriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="channelDescriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="channelName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="channelURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="channelValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "channelDescriptor", propOrder = {
    "channelName",
    "channelURL",
    "channelValue"
})
public class ChannelDescriptor
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(namespace = "http://universaal.org/aalspace-channel/v1.0.0", required = true)
    protected String channelName;
    @XmlElement(namespace = "http://universaal.org/aalspace-channel/v1.0.0", required = true)
    protected String channelURL;
    @XmlElement(namespace = "http://universaal.org/aalspace-channel/v1.0.0", required = true)
    protected String channelValue;

    /**
     * Gets the value of the channelName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Sets the value of the channelName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannelName(String value) {
        this.channelName = value;
    }

    public boolean isSetChannelName() {
        return (this.channelName!= null);
    }

    /**
     * Gets the value of the channelURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannelURL() {
        return channelURL;
    }

    /**
     * Sets the value of the channelURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannelURL(String value) {
        this.channelURL = value;
    }

    public boolean isSetChannelURL() {
        return (this.channelURL!= null);
    }

    /**
     * Gets the value of the channelValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannelValue() {
        return channelValue;
    }

    /**
     * Sets the value of the channelValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannelValue(String value) {
        this.channelValue = value;
    }

    public boolean isSetChannelValue() {
        return (this.channelValue!= null);
    }

}
