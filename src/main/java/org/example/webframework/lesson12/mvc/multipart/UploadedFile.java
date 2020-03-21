package org.example.webframework.lesson12.mvc.multipart;

import java.nio.file.Path;

public class UploadedFile {
    private Path path;
    private String name;
    private String filename;

    public UploadedFile(Path path, String name, String filename) {
        this.path = path;
        this.name = name;
        this.filename = filename;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }
}
