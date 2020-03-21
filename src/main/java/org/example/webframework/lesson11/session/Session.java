package org.example.webframework.lesson11.session;

public interface Session {
    Object get(String key);

    default Object get(String key, Object defaultValue) {
        final var v = get(key);
        return v == null ? defaultValue : v;
    }

    void set(String key, Object value);
    boolean has(String key);
    void remove(String key);
}
