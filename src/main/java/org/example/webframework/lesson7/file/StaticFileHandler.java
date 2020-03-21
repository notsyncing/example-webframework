package org.example.webframework.lesson7.file;

import org.example.webframework.lesson7.Context;
import org.example.webframework.lesson7.Handler;
import org.example.webframework.lesson7.HandlerChain;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements Handler {
    private final Path rootPath;

    public StaticFileHandler(Path rootPath) {
        this.rootPath = rootPath.toAbsolutePath().normalize();
    }

    @Override
    public void handle(Context context, HandlerChain chain) {
        try {
            final var path = context.getRequest().getUrl().getPath().substring(1);
            var fullPath = rootPath.resolve(URLDecoder.decode(path, StandardCharsets.UTF_8)).normalize();

            if (!fullPath.startsWith(rootPath)) {
                context.getResponse().endWithStatusCode(403, "Not allowed!");
                return;
            }

            if (Files.isDirectory(fullPath)) {
                fullPath = fullPath.resolve("index.html");
            }

            new ServerFile(fullPath).send(context);
        } catch (Exception e) {
            e.printStackTrace();
            context.getResponse().endWithStatusCode(500, e.getMessage());
        }

        chain.nextHandler(context);
    }
}
