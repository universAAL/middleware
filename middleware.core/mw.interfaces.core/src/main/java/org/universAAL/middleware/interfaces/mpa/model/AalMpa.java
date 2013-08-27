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
import java.util.ArrayList;
import java.util.List;
import ae.javax.xml.bind.annotation.XmlAccessType;
import ae.javax.xml.bind.annotation.XmlAccessorType;
import ae.javax.xml.bind.annotation.XmlElement;
import ae.javax.xml.bind.annotation.XmlRootElement;
import ae.javax.xml.bind.annotation.XmlSchemaType;
import ae.javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="app">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="distributed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                   &lt;element name="appId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="license" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="link" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="sla" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="applicationProfile">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="aal-space">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="targetProfile" type="{http://universaal.org/aal-mpa/v1.0.0}ProfileType"/>
 *                             &lt;element name="alternativeProfiles">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="profile" type="{http://universaal.org/aal-mpa/v1.0.0}ProfileType" maxOccurs="unbounded" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="requirredOntologies">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="ontology" type="{http://universaal.org/aal-mpa/v1.0.0}OntologyType" maxOccurs="unbounded" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="runtime">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="middleware" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType"/>
 *                             &lt;element name="broker">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element ref="{http://universaal.org/aal-mpa/v1.0.0}broker" maxOccurs="unbounded" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="managers">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="manager" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType" maxOccurs="unbounded" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="applicationProvider">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="webSite" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *                   &lt;element name="certificate" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="applicationManagement">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="contactPoint" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="remoteManagement">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="protocols" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                             &lt;element name="software" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="applicationPart">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://universaal.org/aal-mpa/v1.0.0}part" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "app", "applicationProfile",
	"applicationProvider", "applicationManagement", "applicationPart" })
@XmlRootElement(name = "aal-mpa")
public class AalMpa implements Serializable {

    private final static long serialVersionUID = 12343L;
    @XmlElement(required = true)
    protected AalMpa.App app;
    @XmlElement(required = true)
    protected AalMpa.ApplicationProfile applicationProfile;
    @XmlElement(required = true)
    protected AalMpa.ApplicationProvider applicationProvider;
    @XmlElement(required = true)
    protected AalMpa.ApplicationManagement applicationManagement;
    @XmlElement(required = true)
    protected AalMpa.ApplicationPart applicationPart;

    /**
     * Gets the value of the app property.
     * 
     * @return possible object is {@link AalMpa.App }
     * 
     */
    public AalMpa.App getApp() {
	return app;
    }

    /**
     * Sets the value of the app property.
     * 
     * @param value
     *            allowed object is {@link AalMpa.App }
     * 
     */
    public void setApp(AalMpa.App value) {
	this.app = value;
    }

    public boolean isSetApp() {
	return (this.app != null);
    }

    /**
     * Gets the value of the applicationProfile property.
     * 
     * @return possible object is {@link AalMpa.ApplicationProfile }
     * 
     */
    public AalMpa.ApplicationProfile getApplicationProfile() {
	return applicationProfile;
    }

    /**
     * Sets the value of the applicationProfile property.
     * 
     * @param value
     *            allowed object is {@link AalMpa.ApplicationProfile }
     * 
     */
    public void setApplicationProfile(AalMpa.ApplicationProfile value) {
	this.applicationProfile = value;
    }

    public boolean isSetApplicationProfile() {
	return (this.applicationProfile != null);
    }

    /**
     * Gets the value of the applicationProvider property.
     * 
     * @return possible object is {@link AalMpa.ApplicationProvider }
     * 
     */
    public AalMpa.ApplicationProvider getApplicationProvider() {
	return applicationProvider;
    }

    /**
     * Sets the value of the applicationProvider property.
     * 
     * @param value
     *            allowed object is {@link AalMpa.ApplicationProvider }
     * 
     */
    public void setApplicationProvider(AalMpa.ApplicationProvider value) {
	this.applicationProvider = value;
    }

    public boolean isSetApplicationProvider() {
	return (this.applicationProvider != null);
    }

