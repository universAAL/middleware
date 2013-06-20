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
package org.universAAL.middleware.interfaces.mpa.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ProfileType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ProfileType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="profileId" type="{http://universaal.org/aal-mpa/v1.0.0}SpaceType"/>
 *         &lt;element name="version" type="{http://universaal.org/aal-mpa/v1.0.0}VersionType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProfileType", propOrder = { "profileId", "version" })
public class ProfileType implements Serializable {

    private final static long serialVersionUID = 12343L;
    @XmlElement(required = true)
    protected SpaceType profileId;
    @XmlElement(required = true)
    protected VersionType version;

    /**
     * Gets the value of the profileId property.
     * 
     * @return possible object is {@link SpaceType }
     * 
     */
    public SpaceType getProfileId() {
	return profileId;
    }

    /**
     * Sets the value of the profileId property.
     * 
     * @param value
     *            allowed object is {@link SpaceType }
     * 
     */
    public void setProfileId(SpaceType value) {
	this.profileId = value;
    }

    public boolean isSetProfileId() {
	return (this.profileId != null);
    }

    /**
     * Gets the value of the version property.
     * 
     * @return possible object is {@link VersionType }
     * 
     */
    public VersionType getVersion() {
	return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *            allowed object is {@link VersionType }
     * 
     */
    public void setVersion(VersionType value) {
	this.version = value;
    }

    public boolean isSetVersion() {
	return (this.version != null);
    }

}
