package com.mdc.mcat.engine.mapping.impl;

import com.mdc.mcat.engine.mapping.AbstractMapping;
import jakarta.servlet.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilterMapping extends AbstractMapping implements Filter {
    private Set<String> servletNameMap = new HashSet<>();
    private Filter filter;
    private FilterChain filterChain;

    public FilterMapping() {
        super();
    }

    public FilterMapping(Filter filter, String[] patternUri) {
        this(filter, patternUri, new String[]{});
    }

    public FilterMapping(Filter filter, String[] patternUri, String[] servletNames) {
        super(patternUri);
        this.filter = filter;
        servletNameMap.addAll(Arrays.asList(servletNames));
    }

    public boolean matchServlet(String servletName) {
        if (servletNameMap.isEmpty()) {
            return true;
        } else {
            return servletNameMap.contains(servletName);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.filter.doFilter(request, response, this.filterChain);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
