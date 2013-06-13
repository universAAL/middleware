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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for logicalCriteriaType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="logicalCriteriaType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="not"/>
 *     &lt;enumeration value="equal"/>
 *     &lt;enumeration value="greater"/>
 *     &lt;enumeration value="greater-equal"/>
 *     &lt;enumeration value="less"/>
 *     &lt;enumeration value="less-equal"/>
 *     &lt;enumeration value="contain"/>
 *     &lt;enumeration value="doesn-not-contain"/>
 *     &lt;enumeration value="begin"/>
 *     &lt;enumeration value="end"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "logicalCriteriaType")
@XmlEnum
public enum LogicalCriteriaType {

    @XmlEnumValue("not")
    NOT("not"),
    @XmlEnumValue("equal")
    EQUAL("equal"),
    @XmlEnumValue("greater")
    GREATER("greater"),
    @XmlEnumValue("greater-equal")
    GREATER_EQUAL("greater-equal"),
    @XmlEnumValue("less")
    LESS("less"),
    @XmlEnumValue("less-equal")
    LESS_EQUAL("less-equal"),
    @XmlEnumValue("contain")
    CONTAIN("contain"),
    @XmlEnumValue("doesn-not-contain")
    DOESN_NOT_CONTAIN("doesn-not-contain"),
    @XmlEnumValue("begin")
    BEGIN("begin"),
    @XmlEnumValue("end")
    END("end");
    private final String value;

    LogicalCriteriaType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LogicalCriteriaType fromValue(String v) {
        for (LogicalCriteriaType c: LogicalCriteriaType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
