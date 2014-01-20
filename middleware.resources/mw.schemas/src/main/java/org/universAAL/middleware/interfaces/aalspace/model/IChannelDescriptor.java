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

public interface IChannelDescriptor {

    /**
     * Gets the value of the channelName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public abstract String getChannelName();

    /**
     * Sets the value of the channelName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public abstract void setChannelName(String value);

    public abstract boolean isSetChannelName();

    /**
     * Gets the value of the channelURL property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public abstract String getChannelURL();

    /**
     * Sets the value of the channelURL property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public abstract void setChannelURL(String value);

    public abstract boolean isSetChannelURL();

    /**
     * Gets the value of the channelValue property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public abstract String getChannelValue();

    /**
     * Sets the value of the channelValue property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public abstract void setChannelValue(String value);

    public abstract boolean isSetChannelValue();

}
