package org.example.webframework.lesson7.mvc;

import org.example.webframework.lesson7.Context;
import org.example.webframework.lesson7.file.ServerFile;

import java.nio.file.Path;

public class File implements MvcResult {
    private Path path;
    private String newName;

    public File(Path path, String newName) {
        this.path = path;
        this.newName = newName;
    }

    public File(String path, String newName) {
        this(Path.of(path), newName);
    }

    public File(Path path) {
        this(path, null);
    }

    public File(String path) {
        this(path, null);
    }

    @Override
    public void process(Context context, MvcHandler handler) throws Exception {
        new ServerFile(path)
                .setNewName(newName)
                .send(context);
    }
}
