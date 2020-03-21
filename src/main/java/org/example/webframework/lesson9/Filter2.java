package org.example.webframework.lesson9;

import org.example.webframework.lesson9.filter.Filter;
import org.example.webframework.lesson9.filter.FilterChain;

public class Filter2 implements Filter {
    @Override
    public void doFilter(Context context, FilterChain chain) throws Exception {
        System.out.println("Filter 2 enter!");

        chain.nextFilter(context);

        System.out.println("Filter 2 exit!");
    }
}
