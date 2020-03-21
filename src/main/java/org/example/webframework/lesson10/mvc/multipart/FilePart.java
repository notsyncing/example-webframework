package org.example.webframework.lesson10.mvc.multipart;

import org.example.webframework.lesson10.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FilePart extends Part {
    private static final Path TEMP_PATH = Paths.get("tmp").toAbsolutePath();

    private Path tempFile;
    private OutputStream stream;

    public FilePart(ContentDisposition contentDisposition, ContentType contentType) throws IOException {
        super(contentDisposition, contentType);

        tempFile = Files.createFile(TEMP_PATH.resolve(UUID.randomUUID().toString() + ".tmp"));
        stream = new BufferedOutputStream(Files.newOutputStream(tempFile));
    }

    @Override
    public void parse(int b) throws IOException {
        stream.write(b);
    }

    @Override
    public void done(Request request) throws Exception {
        final var info = new UploadedFile(tempFile, getContentDisposition().getParameter("name"),
                getContentDisposition().getParameter("filename"));
        request.addUploadedFile(info);

        super.done(request);
    }

    @Override
    public void close() throws Exception {
        stream.close();
    }
}
