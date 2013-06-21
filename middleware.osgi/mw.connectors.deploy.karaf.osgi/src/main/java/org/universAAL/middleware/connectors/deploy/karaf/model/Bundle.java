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
package org.universAAL.middleware.connectors.deploy.karaf.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * 
 * Deployable element to install.
 * 
 * 
 * <p>
 * Java class for bundle complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="bundle">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
 *       &lt;attribute name="start-level" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="start" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="dependency" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "bundle", propOrder = { "value" })
public class Bundle {

    @XmlValue
    @XmlSchemaType(name = "anyURI")
    protected String value;
    @XmlAttribute(name = "start-level")
    protected Integer startLevel;
    @XmlAttribute(name = "start")
    protected Boolean start;
    @XmlAttribute(name = "dependency")
    protected Boolean dependency;

    /**
     * Gets the value of the value property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getValue() {
	return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setValue(String value) {
	this.value = value;
    }

    /**
     * Gets the value of the startLevel property.
     * 
     * @return possible object is {@link Integer }
     * 
     */
    public Integer getStartLevel() {
	return startLevel;
    }

    /**
     * Sets the value of the startLevel property.
     * 
     * @param value
     *            allowed object is {@link Integer }
     * 
     */
    public void setStartLevel(Integer value) {
	this.startLevel = value;
    }

    /**
     * Gets the value of the start property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public Boolean isStart() {
	return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *            allowed object is {@link Boolean }
     * 
     */
    public void setStart(Boolean value) {
	this.start = value;
    }

    /**
     * Gets the value of the dependency property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public Boolean isDependency() {
	return dependency;
    }

    /**
     * Sets the value of the dependency property.
     * 
     * @param value
     *            allowed object is {@link Boolean }
     * 
     */
    public void setDependency(Boolean value) {
	this.dependency = value;
    }

}
