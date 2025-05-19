package gg.sunken.sdk.config;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.*;

@BindingAnnotation
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {
    String value();
    String file() default "config.yml";
}