    /**
     * Gets the value of the applicationManagement property.
     * 
     * @return possible object is {@link AalMpa.ApplicationManagement }
     * 
     */
    public AalMpa.ApplicationManagement getApplicationManagement() {
	return applicationManagement;
    }

    /**
     * Sets the value of the applicationManagement property.
     * 
     * @param value
     *            allowed object is {@link AalMpa.ApplicationManagement }
     * 
     */
    public void setApplicationManagement(AalMpa.ApplicationManagement value) {
	this.applicationManagement = value;
    }

    public boolean isSetApplicationManagement() {
	return (this.applicationManagement != null);
    }

    /**
     * Gets the value of the applicationPart property.
     * 
     * @return possible object is {@link AalMpa.ApplicationPart }
     * 
     */
    public AalMpa.ApplicationPart getApplicationPart() {
	return applicationPart;
    }

    /**
     * Sets the value of the applicationPart property.
     * 
     * @param value
     *            allowed object is {@link AalMpa.ApplicationPart }
     * 
     */
    public void setApplicationPart(AalMpa.ApplicationPart value) {
	this.applicationPart = value;
    }

    public boolean isSetApplicationPart() {
	return (this.applicationPart != null);
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="distributed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *         &lt;element name="appId" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="license" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="link" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="sla" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "name", "distributed", "appId",
	    "description", "license", "sla" })
    public static class App implements Serializable {

	private final static long serialVersionUID = 12343L;
	@XmlElement(required = true)
	protected String name;
	protected boolean distributed;
	@XmlElement(required = true)
	protected String appId;
	@XmlElement(required = true)
	protected String description;
	protected AalMpa.App.License license;
	protected String sla;

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
	    return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
	    this.name = value;
	}

	public boolean isSetName() {
	    return (this.name != null);
	}

	/**
	 * Gets the value of the distributed property.
	 * 
	 */
	public boolean isDistributed() {
	    return distributed;
	}

	/**
	 * Sets the value of the distributed property.
	 * 
	 */
	public void setDistributed(boolean value) {
	    this.distributed = value;
	}

	public boolean isSetDistributed() {
	    return true;
	}

	/**
	 * Gets the value of the appId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAppId() {
	    return appId;
	}

	/**
	 * Sets the value of the appId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAppId(String value) {
	    this.appId = value;
	}

	public boolean isSetAppId() {
	    return (this.appId != null);
	}

	/**
	 * Gets the value of the description property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDescription() {
	    return description;
	}

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDescription(String value) {
	    this.description = value;
	}

	public boolean isSetDescription() {
	    return (this.description != null);
	}

	/**
	 * Gets the value of the license property.
	 * 
	 * @return possible object is {@link AalMpa.App.License }
	 * 
	 */
	public AalMpa.App.License getLicense() {
	    return license;
	}

	/**
	 * Sets the value of the license property.
	 * 
	 * @param value
	 *            allowed object is {@link AalMpa.App.License }
	 * 
	 */
	public void setLicense(AalMpa.App.License value) {
	    this.license = value;
	}

	public boolean isSetLicense() {
	    return (this.license != null);
	}

	/**
	 * Gets the value of the sla property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSla() {
	    return sla;
	}

	/**
	 * Sets the value of the sla property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSla(String value) {
	    this.sla = value;
	}

	public boolean isSetSla() {
	    return (this.sla != null);
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content
	 * contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;element name="link" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "name", "link" })
	public static class License implements Serializable {

	    private final static long serialVersionUID = 12343L;
	    @XmlElement(required = true)
	    protected String name;
	    @XmlElement(required = true)
	    @XmlSchemaType(name = "anyURI")
	    protected String link;

	    /**
	     * Gets the value of the name property.
	     * 
	     * @return possible object is {@link String }
	     * 
	     */
	    public String getName() {
		return name;
	    }

	    /**
	     * Sets the value of the name property.
	     * 
	     * @param value
	     *            allowed object is {@link String }
	     * 
	     */
	    public void setName(String value) {
		this.name = value;
	    }

	    public boolean isSetName() {
		return (this.name != null);
	    }

	    /**
	     * Gets the value of the link property.
	     * 
	     * @return possible object is {@link String }
	     * 
	     */
	    public String getLink() {
		return link;
	    }

	    /**
	     * Sets the value of the link property.
	     * 
	     * @param value
	     *            allowed object is {@link String }
	     * 
	     */
	    public void setLink(String value) {
		this.link = value;
	    }

	    public boolean isSetLink() {
		return (this.link != null);
	    }

	}

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="contactPoint" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="remoteManagement">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="protocols" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
     *                   &lt;element name="software" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "contactPoint", "remoteManagement" })
    public static class ApplicationManagement implements Serializable {

	private final static long serialVersionUID = 12343L;
	@XmlElement(required = true)
	protected String contactPoint;
	@XmlElement(required = true)
	protected AalMpa.ApplicationManagement.RemoteManagement remoteManagement;

	/**
	 * Gets the value of the contactPoint property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getContactPoint() {
	    return contactPoint;
	}

	/**
	 * Sets the value of the contactPoint property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setContactPoint(String value) {
	    this.contactPoint = value;
	}

	public boolean isSetContactPoint() {
	    return (this.contactPoint != null);
	}

	/**
	 * Gets the value of the remoteManagement property.
	 * 
	 * @return possible object is
	 *         {@link AalMpa.ApplicationManagement.RemoteManagement }
	 * 
	 */
	public AalMpa.ApplicationManagement.RemoteManagement getRemoteManagement() {
	    return remoteManagement;
	}

	/**
	 * Sets the value of the remoteManagement property.
	 * 
	 * @param value
	 *            allowed object is
	 *            {@link AalMpa.ApplicationManagement.RemoteManagement }
	 * 
	 */
	public void setRemoteManagement(
		AalMpa.ApplicationManagement.RemoteManagement value) {
	    this.remoteManagement = value;
	}

	public boolean isSetRemoteManagement() {
	    return (this.remoteManagement != null);
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content
	 * contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="protocols" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
	 *         &lt;element name="software" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "protocols", "software" })
	public static class RemoteManagement implements Serializable {

	    private final static long serialVersionUID = 12343L;
	    @XmlElement(required = true)
	    protected List<String> protocols;
	    @XmlElement(required = true)
	    protected ArtifactType software;

	    /**
	     * Gets the value of the protocols property.
	     * 
	     * <p>
	     * This accessor method returns a reference to the live list, not a
	     * snapshot. Therefore any modification you make to the returned
	     * list will be present inside the JAXB object. This is why there is
	     * not a <CODE>set</CODE> method for the protocols property.
	     * 
	     * <p>
	     * For example, to add a new item, do as follows:
	     * 
	     * <pre>
	     * getProtocols().add(newItem);
	     * </pre>
	     * 
	     * 
	     * <p>
	     * Objects of the following type(s) are allowed in the list
	     * {@link String }
	     * 
	     * 
	     */
	    public List<String> getProtocols() {
		if (protocols == null) {
		    protocols = new ArrayList<String>();
		}
		return this.protocols;
	    }

	    public boolean isSetProtocols() {
		return ((this.protocols != null) && (!this.protocols.isEmpty()));
	    }

	    public void unsetProtocols() {
		this.protocols = null;
	    }

	    /**
	     * Gets the value of the software property.
	     * 
	     * @return possible object is {@link ArtifactType }
	     * 
	     */
	    public ArtifactType getSoftware() {
		return software;
	    }

	    /**
	     * Sets the value of the software property.
	     * 
	     * @param value
	     *            allowed object is {@link ArtifactType }
	     * 
	     */
	    public void setSoftware(ArtifactType value) {
		this.software = value;
	    }

	    public boolean isSetSoftware() {
		return (this.software != null);
	    }

	}

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{http://universaal.org/aal-mpa/v1.0.0}part" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "part" })
    public static class ApplicationPart implements Serializable {

	private final static long serialVersionUID = 12343L;
	@XmlElement(required = true)
	protected List<Part> part;

	/**
	 * Gets the value of the part property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list
	 * will be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the part property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPart().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Part }
	 * 
	 * 
	 */
	public List<Part> getPart() {
	    if (part == null) {
		part = new ArrayList<Part>();
	    }
	    return this.part;
	}

	public boolean isSetPart() {
	    return ((this.part != null) && (!this.part.isEmpty()));
	}

	public void unsetPart() {
	    this.part = null;
	}

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="aal-space">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="targetProfile" type="{http://universaal.org/aal-mpa/v1.0.0}ProfileType"/>
     *                   &lt;element name="alternativeProfiles">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="profile" type="{http://universaal.org/aal-mpa/v1.0.0}ProfileType" maxOccurs="unbounded" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="requirredOntologies">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="ontology" type="{http://universaal.org/aal-mpa/v1.0.0}OntologyType" maxOccurs="unbounded" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="runtime">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="middleware" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType"/>
     *                   &lt;element name="broker">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element ref="{http://universaal.org/aal-mpa/v1.0.0}broker" maxOccurs="unbounded" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="managers">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="manager" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType" maxOccurs="unbounded" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "aalSpace", "runtime" })
    public static class ApplicationProfile implements Serializable {

	private final static long serialVersionUID = 12343L;
	@XmlElement(name = "aal-space", required = true)
	protected AalMpa.ApplicationProfile.AalSpace aalSpace;
	@XmlElement(required = true)
	protected AalMpa.ApplicationProfile.Runtime runtime;

	/**
	 * Gets the value of the aalSpace property.
	 * 
	 * @return possible object is {@link AalMpa.ApplicationProfile.AalSpace }
	 * 
	 */
	public AalMpa.ApplicationProfile.AalSpace getAalSpace() {
	    return aalSpace;
	}

	/**
	 * Sets the value of the aalSpace property.
	 * 
	 * @param value
	 *            allowed object is
	 *            {@link AalMpa.ApplicationProfile.AalSpace }
	 * 
	 */
	public void setAalSpace(AalMpa.ApplicationProfile.AalSpace value) {
	    this.aalSpace = value;
	}

	public boolean isSetAalSpace() {
	    return (this.aalSpace != null);
	}

	/**
	 * Gets the value of the runtime property.
	 * 
	 * @return possible object is {@link AalMpa.ApplicationProfile.Runtime }
	 * 
	 */
	public AalMpa.ApplicationProfile.Runtime getRuntime() {
	    return runtime;
	}

	/**
	 * Sets the value of the runtime property.
	 * 
	 * @param value
	 *            allowed object is
	 *            {@link AalMpa.ApplicationProfile.Runtime }
	 * 
	 */
	public void setRuntime(AalMpa.ApplicationProfile.Runtime value) {
	    this.runtime = value;
	}

	public boolean isSetRuntime() {
	    return (this.runtime != null);
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content
	 * contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="targetProfile" type="{http://universaal.org/aal-mpa/v1.0.0}ProfileType"/>
	 *         &lt;element name="alternativeProfiles">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="profile" type="{http://universaal.org/aal-mpa/v1.0.0}ProfileType" maxOccurs="unbounded" minOccurs="0"/>
	 *                 &lt;/sequence>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *         &lt;element name="requirredOntologies">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="ontology" type="{http://universaal.org/aal-mpa/v1.0.0}OntologyType" maxOccurs="unbounded" minOccurs="0"/>
	 *                 &lt;/sequence>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "targetProfile",
		"alternativeProfiles", "requirredOntologies" })
	public static class AalSpace implements Serializable {

	    private final static long serialVersionUID = 12343L;
	    @XmlElement(required = true)
	    protected ProfileType targetProfile;
	    @XmlElement(required = true)
	    protected AalMpa.ApplicationProfile.AalSpace.AlternativeProfiles alternativeProfiles;
	    @XmlElement(required = true)
	    protected AalMpa.ApplicationProfile.AalSpace.RequirredOntologies requirredOntologies;

	    /**
	     * Gets the value of the targetProfile property.
	     * 
	     * @return possible object is {@link ProfileType }
	     * 
	     */
	    public ProfileType getTargetProfile() {
		return targetProfile;
	    }

	    /**
	     * Sets the value of the targetProfile property.
	     * 
	     * @param value
	     *            allowed object is {@link ProfileType }
	     * 
	     */
	    public void setTargetProfile(ProfileType value) {
		this.targetProfile = value;
	    }

	    public boolean isSetTargetProfile() {
		return (this.targetProfile != null);
	    }

	    /**
	     * Gets the value of the alternativeProfiles property.
	     * 
	     * @return possible object is
	     *         {@link AalMpa.ApplicationProfile.AalSpace.AlternativeProfiles }
	     * 
	     */
	    public AalMpa.ApplicationProfile.AalSpace.AlternativeProfiles getAlternativeProfiles() {
		return alternativeProfiles;
	    }

	    /**
	     * Sets the value of the alternativeProfiles property.
	     * 
	     * @param value
	     *            allowed object is
	     *            {@link AalMpa.ApplicationProfile.AalSpace.AlternativeProfiles }
	     * 
	     */
	    public void setAlternativeProfiles(
		    AalMpa.ApplicationProfile.AalSpace.AlternativeProfiles value) {
		this.alternativeProfiles = value;
	    }

	    public boolean isSetAlternativeProfiles() {
		return (this.alternativeProfiles != null);
	    }

	    /**
	     * Gets the value of the requirredOntologies property.
	     * 
	     * @return possible object is
	     *         {@link AalMpa.ApplicationProfile.AalSpace.RequirredOntologies }
	     * 
	     */
	    public AalMpa.ApplicationProfile.AalSpace.RequirredOntologies getRequirredOntologies() {
		return requirredOntologies;
	    }

	    /**
	     * Sets the value of the requirredOntologies property.
	     * 
	     * @param value
	     *            allowed object is
	     *            {@link AalMpa.ApplicationProfile.AalSpace.RequirredOntologies }
	     * 
	     */
	    public void setRequirredOntologies(
		    AalMpa.ApplicationProfile.AalSpace.RequirredOntologies value) {
		this.requirredOntologies = value;
	    }

	    public boolean isSetRequirredOntologies() {
		return (this.requirredOntologies != null);
	    }

	    /**
	     * <p>
	     * Java class for anonymous complex type.
	     * 
	     * <p>
	     * The following schema fragment specifies the expected content
	     * contained within this class.
	     * 
	     * <pre>
	     * &lt;complexType>
	     *   &lt;complexContent>
	     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	     *       &lt;sequence>
	     *         &lt;element name="profile" type="{http://universaal.org/aal-mpa/v1.0.0}ProfileType" maxOccurs="unbounded" minOccurs="0"/>
	     *       &lt;/sequence>
	     *     &lt;/restriction>
	     *   &lt;/complexContent>
	     * &lt;/complexType>
	     * </pre>
	     * 
	     * 
	     */
	    @XmlAccessorType(XmlAccessType.FIELD)
	    @XmlType(name = "", propOrder = { "profile" })
	    public static class AlternativeProfiles implements Serializable {

		private final static long serialVersionUID = 12343L;
		protected List<ProfileType> profile;

		/**
		 * Gets the value of the profile property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list,
		 * not a snapshot. Therefore any modification you make to the
		 * returned list will be present inside the JAXB object. This is
		 * why there is not a <CODE>set</CODE> method for the profile
		 * property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getProfile().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link ProfileType }
		 * 
		 * 
		 */
		public List<ProfileType> getProfile() {
		    if (profile == null) {
			profile = new ArrayList<ProfileType>();
		    }
		    return this.profile;
		}

		public boolean isSetProfile() {
		    return ((this.profile != null) && (!this.profile.isEmpty()));
		}

		public void unsetProfile() {
		    this.profile = null;
		}

	    }

	    /**
	     * <p>
	     * Java class for anonymous complex type.
	     * 
	     * <p>
	     * The following schema fragment specifies the expected content
	     * contained within this class.
	     * 
	     * <pre>
	     * &lt;complexType>
	     *   &lt;complexContent>
	     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	     *       &lt;sequence>
	     *         &lt;element name="ontology" type="{http://universaal.org/aal-mpa/v1.0.0}OntologyType" maxOccurs="unbounded" minOccurs="0"/>
	     *       &lt;/sequence>
	     *     &lt;/restriction>
	     *   &lt;/complexContent>
	     * &lt;/complexType>
	     * </pre>
	     * 
	     * 
	     */
	    @XmlAccessorType(XmlAccessType.FIELD)
	    @XmlType(name = "", propOrder = { "ontology" })
	    public static class RequirredOntologies implements Serializable {

		private final static long serialVersionUID = 12343L;
		protected List<OntologyType> ontology;

		/**
		 * Gets the value of the ontology property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list,
		 * not a snapshot. Therefore any modification you make to the
		 * returned list will be present inside the JAXB object. This is
		 * why there is not a <CODE>set</CODE> method for the ontology
		 * property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getOntology().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link OntologyType }
		 * 
		 * 
		 */
		public List<OntologyType> getOntology() {
		    if (ontology == null) {
			ontology = new ArrayList<OntologyType>();
		    }
		    return this.ontology;
		}

		public boolean isSetOntology() {
		    return ((this.ontology != null) && (!this.ontology
			    .isEmpty()));
		}

		public void unsetOntology() {
		    this.ontology = null;
		}

	    }

	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content
	 * contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="middleware" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType"/>
	 *         &lt;element name="broker">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element ref="{http://universaal.org/aal-mpa/v1.0.0}broker" maxOccurs="unbounded" minOccurs="0"/>
	 *                 &lt;/sequence>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *         &lt;element name="managers">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="manager" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType" maxOccurs="unbounded" minOccurs="0"/>
	 *                 &lt;/sequence>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "middleware", "broker", "managers" })
	public static class Runtime implements Serializable {

	    private final static long serialVersionUID = 12343L;
	    @XmlElement(required = true)
	    protected ArtifactType middleware;
	    @XmlElement(required = true)
	    protected AalMpa.ApplicationProfile.Runtime.Broker broker;
	    @XmlElement(required = true)
	    protected AalMpa.ApplicationProfile.Runtime.Managers managers;

	    /**
	     * Gets the value of the middleware property.
	     * 
	     * @return possible object is {@link ArtifactType }
	     * 
	     */
	    public ArtifactType getMiddleware() {
		return middleware;
	    }

	    /**
	     * Sets the value of the middleware property.
	     * 
	     * @param value
	     *            allowed object is {@link ArtifactType }
	     * 
	     */
	    public void setMiddleware(ArtifactType value) {
		this.middleware = value;
	    }

	    public boolean isSetMiddleware() {
		return (this.middleware != null);
	    }

	    /**
	     * Gets the value of the broker property.
	     * 
	     * @return possible object is
	     *         {@link AalMpa.ApplicationProfile.Runtime.Broker }
	     * 
	     */
	    public AalMpa.ApplicationProfile.Runtime.Broker getBroker() {
		return broker;
	    }

	    /**
	     * Sets the value of the broker property.
	     * 
	     * @param value
	     *            allowed object is
	     *            {@link AalMpa.ApplicationProfile.Runtime.Broker }
	     * 
	     */
	    public void setBroker(AalMpa.ApplicationProfile.Runtime.Broker value) {
		this.broker = value;
	    }

	    public boolean isSetBroker() {
		return (this.broker != null);
	    }

	    /**
	     * Gets the value of the managers property.
	     * 
	     * @return possible object is
	     *         {@link AalMpa.ApplicationProfile.Runtime.Managers }
	     * 
	     */
	    public AalMpa.ApplicationProfile.Runtime.Managers getManagers() {
		return managers;
	    }

	    /**
	     * Sets the value of the managers property.
	     * 
	     * @param value
	     *            allowed object is
	     *            {@link AalMpa.ApplicationProfile.Runtime.Managers }
	     * 
	     */
	    public void setManagers(
		    AalMpa.ApplicationProfile.Runtime.Managers value) {
		this.managers = value;
	    }

	    public boolean isSetManagers() {
		return (this.managers != null);
	    }

	    /**
	     * <p>
	     * Java class for anonymous complex type.
	     * 
	     * <p>
	     * The following schema fragment specifies the expected content
	     * contained within this class.
	     * 
	     * <pre>
	     * &lt;complexType>
	     *   &lt;complexContent>
	     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	     *       &lt;sequence>
	     *         &lt;element ref="{http://universaal.org/aal-mpa/v1.0.0}broker" maxOccurs="unbounded" minOccurs="0"/>
	     *       &lt;/sequence>
	     *     &lt;/restriction>
	     *   &lt;/complexContent>
	     * &lt;/complexType>
	     * </pre>
	     * 
	     * 
	     */
	    @XmlAccessorType(XmlAccessType.FIELD)
	    @XmlType(name = "", propOrder = { "broker" })
	    public static class Broker implements Serializable {

		private final static long serialVersionUID = 12343L;
		protected List<org.universAAL.middleware.interfaces.mpa.model.Broker> broker;

		/**
		 * Gets the value of the broker property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list,
		 * not a snapshot. Therefore any modification you make to the
		 * returned list will be present inside the JAXB object. This is
		 * why there is not a <CODE>set</CODE> method for the broker
		 * property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getBroker().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link org.universAAL.middleware.connectors.deploy.model.Broker }
		 * 
		 * 
		 */
		public List<org.universAAL.middleware.interfaces.mpa.model.Broker> getBroker() {
		    if (broker == null) {
			broker = new ArrayList<org.universAAL.middleware.interfaces.mpa.model.Broker>();
		    }
		    return this.broker;
		}

		public boolean isSetBroker() {
		    return ((this.broker != null) && (!this.broker.isEmpty()));
		}

		public void unsetBroker() {
		    this.broker = null;
		}

	    }

	    /**
	     * <p>
	     * Java class for anonymous complex type.
	     * 
	     * <p>
	     * The following schema fragment specifies the expected content
	     * contained within this class.
	     * 
	     * <pre>
	     * &lt;complexType>
	     *   &lt;complexContent>
	     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	     *       &lt;sequence>
	     *         &lt;element name="manager" type="{http://universaal.org/aal-mpa/v1.0.0}ArtifactType" maxOccurs="unbounded" minOccurs="0"/>
	     *       &lt;/sequence>
	     *     &lt;/restriction>
	     *   &lt;/complexContent>
	     * &lt;/complexType>
	     * </pre>
	     * 
	     * 
	     */
	    @XmlAccessorType(XmlAccessType.FIELD)
	    @XmlType(name = "", propOrder = { "manager" })
	    public static class Managers implements Serializable {

		private final static long serialVersionUID = 12343L;
		protected List<ArtifactType> manager;

		/**
		 * Gets the value of the manager property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list,
		 * not a snapshot. Therefore any modification you make to the
		 * returned list will be present inside the JAXB object. This is
		 * why there is not a <CODE>set</CODE> method for the manager
		 * property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getManager().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link ArtifactType }
		 * 
		 * 
		 */
		public List<ArtifactType> getManager() {
		    if (manager == null) {
			manager = new ArrayList<ArtifactType>();
		    }
		    return this.manager;
		}

		public boolean isSetManager() {
		    return ((this.manager != null) && (!this.manager.isEmpty()));
		}

		public void unsetManager() {
		    this.manager = null;
		}

	    }

	}

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="webSite" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
     *         &lt;element name="certificate" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "name", "webSite", "certificate" })
    public static class ApplicationProvider implements Serializable {

	private final static long serialVersionUID = 12343L;
	@XmlElement(required = true)
	protected String name;
	@XmlElement(required = true)
	@XmlSchemaType(name = "anyURI")
	protected String webSite;
	@XmlElement(required = true)
	@XmlSchemaType(name = "anyURI")
	protected String certificate;

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
	    return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
	    this.name = value;
	}

	public boolean isSetName() {
	    return (this.name != null);
	}

	/**
	 * Gets the value of the webSite property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getWebSite() {
	    return webSite;
	}

	/**
	 * Sets the value of the webSite property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setWebSite(String value) {
	    this.webSite = value;
	}

	public boolean isSetWebSite() {
	    return (this.webSite != null);
	}

	/**
	 * Gets the value of the certificate property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCertificate() {
	    return certificate;
	}

	/**
	 * Sets the value of the certificate property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCertificate(String value) {
	    this.certificate = value;
	}

	public boolean isSetCertificate() {
	    return (this.certificate != null);
	}

    }

}
