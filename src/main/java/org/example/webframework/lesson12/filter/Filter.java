package org.example.webframework.lesson12.filter;

import org.example.webframework.lesson12.Context;

public interface Filter {
    void doFilter(Context context, FilterChain chain) throws Exception;
}
