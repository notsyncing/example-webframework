package org.example.webframework.lesson10.mvc;

import org.example.webframework.lesson10.Request;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FormBodyDecoder implements BodyDecoder {
    @Override
    public void decode(Request request) throws Exception {
        final var data = request.readBody().get();
        final var encoded = new String(data.array(), StandardCharsets.UTF_8);
        final var params = encoded.split("&");

        for (final var param : params) {
            final var split = param.indexOf('=');
            final var key = URLDecoder.decode(param.substring(0, split), StandardCharsets.UTF_8);
            final var value = URLDecoder.decode(param.substring(split + 1), StandardCharsets.UTF_8);
            request.addParameter(key, value);
        }
    }

    @Override
    public void close() throws Exception {
    }
}
