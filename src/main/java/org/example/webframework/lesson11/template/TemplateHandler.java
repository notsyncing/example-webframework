package org.example.webframework.lesson11.template;

import org.example.webframework.lesson11.Context;
import org.example.webframework.lesson11.Handler;
import org.example.webframework.lesson11.HandlerChain;
import org.example.webframework.lesson11.HttpMethod;
import org.example.webframework.lesson11.routing.MatchedRoute;
import org.example.webframework.lesson11.routing.RegexRoute;
import org.example.webframework.lesson11.routing.Router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class TemplateHandler implements Handler {
    private Router router = new Router();
    private TemplateEngine templateEngine = new TemplateEngine();

    public TemplateHandler() throws IOException {
        templateEngine.registerTemplateDirectory(Paths.get("static"));

        router.register(new RegexRoute(HttpMethod.GET, "/hello"));
    }

    @Override
    public void handle(Context context, HandlerChain chain) {
        final MatchedRoute matchedRoute;

        try {
            matchedRoute = router.match(context.getRequest().getMethod(), context.getRequest().getUrl());
            context.getRequest().setRoute(matchedRoute);
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

        try {
            final var template = templateEngine.createFromTemplate("hello.template", context);
            template.render();
            final var content = template.getContentText();

            context.getResponse()
                    .setStatusCode(200)
                    .setContentType("text/html")
                    .setContentLength(content.length())
                    .write(ByteBuffer.wrap(content.getBytes()))
                    .end();
        } catch (Exception e) {
            e.printStackTrace();

            context.getResponse()
                    .setStatusCode(500)
                    .setContentLength(0)
                    .end();

            return;
        }

        chain.nextHandler(context);
    }
}
