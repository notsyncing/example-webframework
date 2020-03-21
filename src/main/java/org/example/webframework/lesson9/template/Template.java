package org.example.webframework.lesson9.template;

import org.example.webframework.lesson9.Context;

import java.lang.reflect.InvocationTargetException;

public abstract class Template {
    private final TemplateEngine engine;
    private final Context context;
    private final StringBuilder content = new StringBuilder();

    public Template(TemplateEngine engine, Context context) {
        this.engine = engine;
        this.context = context;
    }

    protected Object getRequestParameter(String parameter) {
        return context.getRequest().getRoute().getQueryParameter(parameter);
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

    protected Object model(String key) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final var model = context.getModel();

        if (model == null) {
            return null;
        }

        final var getterName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
        final var getter = model.getClass().getMethod(getterName);
        return getter.invoke(model);
    }
}
