package com.mdc.mcat.engine.config;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ServletConfigImpl implements ServletConfig {
    private String servletName;
    private ServletContext servletContext;
    private Map<String, String> attributes;

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(attributes.keySet());
    }
}
