package org.example.webframework.lesson11.cors;

import org.example.webframework.lesson11.*;
import org.example.webframework.lesson11.routing.Route;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CorsHandler implements Handler {
    private static Map<Route, CorsConfig> configs = new HashMap<>();

    public static void addConfig(Route route, CorsConfig config) {
        configs.put(route, config);
    }

    @Override
    public void handle(Context context, HandlerChain chain) {
        if (!context.getRequest().hasHeader("Origin")) {
            chain.nextHandler(context);
            return;
        }

        try {
            for (final var pair : configs.entrySet()) {
                final var matched = pair.getKey().match(null, context.getRequest().getUrl());
                final var config = pair.getValue();

                if (matched == null) {
                    continue;
                }

                if (context.getRequest().getMethod() == HttpMethod.OPTIONS) {
                    var statusCode = 200;

                    if (!checkPreflightCorsConfig(config, context.getRequest())) {
                        statusCode = 403;
                    } else {
                        attachCorsHeaders(config, context);
                    }

                    context.getResponse().endWithStatusCode(statusCode);
                    return;
                } else {
                    if (!checkSimpleCorsConfig(config, context.getRequest())) {
                        context.getResponse().endWithStatusCode(403);
                        return;
                    } else {
                        attachCorsHeaders(config, context);
                    }
                }

                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.getResponse().endWithStatusCode(500);
            return;
        }

        chain.nextHandler(context);
    }

    private boolean checkSimpleCorsConfig(CorsConfig config, Request request) {
        if (!config.getAllowedOrigins().contains("*")) {
            final var origin = request.getHeaderValue("Origin");

            if (!config.getAllowedOrigins().contains(origin)) {
                return false;
            }
        }

        if (!config.getAllowedHttpMethods().contains(request.getMethod())) {
            return false;
        }

        return true;
    }

    private boolean checkPreflightCorsConfig(CorsConfig config, Request request) {
        if (!config.getAllowedOrigins().contains("*")) {
            final var origin = request.getHeaderValue("Origin");

            if (!config.getAllowedOrigins().contains(origin)) {
                return false;
            }
        }

        final var method = HttpMethod.valueOf(request.getHeaderValue("Access-Control-Request-Method"));

        if (!config.getAllowedHttpMethods().contains(method)) {
            return false;
        }

        final var requestHeaderSet = Stream.of(request.getHeaderValue("Access-Control-Request-Headers")
                .split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        for (final var header : requestHeaderSet) {
            if (!config.getAllowedHeaders().contains(header)) {
                return false;
            }
        }

        return true;
    }

    private void attachCorsHeaders(CorsConfig config, Context context) {
        final var response = context.getResponse();

        final String origin;

        if (config.getAllowedOrigins().contains("*")) {
            origin = "*";
        } else {
            origin = context.getRequest().getHeaderValue("Origin");
            response.addHeader("Vary", "Origin");
        }

        response.addHeader("Access-Control-Allow-Origin", origin);

        if (context.getRequest().getMethod() != HttpMethod.OPTIONS) {
            if (!config.getExposedHeaders().isEmpty()) {
                response.addHeader("Access-Control-Expose-Headers",
                        String.join(", ", config.getExposedHeaders()));
            }
        }

        if (config.isAllowCredentials()) {
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }

        if (context.getRequest().getMethod() == HttpMethod.OPTIONS) {
            response.addHeader("Access-Control-Allow-Methods",
                    config.getAllowedHttpMethods().stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));

            if (!config.getAllowedHeaders().isEmpty()) {
                response.addHeader("Access-Control-Allow-Headers",
                        String.join(", ", config.getAllowedHeaders()));
            }

            if (config.getMaxAge() > 0) {
                response.addHeader("Access-Control-Max-Age", String.valueOf(config.getMaxAge()));
            }
        }
    }
}
