//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2013.08.05 at 03:28:04 PM CEST
//


package org.universAAL.middleware.deploymanager.uapp.model;

import java.io.Serializable;
import ae.javax.xml.bind.annotation.XmlAccessType;
import ae.javax.xml.bind.annotation.XmlAccessorType;
import ae.javax.xml.bind.annotation.XmlElement;
import ae.javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for profileType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="profileType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="profileId" type="{http://www.universaal.org/aal-uapp/v1.0.2}spaceType"/>
 *         &lt;element name="version" type="{http://www.universaal.org/aal-uapp/v1.0.2}versionType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "profileType", propOrder = {
    "profileId",
    "version"
})
public class ProfileType
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(required = true)
    protected SpaceType profileId;
    @XmlElement(required = true)
    protected VersionType version;

    /**
     * Gets the value of the profileId property.
     *
     * @return
     *     possible object is
     *     {@link SpaceType }
     *
     */
    public SpaceType getProfileId() {
        return profileId;
    }

    /**
     * Sets the value of the profileId property.
     *
     * @param value
     *     allowed object is
     *     {@link SpaceType }
     *
     */
    public void setProfileId(SpaceType value) {
        this.profileId = value;
    }

    public boolean isSetProfileId() {
        return (this.profileId!= null);
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link VersionType }
     *
     */
    public VersionType getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link VersionType }
     *
     */
    public void setVersion(VersionType value) {
        this.version = value;
    }

    public boolean isSetVersion() {
        return (this.version!= null);
    }

}
