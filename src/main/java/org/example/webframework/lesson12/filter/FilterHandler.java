package org.example.webframework.lesson12.filter;

import org.example.webframework.lesson12.Context;
import org.example.webframework.lesson12.Handler;
import org.example.webframework.lesson12.HandlerChain;
import org.example.webframework.lesson12.ioc.Container;
import org.example.webframework.lesson12.ioc.Order;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class FilterHandler implements Handler {
    private final Container container;
    private final List<Filter> filters = new ArrayList<>();

    public FilterHandler(Container container) {
        this.container = container;

        scanFilters();
    }

    private void scanFilters() {
        final var list = container.getRegistry().getListOfType(Filter.class)
                .stream()
                .sorted((a, b) -> {
                    final var orderA = a.isAnnotationPresent(Order.class) ? a.getAnnotation(Order.class).value() : 0;
                    final var orderB = b.isAnnotationPresent(Order.class) ? b.getAnnotation(Order.class).value() : 0;
                    return orderA - orderB;
                })
                .map(t -> {
                    try {
                        return (Filter) container.get(t);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        filters.addAll(list);
    }

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
