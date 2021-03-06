package org.example.webframework.lesson9;

public enum HttpMethod {
    GET,
    POST(true),
    DELETE,
    PUT(true),
    HEAD,
    OPTIONS
    ;

    public final boolean hasBody;

    HttpMethod(boolean hasBody) {
        this.hasBody = hasBody;
    }

    HttpMethod() {
        this(false);
    }
}
