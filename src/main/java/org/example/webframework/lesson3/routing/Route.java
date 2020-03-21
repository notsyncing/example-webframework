package org.example.webframework.lesson3.routing;

import org.example.webframework.lesson3.HttpMethod;

import java.net.URL;

public abstract class Route {
    private HttpMethod method;
    private Object attachment;

    public Route(HttpMethod method) {
        this.method = method;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public MatchedRoute match(HttpMethod method, URL url) {
        if (method != this.method) {
            return null;
        }

        return matchUrl(url);
    }

    protected abstract MatchedRoute matchUrl(URL url);
}
