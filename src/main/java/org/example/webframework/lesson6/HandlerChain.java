package org.example.webframework.lesson6;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class HandlerChain {
    private Queue<Handler> handlers;

    public HandlerChain(List<Handler> handlers) {
        this.handlers = new LinkedBlockingQueue<>(handlers);
    }

    public void nextHandler(Context context) {
        final var handler = handlers.poll();

        if (handler == null) {
            return;
        }

        handler.handle(context, this);
    }
}
