package org.example.webframework.lesson12.template;

import java.nio.file.Path;
import java.util.Set;

public interface TemplateEngineConfig {
    Set<Path> getTemplateDirectories();
}
