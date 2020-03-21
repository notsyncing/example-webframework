package org.example.webframework.lesson11.filter;

import org.example.webframework.lesson11.Context;

public interface Filter {
    void doFilter(Context context, FilterChain chain) throws Exception;
}
