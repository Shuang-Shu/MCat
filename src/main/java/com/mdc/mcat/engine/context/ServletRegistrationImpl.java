package com.mdc.mcat.engine.context;

import com.mdc.mcat.engine.mapping.impl.ServletMapping;
import jakarta.servlet.*;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class ServletRegistrationImpl implements ServletRegistration.Dynamic {
    private String name;
    private ServletContext context;
    private Class<? extends Servlet> servletClass;
    private ServletMapping servletMapping;
    private int loadOnStartup;
    private final Map<String, String> servletParams = new HashMap<>();

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
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
        Set<String> confict = new HashSet<>();
        for (String p : urlPatterns) {
            if (!servletMapping.addMapping(p)) {
                confict.add(p);
            }
        }
        return confict;
    }

    @Override
    public Collection<String> getMappings() {
        return servletMapping.getMappings();
    }

    @Override
    public String getRunAsRole() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return servletClass.getClass().getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public String getInitParameter(String name) {
        return servletParams.get(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        Set<String> confict = new HashSet<>();
        for (String key : initParameters.keySet()) {
            if (this.servletParams.containsKey(key)) {
                confict.add(key);
            } else {
                initParameters.put(key, initParameters.get(key));
            }
        }
        return confict;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return servletParams;
    }

    public ServletConfig getServletConfig() {
        return new ServletConfig() {
            @Override
            public String getServletName() {
                return ServletRegistrationImpl.this.name;
            }

            @Override
            public ServletContext getServletContext() {
                return ServletRegistrationImpl.this.context;
            }

            @Override
            public String getInitParameter(String name) {
                return ServletRegistrationImpl.this.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(ServletRegistrationImpl.this.getInitParameters().keySet());
            }
        };
    }
}