package org.example.webframework.lesson12.session;

public interface SessionStorageConfig {
    Class<? extends SessionStorage> getStorageClass();
}
