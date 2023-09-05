package com.mdc.mcat.engine.filter.http;

import jakarta.servlet.*;

import java.io.IOException;

public abstract class AbstractHttpFilterChain implements FilterChain {
    public AbstractHttpFilterChain(Servlet servlet) {
        this.servlet = servlet;
    }

    protected Servlet servlet;

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (!hasInitialized()) {
            throw new IllegalStateException("filter chain: {} has not been initialized");
        }
        while (hasNextFilter()) {
            var filter = nextFilter();
            filter.doFilter(request, response, this);
        }
        servlet.service(request, response);
    }

    protected abstract Filter nextFilter();

    protected abstract boolean hasNextFilter();

    protected abstract boolean hasInitialized();
}
