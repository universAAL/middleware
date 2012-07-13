package org.universAAL.middleware.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for generating ServiceProfile.
 * For each of such annotation a separate addOutput call is made on SimpleAPIService.
 * 
 * @author dzmuda
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Output {
	String name() default "";
	String[] propertyPaths() default {};
	Class filteringClass() default void.class;
	Cardinality cardinality() default Cardinality.NOT_SPECIFIED;
}
