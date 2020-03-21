package org.example.webframework.lesson9.filter;

import org.example.webframework.lesson9.Context;
import org.example.webframework.lesson9.Handler;
import org.example.webframework.lesson9.HandlerChain;

import java.util.ArrayList;
import java.util.List;

public class FilterHandler implements Handler {
    private List<Filter> filters = new ArrayList<>();

    @Override
    public void handle(Context context, HandlerChain chain) {
        final var filterChain = new FilterChain(filters, chain);

        try {
            filterChain.nextFilter(context);
        } catch (Exception e) {
            e.printStackTrace();

            context.getResponse()
                    .setStatusCode(500)
                    .setContentLength(0)
                    .end();

            return;
        }
    }

    public FilterHandler addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }
}
