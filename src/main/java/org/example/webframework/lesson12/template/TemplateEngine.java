package org.example.webframework.lesson12.template;

import org.example.webframework.lesson12.Context;

import java.lang.reflect.InvocationTargetException;

public interface TemplateEngine {
    Template createFromTemplate(String path, Context context) throws IllegalAccessException,
            InvocationTargetException, InstantiationException;
}
