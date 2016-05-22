package org.kpull.bastion.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvVar {

    String name() default "";

    String value() default "";

}
