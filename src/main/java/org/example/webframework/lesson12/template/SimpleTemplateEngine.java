package org.example.webframework.lesson12.template;

import org.example.webframework.lesson12.Context;

import javax.inject.Named;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

@Named
public class SimpleTemplateEngine implements TemplateEngine {
    private static class TemplateInfo {
        public Class<Template> templateClass;
        public Path fullPath;

        public TemplateInfo(Class<Template> templateClass, Path fullPath) {
            this.templateClass = templateClass;
            this.fullPath = fullPath;
        }
    }

    private enum State {
        HtmlContent,
        ScriptContent,
        ExpressionContent
    }

    private final Set<Path> templateDirectories = new HashSet<>();
    private final ConcurrentHashMap<String, TemplateInfo> templates = new ConcurrentHashMap<>();
    private final WatchService watcher = FileSystems.getDefault().newWatchService();
    private final Executor watcherThread = Executors.newSingleThreadExecutor(r -> {
        final var t = new Thread(r);
        t.setName("template-watcher-thread");
        t.setDaemon(true);
        return t;
    });

    public SimpleTemplateEngine(TemplateEngineConfig config) throws IOException {
        templateDirectories.addAll(config.getTemplateDirectories());
        watcherThread.execute(this::watchTemplates);
    }

    public void registerTemplateDirectory(Path path) {
        templateDirectories.add(path.toAbsolutePath());
    }

    private Path resolveTemplate(String relativePath) {
        return templateDirectories.stream()
                .map(p -> p.resolve(relativePath))
                .filter(p -> Files.exists(p))
                .findFirst()
                .orElse(null);
    }

    private Class<Template> compile(Path templateFile) throws IOException, ClassNotFoundException {
        final var className = "Template_" +
                templateFile.getFileName().toString().replaceAll("[^a-zA-Z0-9_]", "");
        final var javaSourceFile = Paths.get("tmp").resolve(className + ".java").toAbsolutePath();

        if (!Files.exists(javaSourceFile.getParent())) {
            Files.createDirectories(javaSourceFile.getParent());
        }

        try (final var writer = Files.newBufferedWriter(javaSourceFile)) {
            writer.append("public class ").append(className).append(" extends ")
                    .append(Template.class.getName()).append(" {\n");
            writer.append("public ").append(className).append("(").append(SimpleTemplateEngine.class.getName())
                    .append(" engine, ").append(Context.class.getName()).append(" context) {\n")
                    .append("super(engine, context);\n").append("}\n");
            writer.append("@Override\npublic void render() throws Exception {\n");

            var lastChar = 0;
            var currentChar = 0;
            var state = State.HtmlContent;
            var shouldEscape = true;

            writer.append("write(\"");

            try (final var reader = Files.newBufferedReader(templateFile)) {
                var skipCounter = 1;

                while ((currentChar = reader.read()) >= 0) {
                    switch (state) {
                        case HtmlContent:
                            if (lastChar == '<' && currentChar == '%') {
                                state = State.ScriptContent;
                                skipCounter = 2;
                                writer.append("\");\n");
                                break;
                            }

                            shouldEscape = true;
                            break;

                        case ScriptContent:
                            if (lastChar == '%' && currentChar == '=') {
                                state = State.ExpressionContent;
                                skipCounter = 2;
                                writer.append("write(String.valueOf(");
                                break;
                            } else if (lastChar == '%' && currentChar == '>') {
                                state = State.HtmlContent;
                                skipCounter = 2;
                                writer.append("\n").append("write(\"");
                                break;
                            }

                            shouldEscape = false;
                            break;

                        case ExpressionContent:
                            if (lastChar == '%' && currentChar == '>') {
                                state = State.HtmlContent;
                                skipCounter = 2;
                                writer.append("));\n").append("write(\"");
                                break;
                            }

                            shouldEscape = false;
                            break;

                        default:
                            throw new IllegalStateException("Unexpected value: " + state);
                    }

                    if (skipCounter <= 0) {
                        if (lastChar == '\n') {
                            writer.write("\\n");
                        } else if (shouldEscape && lastChar == '"') {
                            writer.write("\\\"");
                        } else if (lastChar != '\r') {
                            writer.write(lastChar);
                        }
                    } else {
                        skipCounter--;
                    }

                    lastChar = currentChar;
                }
            }

            writer.write(lastChar);
            writer.append("\");\n").append("}\n").append("}\n");
        }

        Files.deleteIfExists(javaSourceFile.getParent().resolve(className + ".class"));

        final var javaCompiler = ToolProvider.getSystemJavaCompiler();
        javaCompiler.run(null, null, null, javaSourceFile.toString());

        final var classLoader = URLClassLoader.newInstance(new URL[] { javaSourceFile.getParent().toUri().toURL() },
                getClass().getClassLoader());
        return (Class<Template>) Class.forName(className, true, classLoader);
    }

    @Override
    public Template createFromTemplate(String path, Context context) throws IllegalAccessException,
            InvocationTargetException, InstantiationException {
        final var templateInfo = templates.computeIfAbsent(path, k -> {
            final var templatePath = resolveTemplate(path);

            if (templatePath == null) {
                return null;
            }

            try {
                final var clazz = compile(templatePath);
                templatePath.getParent().register(watcher, ENTRY_DELETE, ENTRY_MODIFY);
                return new TemplateInfo(clazz, templatePath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        });

        if (templateInfo == null) {
            return null;
        }

        return (Template) templateInfo.templateClass.getConstructors()[0].newInstance(this, context);
    }

    private void watchTemplates() {
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            final var key = watcher.poll();

            if (key == null) {
                continue;
            }

            for (final var event : key.pollEvents()) {
                final var kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                final var ev = (WatchEvent<Path>) event;
                final var filename = ev.context();
                final var dir = (Path) key.watchable();
                final var fullPath = dir.resolve(filename).toAbsolutePath();

                final var iter = templates.entrySet().iterator();

                while (iter.hasNext()) {
                    final var entry = iter.next();

                    if (!entry.getValue().fullPath.equals(fullPath)) {
                        continue;
                    }

                    try {
                        ((URLClassLoader) entry.getValue().templateClass.getClassLoader()).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    iter.remove();
                }
            }

            key.reset();
        }
    }
}
