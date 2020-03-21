package org.example.webframework.lesson8.routing;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchedRoute {
    private URL url;
    private Route route;
    private Map<String, String> matchedParameters = new HashMap<>();
    private Map<String, Object> queryParameters;

    public MatchedRoute(URL url, Route route) {
        this.url = url;
        this.route = route;
    }

    public URL getUrl() {
        return url;
    }

    public Route getRoute() {
        return route;
    }

    public Map<String, String> getMatchedParameters() {
        return matchedParameters;
    }

    public void addMatchedParameter(String key, String value) {
        matchedParameters.put(key, value);
    }

    public Map<String, Object> getQueryParameters() {
        if (queryParameters == null) {
            parseQueryParameters();
        }

        return queryParameters;
    }

    private void parseQueryParameters() {
        queryParameters = new HashMap<>();
        final var queryString = url.getQuery();

        if (queryString == null || queryString.isEmpty()) {
            return;
        }

        final var queryParams = queryString.split("&");

        for (final var queryParam : queryParams) {
            final var split = queryParam.indexOf('=');
            final var key = URLDecoder.decode(queryParam.substring(0, split), StandardCharsets.UTF_8);
            final var value = URLDecoder.decode(queryParam.substring(split + 1), StandardCharsets.UTF_8);

            if (!queryParameters.containsKey(key)) {
                queryParameters.put(key, value);
            } else {
                var v = queryParameters.get(key);

                if (!(v instanceof List)) {
                    final var l = new ArrayList<String>();
                    l.add((String) v);
                    queryParameters.put(key, l);
                    v = l;
                }

                ((List<String>) v).add(value);
            }
        }
    }

    public Object getQueryParameter(String key) {
        if (queryParameters == null) {
            parseQueryParameters();
        }

        return queryParameters.get(key);
    }
}
