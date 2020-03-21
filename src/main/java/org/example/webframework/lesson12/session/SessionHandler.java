package org.example.webframework.lesson12.session;

import org.example.webframework.lesson12.Context;
import org.example.webframework.lesson12.Handler;
import org.example.webframework.lesson12.HandlerChain;
import org.example.webframework.lesson12.ioc.Container;

import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

@Named
public class SessionHandler implements Handler {
    private final SessionStorage storage;

    public SessionHandler(Container container, SessionStorageConfig storage)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        this.storage = (SessionStorage) container.get(storage.getStorageClass());
    }

    @Override
    public void handle(Context context, HandlerChain chain) {
        var sessionId = context.getRequest().getCookieValue("SESSION_ID");
        var hasSession = false;

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        } else {
            hasSession = true;
        }

        final var session = storage.get(sessionId);
        context.setSession(session);

        if (!hasSession) {
            context.getResponse().addCookie("SESSION_ID", sessionId);
        }

        chain.nextHandler(context);
    }
}
