package org.example.webframework.lesson4;

import java.nio.ByteBuffer;

public class DefaultHandler implements Handler {
    @Override
    public void handle(Context context, HandlerChain chain) {
        if (!context.getResponse().isEnd()) {
            context.getResponse()
                    .setStatusCode(404)
                    .setContentLength(9)
                    .write(ByteBuffer.wrap("Not found".getBytes()))
                    .end();
        }

        chain.nextHandler(context);
    }
}
