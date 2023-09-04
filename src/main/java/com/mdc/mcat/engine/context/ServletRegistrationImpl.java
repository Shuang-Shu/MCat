package com.mdc.mcat.engine.context;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ServletRegistrationImpl implements ServletRegistration.Dynamic {
    private Servlet servlet;

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRunAsRole(String roleName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getMappings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRunAsRole() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return null;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }
}