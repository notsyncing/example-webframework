package org.example.webframework.lesson12;

import org.example.webframework.lesson12.file.StaticFileConfig;
import org.example.webframework.lesson12.session.MemorySessionStorage;
import org.example.webframework.lesson12.session.SessionStorage;
import org.example.webframework.lesson12.session.SessionStorageConfig;
import org.example.webframework.lesson12.template.TemplateEngineConfig;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

@Named
@Singleton
public class Config implements TemplateEngineConfig, StaticFileConfig, SessionStorageConfig {
    @Override
    public Path getRootPath() {
        return Paths.get("static");
    }

    @Override
    public Set<Path> getTemplateDirectories() {
        return Collections.singleton(Paths.get("static"));
    }

    @Override
    public Class<? extends SessionStorage> getStorageClass() {
        return MemorySessionStorage.class;
    }
}
