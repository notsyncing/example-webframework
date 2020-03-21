package org.example.webframework.lesson10.filter;

import org.example.webframework.lesson10.Context;

public interface Filter {
    void doFilter(Context context, FilterChain chain) throws Exception;
}
