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

package org.universAAL.middleware.deploymanager.uapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
 *       &lt;choice>
 *         &lt;element name="osUnit" type="{http://www.universaal.org/aal-uapp/v1.0.2}osType"/>
 *         &lt;element name="platformUnit" type="{http://www.universaal.org/aal-uapp/v1.0.2}platformType"/>
 *         &lt;element name="containerUnit">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="karaf">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="embedding" type="{http://www.universaal.org/aal-uapp/v1.0.2}embeddingType"/>
 *                             &lt;element ref="{http://karaf.apache.org/xmlns/features/v1.0.0}features"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="android">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="location" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="tomcat" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                   &lt;element name="equinox" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                   &lt;element name="felix" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                   &lt;element name="osgi-android" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/choice>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "osUnit", "platformUnit", "containerUnit" })
@XmlRootElement(name = "deploymentUnit")
public class DeploymentUnit implements Serializable {

	private final static long serialVersionUID = 12343L;
	protected OsType osUnit;
	protected PlatformType platformUnit;
	protected DeploymentUnit.ContainerUnit containerUnit;
	@XmlAttribute(name = "id")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String id;

	/**
	 * Gets the value of the osUnit property.
	 *
	 * @return possible object is {@link OsType }
	 *
	 */
	public OsType getOsUnit() {
		return osUnit;
	}

	/**
	 * Sets the value of the osUnit property.
	 *
	 * @param value
	 *            allowed object is {@link OsType }
	 *
	 */
	public void setOsUnit(OsType value) {
		this.osUnit = value;
	}

	public boolean isSetOsUnit() {
		return (this.osUnit != null);
	}

	/**
	 * Gets the value of the platformUnit property.
	 *
	 * @return possible object is {@link PlatformType }
	 *
	 */
	public PlatformType getPlatformUnit() {
		return platformUnit;
	}

	/**
	 * Sets the value of the platformUnit property.
	 *
	 * @param value
	 *            allowed object is {@link PlatformType }
	 *
	 */
	public void setPlatformUnit(PlatformType value) {
		this.platformUnit = value;
	}

	public boolean isSetPlatformUnit() {
		return (this.platformUnit != null);
	}

	/**
	 * Gets the value of the containerUnit property.
	 *
	 * @return possible object is {@link DeploymentUnit.ContainerUnit }
	 *
	 */
	public DeploymentUnit.ContainerUnit getContainerUnit() {
		return containerUnit;
	}

	/**
	 * Sets the value of the containerUnit property.
	 *
	 * @param value
	 *            allowed object is {@link DeploymentUnit.ContainerUnit }
	 *
	 */
	public void setContainerUnit(DeploymentUnit.ContainerUnit value) {
		this.containerUnit = value;
	}

	public boolean isSetContainerUnit() {
		return (this.containerUnit != null);
	}

	/**
	 * Gets the value of the id property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setId(String value) {
		this.id = value;
	}

	public boolean isSetId() {
		return (this.id != null);
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
	 *       &lt;choice>
	 *         &lt;element name="karaf">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="embedding" type="{http://www.universaal.org/aal-uapp/v1.0.2}embeddingType"/>
	 *                   &lt;element ref="{http://karaf.apache.org/xmlns/features/v1.0.0}features"/>
	 *                 &lt;/sequence>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *         &lt;element name="android">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;sequence>
	 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *                   &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
	 *                   &lt;element name="location" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded"/>
	 *                 &lt;/sequence>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *         &lt;element name="tomcat" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
	 *         &lt;element name="equinox" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
	 *         &lt;element name="felix" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
	 *         &lt;element name="osgi-android" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
	 *       &lt;/choice>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "karaf", "android", "tomcat", "equinox", "felix", "osgiAndroid" })
	public static class ContainerUnit implements Serializable {

		private final static long serialVersionUID = 12343L;
		protected DeploymentUnit.ContainerUnit.Karaf karaf;
		protected DeploymentUnit.ContainerUnit.Android android;
		protected Object tomcat;
		protected Object equinox;
		protected Object felix;
		@XmlElement(name = "osgi-android")
		protected Object osgiAndroid;

		/**
		 * Gets the value of the karaf property.
		 *
		 * @return possible object is
		 *         {@link DeploymentUnit.ContainerUnit.Karaf }
		 *
		 */
		public DeploymentUnit.ContainerUnit.Karaf getKaraf() {
			return karaf;
		}

		/**
		 * Sets the value of the karaf property.
		 *
		 * @param value
		 *            allowed object is
		 *            {@link DeploymentUnit.ContainerUnit.Karaf }
		 *
		 */
		public void setKaraf(DeploymentUnit.ContainerUnit.Karaf value) {
			this.karaf = value;
		}

		public boolean isSetKaraf() {
			return (this.karaf != null);
		}

		/**
		 * Gets the value of the android property.
		 *
		 * @return possible object is
		 *         {@link DeploymentUnit.ContainerUnit.Android }
		 *
		 */
		public DeploymentUnit.ContainerUnit.Android getAndroid() {
			return android;
		}

		/**
		 * Sets the value of the android property.
		 *
		 * @param value
		 *            allowed object is
		 *            {@link DeploymentUnit.ContainerUnit.Android }
		 *
		 */
		public void setAndroid(DeploymentUnit.ContainerUnit.Android value) {
			this.android = value;
		}

