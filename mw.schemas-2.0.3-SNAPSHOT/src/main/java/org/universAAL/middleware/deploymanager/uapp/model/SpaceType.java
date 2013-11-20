//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2013.08.05 at 03:28:04 PM CEST
//


package org.universAAL.middleware.deploymanager.uapp.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for spaceType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="spaceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="HomeSpace"/>
 *     &lt;enumeration value="HospitalSpace"/>
 *     &lt;enumeration value="MarketSpace"/>
 *     &lt;enumeration value="CarSpace"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
@XmlType(name = "spaceType")
@XmlEnum
public enum SpaceType {

    @XmlEnumValue("HomeSpace")
    HOME_SPACE("HomeSpace"),
    @XmlEnumValue("HospitalSpace")
    HOSPITAL_SPACE("HospitalSpace"),
    @XmlEnumValue("MarketSpace")
    MARKET_SPACE("MarketSpace"),
    @XmlEnumValue("CarSpace")
    CAR_SPACE("CarSpace");
    private final String value;

    SpaceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SpaceType fromValue(String v) {
        for (SpaceType c: SpaceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
