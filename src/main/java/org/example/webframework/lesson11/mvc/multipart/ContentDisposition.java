package org.example.webframework.lesson11.mvc.multipart;

import java.util.HashMap;
import java.util.Map;

public class ContentDisposition {
    private String contentDisposition;
    private Map<String, String> parameters = new HashMap<>();

    public ContentDisposition(String contentDispositionValue) {
        final var values = contentDispositionValue.split(";");

        for (final var param : values) {
            final var trimmedParam = param.trim();
            final var split = trimmedParam.indexOf('=');

            if (split < 0) {
                contentDisposition = trimmedParam;
            } else {
                final var paramName = trimmedParam.substring(0, split).trim();
                final var paramValue = trimmedParam.substring(split + 1).trim();
                parameters.put(paramName, paramValue.substring(1, paramValue.length() - 1));
            }
        }
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public boolean hasParameter(String name) {
        return parameters.containsKey(name);
    }
}
