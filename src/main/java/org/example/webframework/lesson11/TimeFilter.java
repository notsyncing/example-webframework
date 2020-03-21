package org.example.webframework.lesson11;

import org.example.webframework.lesson11.filter.Filter;
import org.example.webframework.lesson11.filter.FilterChain;

public class TimeFilter implements Filter {
    @Override
    public void doFilter(Context context, FilterChain chain) throws Exception {
        final var startTime = System.currentTimeMillis();

        chain.nextFilter(context);

        final var endTime = System.currentTimeMillis();
        System.out.println("Time: " + (endTime - startTime) + "ms");
    }
}
