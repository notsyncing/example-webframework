package org.example.webframework.lesson12.cors;

import org.example.webframework.lesson12.HttpMethod;

import java.util.*;

public class CorsConfig {
    private Set<String> allowedOrigins = new HashSet<>();
    private Set<HttpMethod> allowedHttpMethods = new HashSet<>();
    private Map<String, String> allowedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Set<String> exposedHeaders = new HashSet<>();
    private boolean allowCredentials;
    private long maxAge;

    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public Set<HttpMethod> getAllowedHttpMethods() {
        return allowedHttpMethods;
    }

    public Set<String> getAllowedHeaders() {
        return allowedHeaders.keySet();
    }

    public Set<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public CorsConfig allowOrigins(String... origins) {
        allowedOrigins.addAll(Arrays.asList(origins));
        return this;
    }

    public CorsConfig allowMethods(HttpMethod... methods) {
        allowedHttpMethods.addAll(Arrays.asList(methods));
        return this;
    }

    public CorsConfig allowHeader(String... headers) {
        for (final var h : headers) {
            allowedHeaders.put(h, null);
        }

        return this;
    }

    public CorsConfig exposeHeader(String... headers) {
        exposedHeaders.addAll(Arrays.asList(headers));
        return this;
    }

    public CorsConfig allowCredentials(boolean allow) {
        allowCredentials = allow;
        return this;
    }

    public CorsConfig maxAge(long age) {
        maxAge = age;
        return this;
    }
}
