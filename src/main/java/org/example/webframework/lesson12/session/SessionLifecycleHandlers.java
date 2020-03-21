package org.example.webframework.lesson12.session;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Named
@Singleton
public class SessionLifecycleHandlers {
    private final Set<Consumer<Session>> destroyHandlers = new HashSet<>();

    public void addDestroyHandler(Consumer<Session> handler) {
        destroyHandlers.add(handler);
    }

    public void runDestroyHandlers(Session session) {
        for (final var handler : destroyHandlers) {
            try {
                handler.accept(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
