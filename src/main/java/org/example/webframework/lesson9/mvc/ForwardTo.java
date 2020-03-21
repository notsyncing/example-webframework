package org.example.webframework.lesson9.mvc;

import org.example.webframework.lesson9.Context;

import java.net.URL;

public class ForwardTo implements MvcResult {
    private String path;

    public ForwardTo(String path) {
        this.path = path;
    }

    @Override
    public void process(Context context, MvcHandler handler) throws Exception {
        final var request = context.getRequest();

        if (!path.startsWith("/")) {
            var prefix = request.getUrl().getPath();
            final var index = prefix.lastIndexOf('/');

            if (index >= 0) {
                prefix = prefix.substring(0, index);
            }

            if (!prefix.endsWith("/")) {
                prefix += "/";
            }

            path = prefix + path;
        }

        final var oldUrl = request.getUrl();
        final var newUrl = new URL(oldUrl.getProtocol(), oldUrl.getHost(), oldUrl.getPort(), path);

        final var oldMatchedRoute = request.getRoute();
        final var newMatchedRoute = handler.getRouter().match(request.getMethod(), newUrl);
        request.setRoute(newMatchedRoute);

        if (newMatchedRoute == null) {
            request.setPathAndQuery(path);
            return;
        }

        newMatchedRoute.getQueryParameters().putAll(oldMatchedRoute.getQueryParameters());

        handler.action(newMatchedRoute, context);
    }
}
