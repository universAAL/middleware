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
import ae.javax.xml.bind.annotation.XmlAccessType;
import ae.javax.xml.bind.annotation.XmlAccessorType;
import ae.javax.xml.bind.annotation.XmlAttribute;
import ae.javax.xml.bind.annotation.XmlElement;
import ae.javax.xml.bind.annotation.XmlID;
import ae.javax.xml.bind.annotation.XmlRootElement;
import ae.javax.xml.bind.annotation.XmlSchemaType;
import ae.javax.xml.bind.annotation.XmlType;
import ae.javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import ae.javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
 *         &lt;element name="osUnit" type="{http://universaal.org/aal-mpa/v1.0.0}osType"/>
 *         &lt;element name="platformUnit" type="{http://universaal.org/aal-mpa/v1.0.0}platformType"/>
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
 *                             &lt;sequence>
 *                               &lt;element name="embedding" type="{http://universaal.org/aal-mpa/v1.0.0}EmbeddingType"/>
 *                               &lt;element name="embeddingName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;/sequence>
 *                             &lt;element ref="{http://karaf.apache.org/xmlns/features/v1.0.0}features"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="tomcat" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                   &lt;element name="equinox" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                   &lt;element name="felix" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
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
     *                   &lt;sequence>
     *                     &lt;element name="embedding" type="{http://universaal.org/aal-mpa/v1.0.0}EmbeddingType"/>
     *                     &lt;element name="embeddingName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;/sequence>
     *                   &lt;element ref="{http://karaf.apache.org/xmlns/features/v1.0.0}features"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="tomcat" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
     *         &lt;element name="equinox" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
     *         &lt;element name="felix" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "karaf", "tomcat", "equinox", "felix" })
    public static class ContainerUnit implements Serializable {

	private final static long serialVersionUID = 12343L;
	protected DeploymentUnit.ContainerUnit.Karaf karaf;
	protected Object tomcat;
	protected Object equinox;
	protected Object felix;

	/**
	 * Gets the value of the karaf property.
	 * 
	 * @return possible object is {@link DeploymentUnit.ContainerUnit.Karaf }
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
	 *         &lt;sequence>
	 *           &lt;element name="embedding" type="{http://universaal.org/aal-mpa/v1.0.0}EmbeddingType"/>
	 *           &lt;element name="embeddingName" type="{http://www.w3.org/2001/XMLSchema}string"/>
	 *         &lt;/sequence>
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
	@XmlType(name = "", propOrder = { "embedding", "embeddingName",
		"features" })
	public static class Karaf implements Serializable {

	    private final static long serialVersionUID = 12343L;
	    @XmlElement(required = true)
	    protected EmbeddingType embedding;
	    @XmlElement(required = true)
	    protected String embeddingName;
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
	     * Gets the value of the embeddingName property.
	     * 
	     * @return possible object is {@link String }
	     * 
	     */
	    public String getEmbeddingName() {
		return embeddingName;
	    }

	    /**
	     * Sets the value of the embeddingName property.
	     * 
	     * @param value
	     *            allowed object is {@link String }
	     * 
	     */
	    public void setEmbeddingName(String value) {
		this.embeddingName = value;
	    }

	    public boolean isSetEmbeddingName() {
		return (this.embeddingName != null);
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
