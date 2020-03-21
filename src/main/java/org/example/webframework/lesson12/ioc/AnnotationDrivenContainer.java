package org.example.webframework.lesson12.ioc;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named
@Singleton
public class AnnotationDrivenContainer implements Container {
    public static void run(Class<?> configClass) {
        new AnnotationDrivenContainer().start(configClass);
    }

    private ClassInfoRegistry registry = new ClassInfoRegistry();
    private ClasspathScanner scanner = new ClasspathScanner(registry);
    private Injector injector = new Injector();
    private List<LifecycleAware> lifecycleObjects = new ArrayList<>();

    @Override
    public void start(Class<?> configClass) {
        final var scanPackages = new ArrayList<String>();
        final var scanPackagesAnnotation = configClass.getAnnotation(ScanPackages.class);

        if (scanPackagesAnnotation != null && scanPackagesAnnotation.packages().length > 0) {
            scanPackages.addAll(Arrays.asList(scanPackagesAnnotation.packages()));
        } else {
            scanPackages.add(configClass.getPackageName());
        }

        scanner.scanPackages(scanPackages);

        injector.registerSingleton(this);
        injector.init(registry);

        initLifecycleObjects();

        for (final var o : lifecycleObjects) {
            o.started();
        }
    }

    private void initLifecycleObjects() {
        final var lifecycleTypes = registry.getListOfType(LifecycleAware.class);

        for (final var type : lifecycleTypes) {
            try {
                final var o = (LifecycleAware) injector.get(type);
                lifecycleObjects.add(o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object get(Class<?> type) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return injector.get(type);
    }

    @Override
    public ClassInfoRegistry getRegistry() {
        return registry;
    }
}
