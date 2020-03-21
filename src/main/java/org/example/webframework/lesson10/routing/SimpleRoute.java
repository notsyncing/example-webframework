package org.example.webframework.lesson10.routing;

import org.example.webframework.lesson10.HttpMethod;

public class SimpleRoute extends RegexRoute {
    public SimpleRoute(HttpMethod method, String pattern) {
        super(method, makeRegex(pattern));
    }

    private static String makeRegex(String simplePattern) {
        return "^" + simplePattern.replaceAll("\\{([a-zA-Z0-9_-]+)}", "(?<$1>.*)") + "$";
    }
}
