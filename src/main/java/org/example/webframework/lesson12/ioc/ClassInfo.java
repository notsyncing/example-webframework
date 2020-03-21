package org.example.webframework.lesson12.ioc;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassInfo {
    private Class<?> type;
    private Annotation scope;
    private Annotation qualifier;

    private Constructor<?> constructor;
    private List<DependencyInfo> constructorDependencies = new ArrayList<>();
    private Map<Field, DependencyInfo> fieldDependencies = new HashMap<>();
    private Map<Method, List<DependencyInfo>> methodDependencies = new HashMap<>();

    public ClassInfo(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Annotation getScope() {
        return scope;
    }

    public void setScope(Annotation scope) {
        this.scope = scope;
    }

    public Annotation getQualifier() {
        return qualifier;
    }

    public void setQualifier(Annotation qualifier) {
        this.qualifier = qualifier;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public List<DependencyInfo> getConstructorDependencies() {
        return constructorDependencies;
    }

    public Map<Field, DependencyInfo> getFieldDependencies() {
        return fieldDependencies;
    }

    public Map<Method, List<DependencyInfo>> getMethodDependencies() {
        return methodDependencies;
    }

    public void addConstructorDependency(DependencyInfo dependency) {
        constructorDependencies.add(dependency);
    }

    public void addFieldDependency(Field field, DependencyInfo dependency) {
        fieldDependencies.put(field, dependency);
    }

    public void addMethodDependency(Method method, DependencyInfo dependency) {
        methodDependencies.compute(method, (m, l) -> {
            if (l == null) {
                l = new ArrayList<>();
            }

            l.add(dependency);
            return l;
        });
    }

    public boolean isSingleton() {
        return qualifier != null && qualifier.annotationType().equals(Singleton.class);
    }
}
