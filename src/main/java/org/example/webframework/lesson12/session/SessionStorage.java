package org.example.webframework.lesson12.session;

public interface SessionStorage {
    Session get(String sessionId);
}
