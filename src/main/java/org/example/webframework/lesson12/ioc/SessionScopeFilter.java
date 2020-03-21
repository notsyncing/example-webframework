package org.example.webframework.lesson12.ioc;

import org.example.webframework.lesson12.Context;
import org.example.webframework.lesson12.filter.Filter;
import org.example.webframework.lesson12.filter.FilterChain;
import org.example.webframework.lesson12.session.Session;
import org.example.webframework.lesson12.session.SessionLifecycleHandlers;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
@Order(1)
public class SessionScopeFilter implements Filter {
    public static final String SCOPE_KEY = "wf.ioc.scopes.session";

    public SessionScopeFilter(SessionLifecycleHandlers handlers) {
        handlers.addDestroyHandler(SessionScopeFilter::onSessionDestroyed);
    }

    private static void onSessionDestroyed(Session session) {
        final var map = (Map<Class<?>, Object>) session.get(SCOPE_KEY);
        SessionScope.close(map);
    }

    @Override
    public void doFilter(Context context, FilterChain chain) throws Exception {
        final var session = context.getSession();

        if (!session.has(SCOPE_KEY)) {
            session.set(SCOPE_KEY, new HashMap<Class<?>, Object>());
        }

        SessionScope.setStorage((Map<Class<?>, Object>) context.getSession().get(SCOPE_KEY));

        chain.nextFilter(context);
    }
}
