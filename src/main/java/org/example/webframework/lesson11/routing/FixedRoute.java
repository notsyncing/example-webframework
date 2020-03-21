package org.example.webframework.lesson11.routing;

import org.example.webframework.lesson11.HttpMethod;

import java.net.URL;

public class FixedRoute extends Route {
    private String pattern;

    public FixedRoute(HttpMethod method, String pattern) {
        super(method);
        this.pattern = pattern;
    }

    @Override
    public MatchedRoute matchUrl(URL url) {
        if (!pattern.equals(url.getPath())) {
            return null;
        }

        return new MatchedRoute(url, this);
    }
}
