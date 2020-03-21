package org.example.webframework.lesson11.mvc.multipart;

import java.util.HashMap;
import java.util.Map;

public class ContentType {
    private String type;
    private Map<String, String> parameters = new HashMap<>();

    public ContentType(String contentTypeValue) {
        final var values = contentTypeValue.split(";");

        for (final var param : values) {
            final var trimmedParam = param.trim();
            final var split = trimmedParam.indexOf('=');

            if (split < 0) {
                type = trimmedParam;
            } else {
                final var paramName = trimmedParam.substring(0, split).trim();
                final var paramValue = trimmedParam.substring(split + 1).trim();
                parameters.put(paramName, paramValue);
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public boolean hasParameter(String name) {
        return parameters.containsKey(name);
    }
}
