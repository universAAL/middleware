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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * describes multiple requirements with a given logical relation
 *
 * <p>Java class for reqGroupType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="reqGroupType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="logicalRelation" type="{http://www.universaal.org/aal-uapp/v1.0.2}logicalRelationType"/>
 *         &lt;element name="requirement" type="{http://www.universaal.org/aal-uapp/v1.0.2}reqType" maxOccurs="unbounded" minOccurs="2"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reqGroupType", propOrder = {
    "logicalRelation",
    "requirement"
})
public class ReqGroupType
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(required = true)
    protected LogicalRelationType logicalRelation;
    @XmlElement(required = true)
    protected List<ReqType> requirement;

    /**
     * Gets the value of the logicalRelation property.
     *
     * @return
     *     possible object is
     *     {@link LogicalRelationType }
     *
     */
    public LogicalRelationType getLogicalRelation() {
        return logicalRelation;
    }

    /**
     * Sets the value of the logicalRelation property.
     *
     * @param value
     *     allowed object is
     *     {@link LogicalRelationType }
     *
     */
    public void setLogicalRelation(LogicalRelationType value) {
        this.logicalRelation = value;
    }

    public boolean isSetLogicalRelation() {
        return (this.logicalRelation!= null);
    }

    /**
     * Gets the value of the requirement property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requirement property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequirement().add(newItem);
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
        return ((this.requirement!= null)&&(!this.requirement.isEmpty()));
    }

    public void unsetRequirement() {
        this.requirement = null;
    }

}
