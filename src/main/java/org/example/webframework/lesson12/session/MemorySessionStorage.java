package org.example.webframework.lesson12.session;

import javax.inject.Named;
import java.util.concurrent.*;

@Named
public class MemorySessionStorage implements SessionStorage {
    private static final long SESSION_EXPIRE_TIME = 600 * 1000;

    private final ConcurrentMap<String, MemorySession> storage = new ConcurrentHashMap<>();

    private ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        final var t = new Thread(r);
        t.setName("memorysession-cleanup-thread");
        t.setDaemon(true);
        return t;
    });

    private SessionLifecycleHandlers handlers;

    public MemorySessionStorage(SessionLifecycleHandlers handlers) {
        this.handlers = handlers;
        cleaner.scheduleWithFixedDelay(this::removeExpired, 0, 100, TimeUnit.SECONDS);
    }

    @Override
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

        final var iter = storage.entrySet().iterator();

        while (iter.hasNext()) {
            final var e = iter.next();
            final var session = e.getValue();

            if (now - e.getValue().getLastAccessTime() > SESSION_EXPIRE_TIME) {
                iter.remove();
                handlers.runDestroyHandlers(session);
            }
        }
    }
}
