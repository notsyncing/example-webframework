package org.example.webframework.lesson4.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemorySession implements Session {
    private final Map<String, Object> map = new ConcurrentHashMap<>();
    private long lastAccessTime;

    public MemorySession() {
        updateLastAccessTime();
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void updateLastAccessTime() {
        lastAccessTime = System.currentTimeMillis();
    }

    @Override
    public Object get(String key) {
        updateLastAccessTime();
        return map.get(key);
    }

    @Override
    public void set(String key, Object value) {
        updateLastAccessTime();
        map.put(key, value);
    }

    @Override
    public boolean has(String key) {
        updateLastAccessTime();
        return map.containsKey(key);
    }

    @Override
    public void remove(String key) {
        updateLastAccessTime();
        map.remove(key);
    }
}
