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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * org.universAAL.middleware.connectors.deploy.karaf.core.model package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Features_QNAME = new QName(
	    "http://karaf.apache.org/xmlns/features/v1.0.0", "features");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * org.universAAL.middleware.connectors.deploy.karaf.core.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FeaturesRoot }
     * 
     */
    public FeaturesRoot createFeaturesRoot() {
	return new FeaturesRoot();
    }

    /**
     * Create an instance of {@link Dependency }
     * 
     */
    public Dependency createDependency() {
	return new Dependency();
    }

    /**
     * Create an instance of {@link ConfigFile }
     * 
     */
    public ConfigFile createConfigFile() {
	return new ConfigFile();
    }

    /**
     * Create an instance of {@link Config }
     * 
     */
    public Config createConfig() {
	return new Config();
    }

    /**
     * Create an instance of {@link Bundle }
     * 
     */
    public Bundle createBundle() {
	return new Bundle();
    }

    /**
     * Create an instance of {@link Feature }
     * 
     */
    public Feature createFeature() {
	return new Feature();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FeaturesRoot }
     * {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://karaf.apache.org/xmlns/features/v1.0.0", name = "features")
    public JAXBElement<FeaturesRoot> createFeatures(FeaturesRoot value) {
	return new JAXBElement<FeaturesRoot>(_Features_QNAME,
		FeaturesRoot.class, null, value);
    }

}
