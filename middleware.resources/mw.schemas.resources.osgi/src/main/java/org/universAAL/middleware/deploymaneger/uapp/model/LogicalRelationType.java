//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.20 at 06:22:00 PM CET 
//


package org.universAAL.middleware.deploymaneger.uapp.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for logicalRelationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="logicalRelationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="or"/>
 *     &lt;enumeration value="and"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "logicalRelationType")
@XmlEnum
public enum LogicalRelationType {

    @XmlEnumValue("or")
    OR("or"),
    @XmlEnumValue("and")
    AND("and"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    LogicalRelationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LogicalRelationType fromValue(String v) {
        for (LogicalRelationType c: LogicalRelationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
