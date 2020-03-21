package org.example.webframework.lesson12.routing;

import org.example.webframework.lesson12.HttpMethod;

import javax.inject.Named;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Named
public class SimpleRouter implements Router {
    private List<Route> routes = new ArrayList<>();

    @Override
    public Router register(Route route) {
        routes.add(route);
        return this;
    }

    @Override
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
