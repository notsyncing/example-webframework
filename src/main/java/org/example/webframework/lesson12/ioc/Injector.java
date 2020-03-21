package org.example.webframework.lesson12.ioc;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Injector {
    private ClassInfoRegistry registry;
    private Map<Class<?>, List<ClassInfo>> classInfoTypeMap = new ConcurrentHashMap<>();
    private Map<Class<? extends Annotation>, ObjectScope> scopes = new HashMap<>();

    public Injector() {
        registerScope(Singleton.class, new StaticScope());
        registerScope(RequestScoped.class, new RequestScope());
        registerScope(SessionScoped.class, new SessionScope());
    }

    public void registerScope(Class<? extends Annotation> scopeAnnotation, ObjectScope scope) {
        scopes.put(scopeAnnotation, scope);
    }

    public void registerSingleton(Object object) {
        scopes.get(Singleton.class).put(object.getClass(), object);
    }

    public void init(ClassInfoRegistry registry) {
        this.registry = registry;

        initSingletons();
    }

    private void initSingletons() {
        final var scope = scopes.get(Singleton.class);

        for (final var info : registry.getClassInfoList()) {
            if (!info.isSingleton()) {
                continue;
            }

            if (scope.get(info.getType()) != null) {
                continue;
            }

            try {
                create(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Object get(Class<?> type) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final var condition = new DependencyInfo(type);
        return get(condition);
    }

    private Object get(DependencyInfo condition) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (condition.isProvider()) {
            return (Provider<Object>) () -> {
                try {
                    return Injector.this.get(condition.stripProvider());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            };
        }

        final var info = resolveClassInfo(condition);

        if (info == null) {
            return null;
        }

        final var scopeAnnotation = info.getScope();
        ObjectScope scope = null;
        Object object = null;

        if (scopeAnnotation != null) {
            scope = scopes.get(scopeAnnotation.annotationType());

            if (scope == null) {
                throw new RuntimeException("Scope " + scopeAnnotation + " on type " + condition.getType() +
                        " does not exist!");
            }

            object = scope.get(info.getType());
        }

        if (object != null) {
            return object;
        }

        object = create(info);

        if (scope != null) {
            scope.put(info.getType(), object);
        }

        return object;
    }

    private ClassInfo resolveClassInfo(DependencyInfo condition) {
        final var matchedClassInfoList = classInfoTypeMap.computeIfAbsent(condition.getType(),
                t -> registry.getClassInfoList().stream()
                        .filter(c -> condition.getType().isAssignableFrom(c.getType()))
                        .collect(Collectors.toList()));

        if (matchedClassInfoList.isEmpty()) {
            return null;
        }

        if (condition.getQualifier() == null) {
            if (matchedClassInfoList.size() == 1) {
                return matchedClassInfoList.get(0);
            } else {
                final var matchedTypeString = matchedClassInfoList.stream()
                        .map(ClassInfo::getType)
                        .map(Class::toString)
                        .collect(Collectors.joining(", "));

                throw new RuntimeException("There are " + matchedClassInfoList.size() + " matches to type " +
                        condition.getType() + ": " + matchedTypeString);
            }
        } else {
            return matchedClassInfoList.stream()
                    .filter(c -> condition.getQualifier().equals(c.getQualifier()))
                    .findFirst()
                    .orElse(null);
        }
    }

    private Object create(ClassInfo info) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final var params = new ArrayList<>();

        for (final var dep : info.getConstructorDependencies()) {
            final var depObj = get(dep);
            params.add(depObj);
        }

        final var object = info.getConstructor().newInstance(params.toArray());

        if (info.getScope() != null) {
            final var scope = scopes.get(info.getScope().annotationType());

            if (scope == null) {
                throw new RuntimeException("Scope " + info.getScope() + " on type " + info.getType() +
                        " does not exist!");
            }

            scope.put(info.getType(), object);
        }

        injectFields(object, info);
        injectMethods(object, info);

        return object;
    }

    private void injectFields(Object object, ClassInfo info) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (final var fieldInfo : info.getFieldDependencies().entrySet()) {
            final var field = fieldInfo.getKey();
            final var depObj = get(fieldInfo.getValue());
            var restoreAccess = false;

            if (!field.canAccess(object)) {
                field.setAccessible(true);
                restoreAccess = true;
            }

            field.set(object, depObj);

            if (restoreAccess) {
                field.setAccessible(false);
            }
        }
    }

    private void injectMethods(Object object, ClassInfo info) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        for (final var methodInfo : info.getMethodDependencies().entrySet()) {
            final var method = methodInfo.getKey();
            final var params = new ArrayList<>();

            for (final var dep : methodInfo.getValue()) {
                final var depObj = get(dep);
                params.add(depObj);
            }

            var restoreAccess = false;

            if (!method.canAccess(object)) {
                method.setAccessible(true);
                restoreAccess = true;
            }

            method.invoke(object, params.toArray());

            if (restoreAccess) {
                method.setAccessible(false);
            }
        }
    }
}
