package org.example.webframework.lesson4;

import org.example.webframework.lesson4.session.Session;

public class Context {
    private final Request request;
    private final Response response;
    private Session session;

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

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
