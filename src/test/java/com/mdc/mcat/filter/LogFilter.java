package com.mdc.mcat.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebFilter(
        value = "/",
        filterName = "logFilter"
)
public class LogFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(LogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;
        logger.info("request: {} path: {}", httpRequest.getMethod(), httpRequest.getRequestURI());
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
