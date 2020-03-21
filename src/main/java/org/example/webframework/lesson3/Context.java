package org.example.webframework.lesson3;

public class Context {
    private final Request request;
    private final Response response;

    public Context(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }
}
