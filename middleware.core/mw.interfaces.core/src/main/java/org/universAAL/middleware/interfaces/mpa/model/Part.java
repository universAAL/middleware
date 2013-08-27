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
 *       &lt;sequence>
 *         &lt;element ref="{http://universaal.org/aal-mpa/v1.0.0}deploymentUnit" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://universaal.org/aal-mpa/v1.0.0}executionUnit" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="partId" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "deploymentUnit", "executionUnit" })
@XmlRootElement(name = "part")
public class Part implements Serializable {

    private final static long serialVersionUID = 12343L;
    @XmlElement(required = true)
    protected List<DeploymentUnit> deploymentUnit;
    @XmlElement(required = true)
    protected List<ExecutionUnit> executionUnit;
    @XmlAttribute(name = "partId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String partId;

    /**
     * Gets the value of the deploymentUnit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the deploymentUnit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDeploymentUnit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DeploymentUnit }
     * 
     * 
     */
    public List<DeploymentUnit> getDeploymentUnit() {
	if (deploymentUnit == null) {
	    deploymentUnit = new ArrayList<DeploymentUnit>();
	}
	return this.deploymentUnit;
    }

    public boolean isSetDeploymentUnit() {
	return ((this.deploymentUnit != null) && (!this.deploymentUnit
		.isEmpty()));
    }

    public void unsetDeploymentUnit() {
	this.deploymentUnit = null;
    }

    /**
     * Gets the value of the executionUnit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the executionUnit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getExecutionUnit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExecutionUnit }
     * 
     * 
     */
    public List<ExecutionUnit> getExecutionUnit() {
	if (executionUnit == null) {
	    executionUnit = new ArrayList<ExecutionUnit>();
	}
	return this.executionUnit;
    }

    public boolean isSetExecutionUnit() {
	return ((this.executionUnit != null) && (!this.executionUnit.isEmpty()));
    }

    public void unsetExecutionUnit() {
	this.executionUnit = null;
    }

    /**
     * Gets the value of the partId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPartId() {
	return partId;
    }

    /**
     * Sets the value of the partId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPartId(String value) {
	this.partId = value;
    }

    public boolean isSetPartId() {
	return (this.partId != null);
    }

}
