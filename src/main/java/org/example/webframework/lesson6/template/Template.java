package org.example.webframework.lesson6.template;

import org.example.webframework.lesson6.Context;

public abstract class Template {
    private final TemplateEngine engine;
    private final Context context;
    private final StringBuilder content = new StringBuilder();

    public Template(TemplateEngine engine, Context context) {
        this.engine = engine;
        this.context = context;
    }

    protected String getRequestParameter(String parameter) {
        return context.getRequest().getRoute().getFirstQueryParameterValue(parameter);
    }

    protected void include(String path) throws Exception {
        final var template = engine.createFromTemplate(path, context);
        template.render();
        write(template.getContentText());
    }

    public abstract void render() throws Exception;

    protected void write(String text) {
        content.append(text);
    }

    public String getContentText() {
        return content.toString();
    }
}
