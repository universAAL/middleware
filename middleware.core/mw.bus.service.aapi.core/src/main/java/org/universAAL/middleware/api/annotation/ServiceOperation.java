package org.universAAL.middleware.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Annotation which marks method as UniversAAL service. For each of those methods a 
 * separate ServiceProfile is generated. If no value is specified a method name is taken by
 * default.
 * @author dzmuda
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceOperation {
	String value() default "";
	MatchMakingType type() default MatchMakingType.NOT_SPECIFIED;
	
	enum MatchMakingType{
	    ONTOLOGICAL,
	    BY_URI,
	    NOT_SPECIFIED
	}
}


