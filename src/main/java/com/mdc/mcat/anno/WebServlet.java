package com.mdc.mcat.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebServlet {
    String name();

    String[] value() default {};

    WebInitParam[] initParams() default {};

    int loadOnStartup() default -1;
}
