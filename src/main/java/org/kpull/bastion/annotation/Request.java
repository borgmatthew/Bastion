package org.kpull.bastion.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Request {

    String name() default "";

    String description() default "";

    String method();

    String type();

    String url();

    Header[] headers() default { };

    QueryParam[] queryParams() default { };

}
