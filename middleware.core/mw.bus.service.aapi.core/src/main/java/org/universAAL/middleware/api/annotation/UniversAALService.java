package org.universAAL.middleware.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Annotation which marks interface as UniversAALService. This means that all
 * methods will be scanned for @ServiceOperation occurences and on its base a
 * ServiceProfiles will be generated.
 * 
 * - namespace - it is a service provider namespace which act as a prefix for
 * other annotation values - especially @Inputs, @Ouputs, and @ChangeEffects; -
 * name - name of service provider. If it is not provided then a interface
 * simple name is used as default.
 * 
 * @author dzmuda
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UniversAALService {
    String namespace();

    String name() default "";
}
