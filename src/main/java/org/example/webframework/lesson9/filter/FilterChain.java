package org.example.webframework.lesson9.filter;

import org.example.webframework.lesson9.Context;
import org.example.webframework.lesson9.HandlerChain;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class FilterChain {
    private Queue<Filter> filters;
    private HandlerChain handlerChain;

    public FilterChain(List<Filter> filters, HandlerChain handlerChain) {
        this.filters = new LinkedBlockingQueue<>(filters);
        this.handlerChain = handlerChain;
    }

    public void nextFilter(Context context) throws Exception {
        final var filter = filters.poll();

        if (filter == null) {
            handlerChain.nextHandler(context);
            return;
        }

        filter.doFilter(context, this);
    }
}
