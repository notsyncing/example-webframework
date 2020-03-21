package org.example.webframework.lesson6.routing;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MatchedRoute {
    private URL url;
    private Route route;
    private Map<String, String> matchedParameters = new HashMap<>();
    private Map<String, List<String>> queryParameters;

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

    public Map<String, List<String>> getQueryParameters() {
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
                queryParameters.put(key, new ArrayList<>());
            }

            queryParameters.get(key).add(value);
        }
    }

    public List<String> getQueryParameterValues(String key) {
        if (queryParameters == null) {
            parseQueryParameters();
        }

        final var list = queryParameters.get(key);

        if (list == null) {
            return Collections.emptyList();
        }

        return list;
    }

    public String getFirstQueryParameterValue(String key) {
        final var list = getQueryParameterValues(key);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
}
