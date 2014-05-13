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
 * <p>Java class for embeddingType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="embeddingType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="singleContainer"/>
 *     &lt;enumeration value="anyContainer"/>
 *     &lt;enumeration value="namedContainer"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
@XmlType(name = "embeddingType")
@XmlEnum
public enum EmbeddingType {

    @XmlEnumValue("singleContainer")
    SINGLE_CONTAINER("singleContainer"),
    @XmlEnumValue("anyContainer")
    ANY_CONTAINER("anyContainer"),
    @XmlEnumValue("namedContainer")
    NAMED_CONTAINER("namedContainer");
    private final String value;

    EmbeddingType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmbeddingType fromValue(String v) {
        for (EmbeddingType c: EmbeddingType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
