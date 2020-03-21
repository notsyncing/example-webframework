package org.example.webframework.lesson9;

import org.example.webframework.lesson9.filter.Filter;
import org.example.webframework.lesson9.filter.FilterChain;

import java.nio.ByteBuffer;

public class TestFilter implements Filter {
    @Override
    public void doFilter(Context context, FilterChain chain) throws Exception {
        if (context.getRequest().getUrl().getPath().contains("test")) {
            final var message = "Test forbidden!";

            context.getResponse()
                    .setStatusCode(403)
                    .setContentType("text/plain")
                    .setContentLength(message.length())
                    .write(ByteBuffer.wrap(message.getBytes()))
                    .end();

            return;
        }

        chain.nextFilter(context);
    }
}
