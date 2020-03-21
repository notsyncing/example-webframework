package org.example.webframework.lesson9.routing;

import org.example.webframework.lesson9.HttpMethod;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Router {
    private List<Route> routes = new ArrayList<>();

    public Router register(Route route) {
        routes.add(route);
        return this;
    }

    public MatchedRoute match(HttpMethod method, URL url) {
        for (final var route : routes) {
            final var mr = route.match(method, url);

            if (mr != null) {
                return mr;
            }
        }

        return null;
    }
}
