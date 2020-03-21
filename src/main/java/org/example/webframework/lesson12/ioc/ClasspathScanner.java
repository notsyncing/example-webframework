package org.example.webframework.lesson12.ioc;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Scope;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class ClasspathScanner {
    private ClassLoader classLoader = getClass().getClassLoader();
    private ClassInfoRegistry registry;

    public ClasspathScanner(ClassInfoRegistry registry) {
        this.registry = registry;
    }

    public void scanPackages(List<String> packages) {
        for (final var p : packages) {
            try {
                scanPackage(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void scanPackage(String packageName) throws IOException, URISyntaxException {
        final var path = getPackagePath(packageName);

        if (path == null) {
            throw new IOException("Package " + packageName + " not found!");
        }

        Files.walk(path)
                .filter(p -> p.getFileName().toString().endsWith(".class"))
                .map(p -> path.relativize(p.toAbsolutePath()))
                .forEach(p -> {
                    try {
                        processClassFile(packageName, p);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
    }

    private Path getPackagePath(String packageName) throws IOException, URISyntaxException {
        final var path = packageName.replace('.', '/');
        final var resource = classLoader.getResource(path);

        if (resource == null) {
            return null;
        }

        final var uri = resource.toURI();

        if ("jar".equals(uri.getScheme())) {
            final var fileSystem = FileSystems.newFileSystem(uri, System.getenv());
            return fileSystem.getPath(path);
        } else {
            return Paths.get(uri);
        }
    }

    private void processClassFile(String basePackageName, Path classFile) throws ClassNotFoundException {
        final var className = basePackageName + "." + classFile.toString()
                .replace('\\', '.')
                .replace('/', '.')
                .replace(".class", "");

        final var clazz = classLoader.loadClass(className);
        final var info = new ClassInfo(clazz);

        processClassAnnotations(info);

        if (info.getQualifier() == null) {
            return;
        }

        processClassConstructor(info);
        processClassFields(info, clazz);
        processClassMethods(info, clazz);

        registry.addClassInfo(info);
    }

    private void processClassAnnotations(ClassInfo info) {
        info.setScope(getScopeAnnotationOnClass(info.getType()));
        info.setQualifier(getQualifierAnnotationOnClass(info.getType()));
    }

    private Annotation getScopeAnnotationOnClass(Class<?> clazz) {
        for (final var annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Scope.class)) {
                return annotation;
            }
        }

        return null;
    }

    private Annotation getQualifierAnnotationOnClass(Class<?> clazz) {
        for (final var annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                return annotation;
            }
        }

        return null;
    }

    private void processClassConstructor(ClassInfo info) {
        final var constructors = info.getType().getConstructors();
        Constructor<?> candidateConstructor;

        if (constructors.length == 1) {
            candidateConstructor = constructors[0];
        } else {
            candidateConstructor = Stream.of(constructors)
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .findFirst()
                    .orElse(null);
        }

        if (candidateConstructor == null) {
            throw new RuntimeException("Cannot determine constructor for " + info.getType());
        }

        info.setConstructor(candidateConstructor);

        for (final var param : candidateConstructor.getParameters()) {
            final var dependency = new DependencyInfo(param.getParameterizedType());
            processDependencyAnnotations(dependency);
            info.addConstructorDependency(dependency);
        }
    }

    private void processDependencyAnnotations(DependencyInfo dependency) {
        dependency.setQualifier(getQualifierAnnotationOnClass(dependency.getType()));
    }

    private void processClassFields(ClassInfo info, Class<?> type) {
        final var superClass = type.getSuperclass();

        if (superClass != null && !superClass.equals(Object.class)) {
            processClassFields(info, type.getSuperclass());
        }

        for (final var field : type.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }

            final var dependency = new DependencyInfo(field.getGenericType());
            processDependencyAnnotations(dependency);
            info.addFieldDependency(field, dependency);
        }
    }

    private void processClassMethods(ClassInfo info, Class<?> type) {
        final var superClass = type.getSuperclass();

        if (superClass != null && !superClass.equals(Object.class)) {
            processClassMethods(info, type.getSuperclass());
        }

        for (final var method : info.getType().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Inject.class)) {
                continue;
            }

            for (final var param : method.getParameters()) {
                final var dependency = new DependencyInfo(param.getParameterizedType());
                processDependencyAnnotations(dependency);
                info.addMethodDependency(method, dependency);
            }
        }
    }
}
