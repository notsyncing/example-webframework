package org.example.webframework.lesson12.ioc;

public interface ObjectScope {
    Object get(Class<?> clazz);
    void put(Class<?> clazz, Object instance);
}
