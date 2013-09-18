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

/**
 * 
 * 
 * @author <a href="mailto:sterfano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * @since 2.0.1
 */
public interface ISpaceDescriptor {

    /**
     * Gets the value of the profile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public abstract String getProfile();

    /**
     * Sets the value of the profile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public abstract void setProfile(String value);

    public abstract boolean isSetProfile();

    /**
     * Gets the value of the spaceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public abstract String getSpaceId();

    /**
     * Sets the value of the spaceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public abstract void setSpaceId(String value);

    public abstract boolean isSetSpaceId();

    /**
     * Gets the value of the spaceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public abstract String getSpaceName();

    /**
     * Sets the value of the spaceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public abstract void setSpaceName(String value);

    public abstract boolean isSetSpaceName();

    /**
     * Gets the value of the spaceDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public abstract String getSpaceDescription();

    /**
     * Sets the value of the spaceDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public abstract void setSpaceDescription(String value);

    public abstract boolean isSetSpaceDescription();

}