package org.example.webframework.lesson12.ioc;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DependencyInfo implements Cloneable {
    private Class<?> type;
    private Annotation qualifier;
    private boolean provider;

    public DependencyInfo(Type type) {
        if (type instanceof Class<?>) {
            this.type = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            final var p = (ParameterizedType) type;

            if (p.getRawType().getTypeName().equals(Provider.class.getName())) {
                this.type = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
                provider = true;
            } else {
                this.type = (Class<?>) p.getRawType();
            }
        } else {
            throw new RuntimeException("Unsupported type " + type);
        }
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Annotation getQualifier() {
        return qualifier;
    }

    public void setQualifier(Annotation qualifier) {
        this.qualifier = qualifier;
    }

    public boolean isProvider() {
        return provider;
    }

    public void setProvider(boolean provider) {
        this.provider = provider;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public DependencyInfo stripProvider() throws CloneNotSupportedException {
        final var info = (DependencyInfo) this.clone();
        info.setProvider(false);
        return info;
    }
}
