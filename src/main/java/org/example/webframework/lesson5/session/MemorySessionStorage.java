package org.example.webframework.lesson5.session;

import java.util.concurrent.*;

public class MemorySessionStorage {
    private static final long SESSION_EXPIRE_TIME = 600 * 1000;

    private ConcurrentMap<String, MemorySession> storage = new ConcurrentHashMap<>();

    private ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        final var t = new Thread(r);
        t.setName("memorysession-cleanup-thread");
        t.setDaemon(true);
        return t;
    });

    public MemorySessionStorage() {
        cleaner.scheduleWithFixedDelay(this::removeExpired, 0, 100, TimeUnit.SECONDS);
    }

    public MemorySession get(String sessionId) {
        return storage.compute(sessionId, (k, m) -> {
            if (m == null) {
                return new MemorySession();
            } else if (System.currentTimeMillis() - m.getLastAccessTime() > SESSION_EXPIRE_TIME) {
                return new MemorySession();
            } else {
                m.updateLastAccessTime();
                return m;
            }
        });
    }

    private void removeExpired() {
        final var now = System.currentTimeMillis();
        storage.entrySet().removeIf(e -> now - e.getValue().getLastAccessTime() > SESSION_EXPIRE_TIME);
    }
}
