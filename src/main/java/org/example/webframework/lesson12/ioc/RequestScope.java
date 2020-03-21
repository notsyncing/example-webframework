package org.example.webframework.lesson12.ioc;

import java.util.HashMap;
import java.util.Map;

public class RequestScope implements ObjectScope {
    private static final ThreadLocal<Map<Class<?>, Object>> scopeObjects = new ThreadLocal<>();

    public static void reset() {
        scopeObjects.remove();
        scopeObjects.set(new HashMap<>());
    }

    public static void close() {
        final var map = scopeObjects.get();

        for (final var obj : map.values()) {
            if (obj instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) obj).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        reset();
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
