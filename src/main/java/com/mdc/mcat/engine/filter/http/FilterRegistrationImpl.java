package com.mdc.mcat.engine.filter.http;

import com.mdc.mcat.engine.config.Configuration;
import com.mdc.mcat.engine.mapping.impl.FilterMapping;
import jakarta.servlet.*;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class FilterRegistrationImpl implements FilterRegistration.Dynamic {
    private String name;
    private Configuration configuration;
    private Class<? extends Filter> filterClass;
    private FilterMapping filterMapping;
    private final Map<String, String> filterParams = new HashMap<>();

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {

    }

    @Override
    public Collection<String> getServletNameMappings() {
        return null;
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {

    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return null;
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return filterMapping.getFilter().getClass().getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return filterParams.put(name, value) == null;
    }

    @Override
    public String getInitParameter(String name) {
        return filterParams.get(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return null;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    public FilterConfig getFilterConfig() {
        return new FilterConfig() {
            @Override
            public String getFilterName() {
                return FilterRegistrationImpl.this.name;
            }

            @Override
            public ServletContext getServletContext() {
                return FilterRegistrationImpl.this.configuration.getServletContext();
            }

            @Override
            public String getInitParameter(String name) {
                return FilterRegistrationImpl.this.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(
                        FilterRegistrationImpl.this.filterParams.keySet()
                );
            }
        };
    }
}
