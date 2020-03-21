package org.example.webframework.lesson12;

import org.example.webframework.lesson12.filter.Filter;
import org.example.webframework.lesson12.filter.FilterChain;
import org.example.webframework.lesson12.ioc.Order;

import javax.inject.Named;

@Named
@Order(Integer.MIN_VALUE)
public class TimeFilter implements Filter {
    @Override
    public void doFilter(Context context, FilterChain chain) throws Exception {
        final var startTime = System.currentTimeMillis();

        chain.nextFilter(context);

        final var endTime = System.currentTimeMillis();
        System.out.println("Time: " + (endTime - startTime) + "ms");
    }
}
