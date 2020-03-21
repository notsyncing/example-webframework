package org.example.webframework.lesson10.mvc;

import org.example.webframework.lesson10.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MvcRoute {
    String value();
    HttpMethod[] methods() default { HttpMethod.GET };
    boolean simple() default true;
}
