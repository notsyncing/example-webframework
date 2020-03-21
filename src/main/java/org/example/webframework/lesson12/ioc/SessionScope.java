package org.example.webframework.lesson12.ioc;

import java.util.Map;

public class SessionScope implements ObjectScope {
    private static final ThreadLocal<Map<Class<?>, Object>> scopeObjects = new ThreadLocal<>();

    public static void setStorage(Map<Class<?>, Object> map) {
        scopeObjects.set(map);
    }

    public static void close(Map<Class<?>, Object> map) {
        for (final var obj : map.values()) {
            if (obj instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) obj).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Object get(Class<?> clazz) {
        return scopeObjects.get().get(clazz);
    }

    @Override
    public void put(Class<?> clazz, Object instance) {
        scopeObjects.get().put(clazz, instance);
    }
}
