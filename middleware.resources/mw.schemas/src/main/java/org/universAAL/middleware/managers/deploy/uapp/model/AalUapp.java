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

package org.universAAL.middleware.managers.deploy.uapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

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
 *                   &lt;element name="version" type="{http://www.universaal.org/aal-uapp/v1.0.2}versionType"/>
 *                   &lt;element name="appId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="multipart" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                   &lt;element name="tags" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="applicationProvider" type="{http://www.universaal.org/aal-uapp/v1.0.2}contactType"/>
 *                   &lt;element name="licenses" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="license" type="{http://www.universaal.org/aal-uapp/v1.0.2}licenseType" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="sla" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                       &lt;element name="link" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
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
 *                   &lt;element name="applicationProfile" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="applicationOntology" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="menuEntry" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="menuName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="serviceUri" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *                             &lt;element name="icon" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;choice>
 *                                       &lt;element name="path" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *                                       &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                     &lt;/choice>
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
 *         &lt;element name="applicationCapabilities" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="capability" type="{http://www.universaal.org/aal-uapp/v1.0.2}capabilityType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="applicationRequirements" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="requirement" type="{http://www.universaal.org/aal-uapp/v1.0.2}reqType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="applicationManagement" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="contactPoint" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="remoteManagement" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="protocols" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                             &lt;element name="software" type="{http://www.universaal.org/aal-uapp/v1.0.2}artifactType"/>
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
 *                   &lt;element ref="{http://www.universaal.org/aal-uapp/v1.0.2}part" maxOccurs="unbounded"/>
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
@XmlType(name = "", propOrder = { "app", "applicationCapabilities", "applicationRequirements", "applicationManagement",
		"applicationPart" })
@XmlRootElement(name = "aal-uapp")
public class AalUapp implements Serializable {

	private final static long serialVersionUID = 12343L;
	@XmlElement(required = true)
	protected AalUapp.App app;
	protected AalUapp.ApplicationCapabilities applicationCapabilities;
	protected AalUapp.ApplicationRequirements applicationRequirements;
	protected AalUapp.ApplicationManagement applicationManagement;
	@XmlElement(required = true)
	protected AalUapp.ApplicationPart applicationPart;

	/**
	 * Gets the value of the app property.
	 *
	 * @return possible object is {@link AalUapp.App }
	 *
	 */
	public AalUapp.App getApp() {
		return app;
	}

	/**
	 * Sets the value of the app property.
	 *
	 * @param value
	 *            allowed object is {@link AalUapp.App }
	 *
	 */
	public void setApp(AalUapp.App value) {
		this.app = value;
	}

	public boolean isSetApp() {
		return (this.app != null);
	}

	/**
	 * Gets the value of the applicationCapabilities property.
	 *
	 * @return possible object is {@link AalUapp.ApplicationCapabilities }
	 *
	 */
	public AalUapp.ApplicationCapabilities getApplicationCapabilities() {
		return applicationCapabilities;
	}

	/**
	 * Sets the value of the applicationCapabilities property.
	 *
	 * @param value
	 *            allowed object is {@link AalUapp.ApplicationCapabilities }
	 *
	 */
	public void setApplicationCapabilities(AalUapp.ApplicationCapabilities value) {
		this.applicationCapabilities = value;
	}

	public boolean isSetApplicationCapabilities() {
		return (this.applicationCapabilities != null);
	}

	/**
	 * Gets the value of the applicationRequirements property.
	 *
	 * @return possible object is {@link AalUapp.ApplicationRequirements }
	 *
	 */
	public AalUapp.ApplicationRequirements getApplicationRequirements() {
		return applicationRequirements;
	}

	/**
	 * Sets the value of the applicationRequirements property.
	 *
	 * @param value
	 *            allowed object is {@link AalUapp.ApplicationRequirements }
	 *
	 */
	public void setApplicationRequirements(AalUapp.ApplicationRequirements value) {
		this.applicationRequirements = value;
	}

	public boolean isSetApplicationRequirements() {
		return (this.applicationRequirements != null);
	}

	/**
	 * Gets the value of the applicationManagement property.
	 *
	 * @return possible object is {@link AalUapp.ApplicationManagement }
	 *
	 */
	public AalUapp.ApplicationManagement getApplicationManagement() {
		return applicationManagement;
	}

	/**
	 * Sets the value of the applicationManagement property.
	 *
	 * @param value
	 *            allowed object is {@link AalUapp.ApplicationManagement }
	 *
	 */
	public void setApplicationManagement(AalUapp.ApplicationManagement value) {
		this.applicationManagement = value;
	}

	public boolean isSetApplicationManagement() {
		return (this.applicationManagement != null);
	}

	/**
	 * Gets the value of the applicationPart property.
	 *
	 * @return possible object is {@link AalUapp.ApplicationPart }
	 *
	 */
	public AalUapp.ApplicationPart getApplicationPart() {
		return applicationPart;
	}

	/**
	 * Sets the value of the applicationPart property.
	 *
	 * @param value
	 *            allowed object is {@link AalUapp.ApplicationPart }
	 *
	 */
	public void setApplicationPart(AalUapp.ApplicationPart value) {
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
	 *         &lt;element name="version" type="{http://www.universaal.org/aal-uapp/v1.0.2}versionType"/>
	 *         &lt;element name="appId" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;element name="multipart" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
	 *         &lt;element name="tags" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;element name="applicationProvider" type="{http://www.universaal.org/aal-uapp/v1.0.2}contactType"/>
	 *         &lt;element name="licenses" maxOccurs="unbounded" minOccurs="0">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="license" type="{http://www.universaal.org/aal-uapp/v1.0.2}licenseType" maxOccurs="unbounded" minOccurs="0"/>
	 *                   &lt;element name="sla" minOccurs="0">
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
	 *                 &lt;/sequence>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *         &lt;element name="applicationProfile" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;element name="applicationOntology" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
	 *         &lt;element name="menuEntry" minOccurs="0">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="menuName" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *                   &lt;element name="serviceUri" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
	 *                   &lt;element name="icon" minOccurs="0">
	 *                     &lt;complexType>
	 *                       &lt;complexContent>
	 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                           &lt;choice>
	 *                             &lt;element name="path" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
	 *                             &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *                           &lt;/choice>
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
	@XmlType(name = "", propOrder = { "name", "version", "appId", "description", "multipart", "tags",
			"applicationProvider", "licenses", "applicationProfile", "applicationOntology", "menuEntry" })
	public static class App implements Serializable {

		private final static long serialVersionUID = 12343L;
		@XmlElement(required = true)
		protected String name;
		@XmlElement(required = true)
		protected VersionType version;
		@XmlElement(required = true)
		protected String appId;
		@XmlElement(required = true)
		protected String description;
		protected boolean multipart;
		@XmlElement(required = true)
		protected String tags;
		@XmlElement(required = true)
		protected ContactType applicationProvider;
		protected List<AalUapp.App.Licenses> licenses;
		@XmlElement(required = true)
		protected String applicationProfile;
		protected String applicationOntology;
		protected AalUapp.App.MenuEntry menuEntry;

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
		 * Gets the value of the multipart property.
		 *
		 */
		public boolean isMultipart() {
			return multipart;
		}

		/**
		 * Sets the value of the multipart property.
		 *
		 */
		public void setMultipart(boolean value) {
			this.multipart = value;
		}

		public boolean isSetMultipart() {
			return true;
		}

		/**
		 * Gets the value of the tags property.
		 *
		 * @return possible object is {@link String }
		 *
		 */
		public String getTags() {
			return tags;
		}

		/**
		 * Sets the value of the tags property.
		 *
		 * @param value
		 *            allowed object is {@link String }
		 *
		 */
		public void setTags(String value) {
			this.tags = value;
		}

		public boolean isSetTags() {
			return (this.tags != null);
		}

		/**
		 * Gets the value of the applicationProvider property.
		 *
		 * @return possible object is {@link ContactType }
		 *
		 */
		public ContactType getApplicationProvider() {
			return applicationProvider;
		}

		/**
		 * Sets the value of the applicationProvider property.
		 *
		 * @param value
		 *            allowed object is {@link ContactType }
		 *
		 */
		public void setApplicationProvider(ContactType value) {
			this.applicationProvider = value;
		}

		public boolean isSetApplicationProvider() {
			return (this.applicationProvider != null);
		}

		/**
		 * Gets the value of the licenses property.
		 *
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the licenses property.
		 *
		 * <p>
		 * For example, to add a new item, do as follows:
		 *
		 * <pre>
		 * getLicenses().add(newItem);
		 * </pre>
		 *
		 *
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link AalUapp.App.Licenses }
		 *
		 *
		 */
		public List<AalUapp.App.Licenses> getLicenses() {
			if (licenses == null) {
				licenses = new ArrayList<AalUapp.App.Licenses>();
			}
			return this.licenses;
		}

		public boolean isSetLicenses() {
			return ((this.licenses != null) && (!this.licenses.isEmpty()));
		}

		public void unsetLicenses() {
			this.licenses = null;
		}

		/**
		 * Gets the value of the applicationProfile property.
		 *
		 * @return possible object is {@link String }
		 *
		 */
		public String getApplicationProfile() {
			return applicationProfile;
		}

		/**
		 * Sets the value of the applicationProfile property.
		 *
		 * @param value
		 *            allowed object is {@link String }
		 *
		 */
		public void setApplicationProfile(String value) {
			this.applicationProfile = value;
		}

		public boolean isSetApplicationProfile() {
			return (this.applicationProfile != null);
		}

		/**
		 * Gets the value of the applicationOntology property.
		 *
		 * @return possible object is {@link String }
		 *
		 */
		public String getApplicationOntology() {
			return applicationOntology;
		}

		/**
		 * Sets the value of the applicationOntology property.
		 *
		 * @param value
		 *            allowed object is {@link String }
		 *
		 */
		public void setApplicationOntology(String value) {
			this.applicationOntology = value;
		}

		public boolean isSetApplicationOntology() {
			return (this.applicationOntology != null);
		}

		/**
		 * Gets the value of the menuEntry property.
		 *
		 * @return possible object is {@link AalUapp.App.MenuEntry }
		 *
		 */
		public AalUapp.App.MenuEntry getMenuEntry() {
			return menuEntry;
		}

		/**
		 * Sets the value of the menuEntry property.
		 *
		 * @param value
		 *            allowed object is {@link AalUapp.App.MenuEntry }
		 *
		 */
		public void setMenuEntry(AalUapp.App.MenuEntry value) {
			this.menuEntry = value;
		}

		public boolean isSetMenuEntry() {
			return (this.menuEntry != null);
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
		 *         &lt;element name="license" type="{http://www.universaal.org/aal-uapp/v1.0.2}licenseType" maxOccurs="unbounded" minOccurs="0"/>
		 *         &lt;element name="sla" minOccurs="0">
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
		 *       &lt;/sequence>
		 *     &lt;/restriction>
		 *   &lt;/complexContent>
		 * &lt;/complexType>
		 * </pre>
		 *
		 *
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = { "license", "sla" })
		public static class Licenses implements Serializable {

			private final static long serialVersionUID = 12343L;
			protected List<LicenseType> license;
			protected AalUapp.App.Licenses.Sla sla;

			/**
			 * Gets the value of the license property.
			 *
			 * <p>
			 * This accessor method returns a reference to the live list, not a
			 * snapshot. Therefore any modification you make to the returned
			 * list will be present inside the JAXB object. This is why there is
			 * not a <CODE>set</CODE> method for the license property.
			 *
			 * <p>
			 * For example, to add a new item, do as follows:
			 *
			 * <pre>
			 * getLicense().add(newItem);
			 * </pre>
			 *
			 *
			 * <p>
			 * Objects of the following type(s) are allowed in the list
			 * {@link LicenseType }
			 *
			 *
			 */
			public List<LicenseType> getLicense() {
				if (license == null) {
					license = new ArrayList<LicenseType>();
				}
				return this.license;
			}

			public boolean isSetLicense() {
				return ((this.license != null) && (!this.license.isEmpty()));
			}

			public void unsetLicense() {
				this.license = null;
			}

			/**
			 * Gets the value of the sla property.
			 *
			 * @return possible object is {@link AalUapp.App.Licenses.Sla }
			 *
			 */
			public AalUapp.App.Licenses.Sla getSla() {
				return sla;
			}

			/**
			 * Sets the value of the sla property.
			 *
			 * @param value
			 *            allowed object is {@link AalUapp.App.Licenses.Sla }
			 *
			 */
			public void setSla(AalUapp.App.Licenses.Sla value) {
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
			public static class Sla implements Serializable {

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
		 * The following schema fragment specifies the expected content
		 * contained within this class.
		 *
		 * <pre>
		 * &lt;complexType>
		 *   &lt;complexContent>
		 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
		 *       &lt;sequence>
		 *         &lt;element name="menuName" type="{http://www.w3.org/2001/XMLSchema}string"/>
		 *         &lt;element name="serviceUri" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
		 *         &lt;element name="icon" minOccurs="0">
		 *           &lt;complexType>
		 *             &lt;complexContent>
		 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
		 *                 &lt;choice>
		 *                   &lt;element name="path" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
		 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
		 *                 &lt;/choice>
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
		@XmlType(name = "", propOrder = { "menuName", "serviceUri", "icon" })
		public static class MenuEntry implements Serializable {

			private final static long serialVersionUID = 12343L;
			@XmlElement(required = true)
			protected String menuName;
			@XmlElement(required = true)
			@XmlSchemaType(name = "anyURI")
			protected String serviceUri;
			protected AalUapp.App.MenuEntry.Icon icon;

			/**
			 * Gets the value of the menuName property.
			 *
			 * @return possible object is {@link String }
			 *
			 */
			public String getMenuName() {
				return menuName;
			}

			/**
			 * Sets the value of the menuName property.
			 *
			 * @param value
			 *            allowed object is {@link String }
			 *
			 */
			public void setMenuName(String value) {
				this.menuName = value;
			}

			public boolean isSetMenuName() {
				return (this.menuName != null);
			}

			/**
			 * Gets the value of the serviceUri property.
			 *
			 * @return possible object is {@link String }
			 *
			 */
			public String getServiceUri() {
				return serviceUri;
			}

			/**
			 * Sets the value of the serviceUri property.
			 *
			 * @param value
			 *            allowed object is {@link String }
			 *
			 */
			public void setServiceUri(String value) {
				this.serviceUri = value;
			}

			public boolean isSetServiceUri() {
				return (this.serviceUri != null);
			}

			/**
			 * Gets the value of the icon property.
			 *
			 * @return possible object is {@link AalUapp.App.MenuEntry.Icon }
			 *
			 */
			public AalUapp.App.MenuEntry.Icon getIcon() {
				return icon;
			}

			/**
			 * Sets the value of the icon property.
			 *
			 * @param value
			 *            allowed object is {@link AalUapp.App.MenuEntry.Icon }
			 *
			 */
			public void setIcon(AalUapp.App.MenuEntry.Icon value) {
				this.icon = value;
			}

			public boolean isSetIcon() {
				return (this.icon != null);
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
			 *       &lt;choice>
			 *         &lt;element name="path" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
			 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
			 *       &lt;/choice>
			 *     &lt;/restriction>
			 *   &lt;/complexContent>
			 * &lt;/complexType>
			 * </pre>
			 *
			 *
			 */
			@XmlAccessorType(XmlAccessType.FIELD)
			@XmlType(name = "", propOrder = { "path", "name" })
			public static class Icon implements Serializable {

				private final static long serialVersionUID = 12343L;
				@XmlSchemaType(name = "anyURI")
				protected String path;
				protected String name;

				/**
				 * Gets the value of the path property.
				 *
				 * @return possible object is {@link String }
				 *
				 */
				public String getPath() {
					return path;
				}

				/**
				 * Sets the value of the path property.
				 *
				 * @param value
				 *            allowed object is {@link String }
				 *
				 */
				public void setPath(String value) {
					this.path = value;
				}

				public boolean isSetPath() {
					return (this.path != null);
				}

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
	 *         &lt;element name="capability" type="{http://www.universaal.org/aal-uapp/v1.0.2}capabilityType" maxOccurs="unbounded"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "capability" })
	public static class ApplicationCapabilities implements Serializable {

		private final static long serialVersionUID = 12343L;
		@XmlElement(required = true)
		protected List<CapabilityType> capability;

		/**
		 * Gets the value of the capability property.
		 *
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the capability property.
		 *
		 * <p>
		 * For example, to add a new item, do as follows:
		 *
		 * <pre>
		 * getCapability().add(newItem);
		 * </pre>
		 *
		 *
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link CapabilityType }
		 *
		 *
		 */
		public List<CapabilityType> getCapability() {
			if (capability == null) {
				capability = new ArrayList<CapabilityType>();
			}
			return this.capability;
		}

		public boolean isSetCapability() {
			return ((this.capability != null) && (!this.capability.isEmpty()));
		}

		public void unsetCapability() {
			this.capability = null;
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
	 *         &lt;element name="remoteManagement" minOccurs="0">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="protocols" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
	 *                   &lt;element name="software" type="{http://www.universaal.org/aal-uapp/v1.0.2}artifactType"/>
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
		protected AalUapp.ApplicationManagement.RemoteManagement remoteManagement;

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
		 *         {@link AalUapp.ApplicationManagement.RemoteManagement }
		 *
		 */
		public AalUapp.ApplicationManagement.RemoteManagement getRemoteManagement() {
			return remoteManagement;
		}

		/**
		 * Sets the value of the remoteManagement property.
		 *
		 * @param value
		 *            allowed object is
		 *            {@link AalUapp.ApplicationManagement.RemoteManagement }
		 *
		 */
		public void setRemoteManagement(AalUapp.ApplicationManagement.RemoteManagement value) {
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
		 *         &lt;element name="software" type="{http://www.universaal.org/aal-uapp/v1.0.2}artifactType"/>
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
	 *         &lt;element ref="{http://www.universaal.org/aal-uapp/v1.0.2}part" maxOccurs="unbounded"/>
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
		 * Objects of the following type(s) are allowed in the list {@link Part
		 * }
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
	 *         &lt;element name="requirement" type="{http://www.universaal.org/aal-uapp/v1.0.2}reqType" maxOccurs="unbounded"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "requirement" })
	public static class ApplicationRequirements implements Serializable {

		private final static long serialVersionUID = 12343L;
		@XmlElement(required = true)
		protected List<ReqType> requirement;

		/**
		 * Gets the value of the requirement property.
		 *
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the requirement property.
		 *
		 * <p>
		 * For example, to add a new item, do as follows:
		 *
		 * <pre>
		 * getRequirement().add(newItem);
		 * </pre>
		 *
		 *
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link ReqType }
		 *
		 *
		 */
		public List<ReqType> getRequirement() {
			if (requirement == null) {
				requirement = new ArrayList<ReqType>();
			}
			return this.requirement;
		}

		public boolean isSetRequirement() {
			return ((this.requirement != null) && (!this.requirement.isEmpty()));
		}

		public void unsetRequirement() {
			this.requirement = null;
		}

	}

}
