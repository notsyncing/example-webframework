package org.example.webframework.lesson9.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.webframework.lesson9.Context;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Json implements MvcResult {
    private static final ObjectMapper json = new ObjectMapper();

    private final Object object;

    public Json(Object object) {
        this.object = object;
    }

    @Override
    public void process(Context context, MvcHandler handler) throws Exception {
        final var content = json.writeValueAsString(object);
        final var data = content.getBytes(StandardCharsets.UTF_8);

        context.getResponse()
                .setStatusCode(200)
                .setContentType("application/json")
                .setContentLength(data.length)
                .write(ByteBuffer.wrap(data))
                .end();
    }
}
