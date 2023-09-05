package com.mdc.mcat.engine.filter.http.impl;

import com.mdc.mcat.engine.filter.http.AbstractHttpFilterChain;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.Servlet;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class ListHttpFilterChain extends AbstractHttpFilterChain {
    private List<Filter> filters;
    private boolean initialized = false;
    private Iterator<Filter> filterIterator;

    public ListHttpFilterChain(Servlet servlet) {
        super(servlet);
    }

    public void initialize(List<Filter> filters) {
        this.filters = filters;
        filterIterator = filters.iterator();
        initialized = true;
    }

    @Override
    protected Filter nextFilter() {
        return filterIterator.next();
    }

    @Override
    protected boolean hasNextFilter() {
        return filterIterator.hasNext();
    }

    @Override
    protected boolean hasInitialized() {
        return initialized;
    }
}
