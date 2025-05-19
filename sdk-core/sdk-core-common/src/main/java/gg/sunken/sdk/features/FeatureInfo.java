package gg.sunken.sdk.features;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a module
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FeatureInfo {
    String value();
    int priority() default 0;
    String[] dependencies() default {};
    String[] softDependencies() default {};
}
