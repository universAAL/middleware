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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor;


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
    implements Serializable, IChannelDescriptor
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(namespace = "http://universaal.org/aalspace-channel/v1.0.0", required = true)
    protected String channelName;
    @XmlElement(namespace = "http://universaal.org/aalspace-channel/v1.0.0", required = true)
    protected String channelURL;
    @XmlElement(namespace = "http://universaal.org/aalspace-channel/v1.0.0", required = true)
    protected String channelValue;

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#getChannelName()
     */
    public String getChannelName() {
        return channelName;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#setChannelName(java.lang.String)
     */
    public void setChannelName(String value) {
        this.channelName = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#isSetChannelName()
     */
    public boolean isSetChannelName() {
        return (this.channelName!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#getChannelURL()
     */
    public String getChannelURL() {
        return channelURL;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#setChannelURL(java.lang.String)
     */
    public void setChannelURL(String value) {
        this.channelURL = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#isSetChannelURL()
     */
    public boolean isSetChannelURL() {
        return (this.channelURL!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#getChannelValue()
     */
    public String getChannelValue() {
        return channelValue;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#setChannelValue(java.lang.String)
     */
    public void setChannelValue(String value) {
        this.channelValue = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#isSetChannelValue()
     */
    public boolean isSetChannelValue() {
        return (this.channelValue!= null);
    }

}
