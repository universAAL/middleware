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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Root element of Feature definition. It contains optional attribute which
 * allow name of repository. This name will be used in shell to display source
 * repository of given feature.
 * 
 * 
 * <p>
 * Java class for featuresRoot complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="featuresRoot">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="repository" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="feature" type="{http://karaf.apache.org/xmlns/features/v1.0.0}feature"/>
 *       &lt;/choice>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "featuresRoot", namespace = "http://karaf.apache.org/xmlns/features/v1.0.0", propOrder = { "repositoryOrFeature" })
public class FeaturesRoot implements Serializable {

    private final static long serialVersionUID = 12343L;
    @XmlElements({ @XmlElement(name = "repository", type = String.class),
	    @XmlElement(name = "feature", type = Feature.class) })
    protected List<Serializable> repositoryOrFeature;
    @XmlAttribute(name = "name")
    protected String name;

    /**
     * Gets the value of the repositoryOrFeature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the repositoryOrFeature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getRepositoryOrFeature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * {@link Feature }
     * 
     * 
     */
    public List<Serializable> getRepositoryOrFeature() {
	if (repositoryOrFeature == null) {
	    repositoryOrFeature = new ArrayList<Serializable>();
	}
	return this.repositoryOrFeature;
    }

    public boolean isSetRepositoryOrFeature() {
	return ((this.repositoryOrFeature != null) && (!this.repositoryOrFeature
		.isEmpty()));
    }

    public void unsetRepositoryOrFeature() {
	this.repositoryOrFeature = null;
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
