package org.example.webframework.lesson11.mvc.multipart;

import org.example.webframework.lesson11.Request;

import java.io.IOException;

public abstract class Part implements AutoCloseable {
    public static Part factory(String contentDispositionValue, String contentTypeValue) throws IOException {
        final var contentDisposition = new ContentDisposition(contentDispositionValue);
        final var contentType = contentTypeValue == null ? null : new ContentType(contentTypeValue);

        switch (contentDisposition.getContentDisposition()) {
            case "form-data":
                if (contentDisposition.hasParameter("filename")) {
                    return new FilePart(contentDisposition, contentType);
                } else {
                    return new FormFieldPart(contentDisposition, contentType);
                }
        }

        throw new UnsupportedOperationException("Unsupported content disposition " + contentDispositionValue);
    }

    private ContentDisposition contentDisposition;
    private ContentType contentType;

    public Part(ContentDisposition contentDisposition, ContentType contentType) {
        this.contentDisposition = contentDisposition;
        this.contentType = contentType;
    }

    public ContentDisposition getContentDisposition() {
        return contentDisposition;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public abstract void parse(int b) throws Exception;

    public void done(Request request) throws Exception {
        close();
    }

    @Override
    public void close() throws Exception {
    }
}
