package org.universAAL.middleware.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.universAAL.middleware.owl.OntologyManagement;

/**
 * Annotation which is used for Ontology registering.
 * Classes provided as a value is scanned for occurence of MY_URI field
 * and then registered with the use of 
 * 
 * OntologyManagement.getInstance().register(baseURI, MY_URI)
 * 
 * where baseURI is namespace + name from @UniversAALService annotation
 * 
 * @author dzmuda
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OntologyClasses {
	Class[] value();
}
