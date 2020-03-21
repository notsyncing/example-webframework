package org.example.webframework.lesson8;

import org.example.webframework.lesson8.session.Session;

public class Context {
    private final Request request;
    private final Response response;
    private Session session;
    private Object model;

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

    public Object getModel() {
        return model;
    }

    public void setModel(Object model) {
        this.model = model;
    }
}
