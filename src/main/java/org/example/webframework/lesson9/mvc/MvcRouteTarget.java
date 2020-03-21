package org.example.webframework.lesson9.mvc;

import java.lang.reflect.Method;

public class MvcRouteTarget {
    private Class<?> controllerClass;
    private Method action;

    public MvcRouteTarget(Class<?> controllerClass, Method action) {
        this.controllerClass = controllerClass;
        this.action = action;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getAction() {
        return action;
    }
}
