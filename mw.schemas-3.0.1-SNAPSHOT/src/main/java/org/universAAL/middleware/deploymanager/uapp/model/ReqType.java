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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * describes single offering, mostly used for devices and platforms
 *
 * <p>Java class for reqType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="reqType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="reqAtom" type="{http://www.universaal.org/aal-uapp/v1.0.2}reqAtomType"/>
 *           &lt;element name="reqGroup" type="{http://www.universaal.org/aal-uapp/v1.0.2}reqGroupType"/>
 *         &lt;/choice>
 *         &lt;element name="optional" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reqType", propOrder = {
    "reqAtom",
    "reqGroup",
    "optional"
})
public class ReqType
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    protected ReqAtomType reqAtom;
    protected ReqGroupType reqGroup;
    protected Boolean optional;

    /**
     * Gets the value of the reqAtom property.
     *
     * @return
     *     possible object is
     *     {@link ReqAtomType }
     *
     */
    public ReqAtomType getReqAtom() {
        return reqAtom;
    }

    /**
     * Sets the value of the reqAtom property.
     *
     * @param value
     *     allowed object is
     *     {@link ReqAtomType }
     *
     */
    public void setReqAtom(ReqAtomType value) {
        this.reqAtom = value;
    }

    public boolean isSetReqAtom() {
        return (this.reqAtom!= null);
    }

    /**
     * Gets the value of the reqGroup property.
     *
     * @return
     *     possible object is
     *     {@link ReqGroupType }
     *
     */
    public ReqGroupType getReqGroup() {
        return reqGroup;
    }

    /**
     * Sets the value of the reqGroup property.
     *
     * @param value
     *     allowed object is
     *     {@link ReqGroupType }
     *
     */
    public void setReqGroup(ReqGroupType value) {
        this.reqGroup = value;
    }

    public boolean isSetReqGroup() {
        return (this.reqGroup!= null);
    }

    /**
     * Gets the value of the optional property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isOptional() {
        return optional;
    }

    /**
     * Sets the value of the optional property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setOptional(Boolean value) {
        this.optional = value;
    }

    public boolean isSetOptional() {
        return (this.optional!= null);
    }

}
