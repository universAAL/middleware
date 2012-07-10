package org.universAAL.middleware.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for generating ServiceProfile.
 * For each of such annotation a separate createInput call is made on SimpleAPIService.
 * @author dzmuda
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Input {
	String name() default "";
	Class filteringClass() default void.class;
	String[] propertyPaths() default {};
}