		public boolean isSetAndroid() {
			return (this.android != null);
		}

		/**
		 * Gets the value of the tomcat property.
		 *
		 * @return possible object is {@link Object }
		 *
		 */
		public Object getTomcat() {
			return tomcat;
		}

		/**
		 * Sets the value of the tomcat property.
		 *
		 * @param value
		 *            allowed object is {@link Object }
		 *
		 */
		public void setTomcat(Object value) {
			this.tomcat = value;
		}

		public boolean isSetTomcat() {
			return (this.tomcat != null);
		}

		/**
		 * Gets the value of the equinox property.
		 *
		 * @return possible object is {@link Object }
		 *
		 */
		public Object getEquinox() {
			return equinox;
		}

		/**
		 * Sets the value of the equinox property.
		 *
		 * @param value
		 *            allowed object is {@link Object }
		 *
		 */
		public void setEquinox(Object value) {
			this.equinox = value;
		}

		public boolean isSetEquinox() {
			return (this.equinox != null);
		}

		/**
		 * Gets the value of the felix property.
		 *
		 * @return possible object is {@link Object }
		 *
		 */
		public Object getFelix() {
			return felix;
		}

		/**
		 * Sets the value of the felix property.
		 *
		 * @param value
		 *            allowed object is {@link Object }
		 *
		 */
		public void setFelix(Object value) {
			this.felix = value;
		}

		public boolean isSetFelix() {
			return (this.felix != null);
		}

		/**
		 * Gets the value of the osgiAndroid property.
		 *
		 * @return possible object is {@link Object }
		 *
		 */
		public Object getOsgiAndroid() {
			return osgiAndroid;
		}

		/**
		 * Sets the value of the osgiAndroid property.
		 *
		 * @param value
		 *            allowed object is {@link Object }
		 *
		 */
		public void setOsgiAndroid(Object value) {
			this.osgiAndroid = value;
		}

		public boolean isSetOsgiAndroid() {
			return (this.osgiAndroid != null);
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
		 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
		 *         &lt;element name="location" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded"/>
		 *       &lt;/sequence>
		 *     &lt;/restriction>
		 *   &lt;/complexContent>
		 * &lt;/complexType>
		 * </pre>
		 *
		 *
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = { "name", "description", "location" })
		public static class Android implements Serializable {

			private final static long serialVersionUID = 12343L;
			@XmlElement(required = true)
			protected String name;
			protected String description;
			@XmlElement(required = true)
			@XmlSchemaType(name = "anyURI")
			protected List<String> location;

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
			 * Gets the value of the location property.
			 *
			 * <p>
			 * This accessor method returns a reference to the live list, not a
			 * snapshot. Therefore any modification you make to the returned
			 * list will be present inside the JAXB object. This is why there is
			 * not a <CODE>set</CODE> method for the location property.
			 *
			 * <p>
			 * For example, to add a new item, do as follows:
			 * 
			 * <pre>
			 * getLocation().add(newItem);
			 * </pre>
			 *
			 *
			 * <p>
			 * Objects of the following type(s) are allowed in the list
			 * {@link String }
			 *
			 *
			 */
			public List<String> getLocation() {
				if (location == null) {
					location = new ArrayList<String>();
				}
				return this.location;
			}

			public boolean isSetLocation() {
				return ((this.location != null) && (!this.location.isEmpty()));
			}

			public void unsetLocation() {
				this.location = null;
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
		 *         &lt;element name="embedding" type="{http://www.universaal.org/aal-uapp/v1.0.2}embeddingType"/>
		 *         &lt;element ref="{http://karaf.apache.org/xmlns/features/v1.0.0}features"/>
		 *       &lt;/sequence>
		 *     &lt;/restriction>
		 *   &lt;/complexContent>
		 * &lt;/complexType>
		 * </pre>
		 *
		 *
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = { "embedding", "features" })
		public static class Karaf implements Serializable {

			private final static long serialVersionUID = 12343L;
			@XmlElement(required = true)
			protected EmbeddingType embedding;
			@XmlElement(namespace = "http://karaf.apache.org/xmlns/features/v1.0.0", required = true)
			protected FeaturesRoot features;

			/**
			 * Gets the value of the embedding property.
			 *
			 * @return possible object is {@link EmbeddingType }
			 *
			 */
			public EmbeddingType getEmbedding() {
				return embedding;
			}

			/**
			 * Sets the value of the embedding property.
			 *
			 * @param value
			 *            allowed object is {@link EmbeddingType }
			 *
			 */
			public void setEmbedding(EmbeddingType value) {
				this.embedding = value;
			}

			public boolean isSetEmbedding() {
				return (this.embedding != null);
			}

			/**
			 * Gets the value of the features property.
			 *
			 * @return possible object is {@link FeaturesRoot }
			 *
			 */
			public FeaturesRoot getFeatures() {
				return features;
			}

			/**
			 * Sets the value of the features property.
			 *
			 * @param value
			 *            allowed object is {@link FeaturesRoot }
			 *
			 */
			public void setFeatures(FeaturesRoot value) {
				this.features = value;
			}

			public boolean isSetFeatures() {
				return (this.features != null);
			}

		}

	}

}
