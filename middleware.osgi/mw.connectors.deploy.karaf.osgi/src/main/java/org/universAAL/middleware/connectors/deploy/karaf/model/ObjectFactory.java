//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.07.10 at 02:38:44 PM CEST 
//

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
