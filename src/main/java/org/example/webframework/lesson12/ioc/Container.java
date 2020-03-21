package org.example.webframework.lesson12.ioc;

import java.lang.reflect.InvocationTargetException;

public interface Container {
    void start(Class<?> configClass);

    Object get(Class<?> type) throws IllegalAccessException, InvocationTargetException, InstantiationException;

    ClassInfoRegistry getRegistry();
}
