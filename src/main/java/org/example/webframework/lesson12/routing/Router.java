package org.example.webframework.lesson12.routing;

import org.example.webframework.lesson12.HttpMethod;

import java.net.URL;

public interface Router {
    Router register(Route route);

    MatchedRoute match(HttpMethod method, URL url);
}
