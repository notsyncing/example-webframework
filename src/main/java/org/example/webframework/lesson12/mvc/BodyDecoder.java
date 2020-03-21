package org.example.webframework.lesson12.mvc;

import org.example.webframework.lesson12.Request;
import org.example.webframework.lesson12.mvc.multipart.MultipartBodyDecoder;

public interface BodyDecoder extends AutoCloseable {
    static BodyDecoder factory(String contentType) {
        switch (contentType) {
            case "application/x-www-form-urlencoded":
                return new FormBodyDecoder();
            case "multipart/form-data":
                return new MultipartBodyDecoder();
            default:
                throw new UnsupportedOperationException("Unsupported body content type " + contentType);
        }
    }

    void decode(Request request) throws Exception;
}
