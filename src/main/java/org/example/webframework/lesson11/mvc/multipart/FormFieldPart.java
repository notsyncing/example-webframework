package org.example.webframework.lesson11.mvc.multipart;

import org.example.webframework.lesson11.Request;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class FormFieldPart extends Part {
    private ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public FormFieldPart(ContentDisposition contentDisposition, ContentType contentType) {
        super(contentDisposition, contentType);
    }

    @Override
    public void parse(int b) {
        stream.write(b);
    }

    @Override
    public void done(Request request) throws Exception {
        final var data = stream.toByteArray();
        request.addParameter(getContentDisposition().getParameter("name"),
                new String(data, StandardCharsets.UTF_8));

        super.done(request);
    }
}
