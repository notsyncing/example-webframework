package org.example.webframework.lesson11.mvc;

import org.example.webframework.lesson11.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cors {
    String[] origins() default { "*" };
    HttpMethod[] methods() default { HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS };
    String[] headers() default {};
    String[] exposeHeaders() default {};
    long maxAge() default 0;
    boolean credentials() default false;
}
