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
import ae.javax.xml.bind.annotation.XmlElement;
import ae.javax.xml.bind.annotation.XmlIDREF;
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
 *         &lt;element name="deploymentUnit" type="{http://www.w3.org/2001/XMLSchema}IDREF"/>
 *         &lt;element name="configFiles" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="spaceStartLevel" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "deploymentUnit", "configFiles",
	"spaceStartLevel" })
@XmlRootElement(name = "executionUnit")
public class ExecutionUnit implements Serializable {

    private final static long serialVersionUID = 12343L;
    @XmlElement(required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object deploymentUnit;
    @XmlElement(required = true)
    protected Object configFiles;
    @XmlElement(required = true)
    protected Object spaceStartLevel;

    /**
     * Gets the value of the deploymentUnit property.
     * 
     * @return possible object is {@link Object }
     * 
     */
    public Object getDeploymentUnit() {
	return deploymentUnit;
    }

    /**
     * Sets the value of the deploymentUnit property.
     * 
     * @param value
     *            allowed object is {@link Object }
     * 
     */
    public void setDeploymentUnit(Object value) {
	this.deploymentUnit = value;
    }

    public boolean isSetDeploymentUnit() {
	return (this.deploymentUnit != null);
    }

    /**
     * Gets the value of the configFiles property.
     * 
     * @return possible object is {@link Object }
     * 
     */
    public Object getConfigFiles() {
	return configFiles;
    }

    /**
     * Sets the value of the configFiles property.
     * 
     * @param value
     *            allowed object is {@link Object }
     * 
     */
    public void setConfigFiles(Object value) {
	this.configFiles = value;
    }

    public boolean isSetConfigFiles() {
	return (this.configFiles != null);
    }

    /**
     * Gets the value of the spaceStartLevel property.
     * 
     * @return possible object is {@link Object }
     * 
     */
    public Object getSpaceStartLevel() {
	return spaceStartLevel;
    }

    /**
     * Sets the value of the spaceStartLevel property.
     * 
     * @param value
     *            allowed object is {@link Object }
     * 
     */
    public void setSpaceStartLevel(Object value) {
	this.spaceStartLevel = value;
    }

    public boolean isSetSpaceStartLevel() {
	return (this.spaceStartLevel != null);
    }

}
