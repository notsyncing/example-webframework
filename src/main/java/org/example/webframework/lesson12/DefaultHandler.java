package org.example.webframework.lesson12;

import javax.inject.Named;
import java.nio.ByteBuffer;

@Named
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
