package org.example.webframework.lesson3;

import org.example.webframework.lesson3.routing.*;

import java.nio.ByteBuffer;

public class HelloWorldHandler implements Handler {
    private Router router = new Router();

    public HelloWorldHandler() {
        router.register(new RegexRoute(HttpMethod.GET, "^/hello/(?<name>.*)$"))
                .register(new SimpleRoute(HttpMethod.GET, "/bye/{name}"))
                .register(new FixedRoute(HttpMethod.GET, "/world"));
    }

    @Override
    public void handle(Context context, HandlerChain chain) {
        final MatchedRoute matchedRoute;

        try {
            matchedRoute = router.match(context.getRequest().getMethod(), context.getRequest().getUrl());
        } catch (Exception e) {
            e.printStackTrace();

            context.getResponse()
                    .setStatusCode(500)
                    .setContentLength(0)
                    .end();

            return;
        }

        if (matchedRoute == null) {
            chain.nextHandler(context);
            return;
        }

        final var name = matchedRoute.getMatchedParameters().get("name");
        final var keys = matchedRoute.getQueryParameterValues("key");
        final var text = "Hello, " + name + "! (keys=" + String.join(",", keys) + ")";

        context.getResponse()
                .setStatusCode(200)
                .setContentType("text/plain")
                .setContentLength(text.length())
                //.useChunkedEncoding()
                .write(ByteBuffer.wrap(text.getBytes()))
                .end();

        chain.nextHandler(context);
    }
}
