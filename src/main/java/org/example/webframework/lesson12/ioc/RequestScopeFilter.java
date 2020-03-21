package org.example.webframework.lesson12.ioc;

import org.example.webframework.lesson12.Context;
import org.example.webframework.lesson12.filter.Filter;
import org.example.webframework.lesson12.filter.FilterChain;

import javax.inject.Named;

@Named
@Order(1)
public class RequestScopeFilter implements Filter {
    @Override
    public void doFilter(Context context, FilterChain chain) throws Exception {
        RequestScope.reset();

        chain.nextFilter(context);

        RequestScope.close();
    }
}
