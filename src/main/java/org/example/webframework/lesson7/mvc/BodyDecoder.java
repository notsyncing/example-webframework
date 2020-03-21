package org.example.webframework.lesson7.mvc;

import org.example.webframework.lesson7.Request;

public interface BodyDecoder {
    static BodyDecoder factory(String contentType) {
        switch (contentType) {
            case "application/x-www-form-urlencoded":
                return new FormBodyDecoder();
            default:
                throw new UnsupportedOperationException("Unsupported body content type " + contentType);
        }
    }

    void decode(Request request) throws Exception;
}
