package org.example.webframework.lesson2;

import java.nio.ByteBuffer;

public class HelloWorldHandler implements Handler {
    @Override
    public void handle(Context context, HandlerChain chain) {
        context.getResponse()
                .setStatusCode(200)
                .setContentType("text/plain")
                .setContentLength(13)
                //.useChunkedEncoding()
                .write(ByteBuffer.wrap("Hello, world!".getBytes()))
                .end();

        chain.nextHandler(context);
    }
}
