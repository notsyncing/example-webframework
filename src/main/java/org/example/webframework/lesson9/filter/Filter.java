package org.example.webframework.lesson9.filter;

import org.example.webframework.lesson9.Context;

public interface Filter {
    void doFilter(Context context, FilterChain chain) throws Exception;
}
