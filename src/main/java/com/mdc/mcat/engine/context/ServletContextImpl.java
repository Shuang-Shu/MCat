package com.mdc.mcat.engine.context;

import com.mdc.mcat.anno.WebServlet;
import com.mdc.mcat.engine.mapping.impl.ServletMapping;
import com.mdc.mcat.utils.AnnoUtils;
import jakarta.servlet.*;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ServletContextImpl implements ServletContext {

    static class DefaultServlet implements Servlet {
        public final static DefaultServlet DEFAULT_SERVLET = new DefaultServlet();

        @Override
        public void init(ServletConfig config) throws ServletException {

        }

        @Override
        public ServletConfig getServletConfig() {
            return null;
        }

        @Override
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            ((HttpServletResponse) res).sendError(404, "Not Found");
        }

        @Override
        public String getServletInfo() {
            return null;
        }

        @Override
        public void destroy() {

        }
    }

    private final static Logger logger = LoggerFactory.getLogger(ServletContextImpl.class);
    private final Map<String, ServletRegistration.Dynamic> registrationMap = new HashMap<>();
    private final Map<String, Servlet> nameToServletMap = new HashMap<>();
    private final List<ServletMapping> servletMappingList = new ArrayList<>();
    private final String contextPath;
    private Servlet defaultServlet = DefaultServlet.DEFAULT_SERVLET;
    private boolean isInitialized = false;
    private Map<String, Object> contextParams = new HashMap<>();

    public ServletContextImpl(String contextPath) {
        this.contextPath = contextPath;
    }

    public void initialize(List<Class<? extends Servlet>> servletClasses) {
        if (isInitialized) {
            throw new IllegalStateException("this servlet context has been initialized");
        }
        // 1 注册所有Servlet（同时实例化）
        for (var clazz : servletClasses) {
            WebServlet webServlet = clazz.getAnnotation(WebServlet.class);
            if (webServlet != null) {
                var servletRegistration = (ServletRegistrationImpl) addServlet(webServlet.name(), clazz);
                registrationMap.put(webServlet.name(), servletRegistration);
                var initParams = AnnoUtils.getInitAttributes(clazz);
                servletRegistration.setInitParameters(initParams);
            }
        }
        // 2 使用ServletConfig所有Servlet
        for (String name : registrationMap.keySet()) {
            var registration = ((ServletRegistrationImpl) registrationMap.get(name));
            try {
                registration.getServletMapping().getServlet().init(
                        registration.getServletConfig()
                );
            } catch (ServletException e) {
                logger.error("ServletContext: {} initializing failed", this);
            }
            nameToServletMap.put(registration.getName(), registration.getServletMapping().getServlet());
            try {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        this.isInitialized = true;
    }

    public void process(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String uri = req.getRequestURI();
        int indexOfQ = uri.indexOf("?");
        if (indexOfQ != -1) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        Servlet matchServlet = null;
        for (ServletMapping servletMapping : servletMappingList) {
            if (servletMapping.match(uri)) {
                matchServlet = servletMapping.getServlet();
                break;
            }
        }
        if (matchServlet == null) {
            defaultServlet.service(req, resp);
        } else {
            matchServlet.service(req, resp);
        }
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public ServletContext getContext(String uripath) {
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        return null;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public void log(String msg) {

    }

    @Override
    public void log(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return contextParams.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(contextParams.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        contextParams.put(name, (String) object);
    }

    @Override
    public void removeAttribute(String name) {
        contextParams.remove(name);
    }

    @Override
    public String getServletContextName() {
        return null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        ServletRegistration.Dynamic result = null;
        try {
            Class<?> servletClazz = Class.forName(className);
            result = addServlet(servletName, (Class<? extends Servlet>) servletClazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String name, Class<? extends Servlet> servletClass) {
        Servlet servlet = null;
        try {
            Constructor<?> constructor = servletClass.getConstructor();
            servlet = (Servlet) constructor.newInstance();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return addServlet(name, servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        var webServlet = servlet.getClass().getAnnotation(WebServlet.class);
        var registration = ServletRegistrationImpl.builder().name(servletName).context(this).servletClass(servlet.getClass()).servletMapping(new ServletMapping()).build();
        registration.getServletParams().putAll(AnnoUtils.getInitAttributes(
                servlet.getClass()
        ));
        for (String url : webServlet.value()) {
            registration.getServletMapping().addMapping(url);
        }
        registration.getServletMapping().setServlet(servlet);
        if (registration.getServletMapping().getIsDefault()) {
            if (defaultServlet == DefaultServlet.DEFAULT_SERVLET) {
                defaultServlet = registration.getServletMapping().getServlet();
            } else {
                throw new IllegalStateException("Only one default servlet can be used");
            }
        }
        servletMappingList.add(registration.getServletMapping());
        Collections.sort(this.servletMappingList);
        return registration;
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String className) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void declareRoles(String... roleNames) {

    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {

    }

    @Override
    public String getRequestCharacterEncoding() {
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {

    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {

    }
}