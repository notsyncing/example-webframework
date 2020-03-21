package org.example.webframework.lesson12.ioc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StaticScope implements ObjectScope {
    private Map<Class<?>, Object> objects = new ConcurrentHashMap<>();

    @Override
    public Object get(Class<?> clazz) {
        return objects.get(clazz);
    }

    @Override
    public void put(Class<?> clazz, Object instance) {
        objects.put(clazz, instance);
    }
}
