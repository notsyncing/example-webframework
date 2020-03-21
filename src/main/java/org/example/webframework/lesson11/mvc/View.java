package org.example.webframework.lesson11.mvc;

import org.example.webframework.lesson11.Context;

import java.nio.ByteBuffer;

public class View implements MvcResult {
    private final String path;
    private final Object model;

    public View(String path, Object model) {
        this.path = path;
        this.model = model;
    }

    public View(String path) {
        this(path, null);
    }

    @Override
    public void process(Context context, MvcHandler handler) throws Exception {
        context.setModel(model);

        final var template = handler.getTemplateEngine().createFromTemplate(path, context);
        template.render();
        final var content = template.getContentText();

        context.getResponse()
                .setStatusCode(200)
                .setContentType("text/html; charset=UTF-8")
                .setContentLength(content.length())
                .write(ByteBuffer.wrap(content.getBytes()))
                .end();
    }
}
