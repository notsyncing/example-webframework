package org.example.webframework.lesson6.session;

import org.example.webframework.lesson6.Context;
import org.example.webframework.lesson6.Handler;
import org.example.webframework.lesson6.HandlerChain;

import java.util.UUID;

public class SessionHandler implements Handler {
    private MemorySessionStorage storage = new MemorySessionStorage();

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
