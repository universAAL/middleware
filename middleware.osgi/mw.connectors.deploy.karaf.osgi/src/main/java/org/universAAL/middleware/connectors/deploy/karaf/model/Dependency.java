//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.07.10 at 02:38:44 PM CEST 
//

package org.universAAL.middleware.connectors.deploy.karaf.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * 
 * Dependency of feature.
 * 
 * 
 * <p>
 * Java class for dependency complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="dependency">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://karaf.apache.org/xmlns/features/v1.0.0>featureName">
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" default="0.0.0" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dependency", propOrder = { "value" })
public class Dependency {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "version")
    protected String version;

    /**
     * 
     * Feature name should be non empty string.
     * 
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
     * Gets the value of the version property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getVersion() {
	if (version == null) {
	    return "0.0.0";
	} else {
	    return version;
	}
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setVersion(String value) {
	this.version = value;
    }

}
