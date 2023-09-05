package com.mdc.mcat.engine.context;

import com.mdc.mcat.engine.filter.http.FilterRegistrationImpl;
import com.mdc.mcat.engine.filter.http.impl.ListHttpFilterChain;
import com.mdc.mcat.engine.mapping.impl.FilterMapping;
import com.mdc.mcat.engine.mapping.impl.ServletMapping;
import com.mdc.mcat.utils.AnnoUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
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
import java.util.stream.Collectors;

public class ServletContextImpl implements ServletContext {
    private final static Servlet DEFAULT_SERVLET = new DefaultServlet();

    static class DefaultServlet implements Servlet {

        @Override
        public void init(ServletConfig config) throws ServletException {

        }

        @Override
        public ServletConfig getServletConfig() {
            return new ServletConfig() {
                @Override
                public String getServletName() {
                    return "defaultServlet";
                }

                @Override
                public ServletContext getServletContext() {
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
            };
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
    private final Map<String, ServletRegistration.Dynamic> servletRegistrationMap = new HashMap<>();
    private final Map<String, FilterRegistration.Dynamic> filterRegistrationMap = new HashMap<>();
    private final Map<String, Servlet> nameToServletMap = new HashMap<>();
    private final Map<String, Filter> nameToFilterMap = new HashMap<>();
    private final List<ServletMapping> servletMappingList = new ArrayList<>();
    private final List<FilterMapping> filterMappingList = new ArrayList<>();
    private final String contextPath;
    private Servlet defaultServlet = DEFAULT_SERVLET;
    private boolean isInitialized = false;
    private Map<String, Object> contextParams = new HashMap<>();

    public ServletContextImpl(String contextPath) {
        this.contextPath = contextPath;
    }

    public void initialize(List<Class<?>> annotationedClasses) throws ServletException {
        if (isInitialized) {
            throw new IllegalStateException("this servlet context has been initialized");
        }
        // 1 注册所有Servlet（同时进行实例化）
        for (var clazz : annotationedClasses) {
            if (clazz.isAnnotationPresent(WebServlet.class)) {
                if (!Servlet.class.isAssignableFrom(clazz)) {
                    throw new ServletException("@WebServlet should be on Servlet class");
                }
                WebServlet webServlet = clazz.getAnnotation(WebServlet.class);
                var servletRegistration = (ServletRegistrationImpl) addServlet(webServlet.name(), (Class<? extends Servlet>) clazz);
                servletRegistrationMap.put(webServlet.name(), servletRegistration);
                var initParams = AnnoUtils.getInitParams(clazz.getAnnotation(WebServlet.class));
                servletRegistration.setInitParameters(initParams);
            } else if (clazz.isAnnotationPresent(WebFilter.class)) {
                if (!Filter.class.isAssignableFrom(clazz)) {
                    throw new ServletException("@WebFilter should be on Filter class");
                }
                WebFilter webFilter = clazz.getAnnotation(WebFilter.class);
                var filterRegistraion = (FilterRegistrationImpl) addFilter(webFilter.filterName(), (Class<? extends Filter>) clazz);
                filterRegistrationMap.put(filterRegistraion.getName(), filterRegistraion);
                var initParams = AnnoUtils.getInitParams(clazz.getAnnotation(WebFilter.class));
                filterRegistraion.setInitParameters(initParams);
            }
        }
        // 2 使用ServletConfig配置所有Servlet
        for (String name : servletRegistrationMap.keySet()) {
            var registration = ((ServletRegistrationImpl) servletRegistrationMap.get(name));
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
        // 3 使用FilterConfig配置所有Filter
        for (String name : filterRegistrationMap.keySet()) {
            var registration = ((FilterRegistrationImpl) filterRegistrationMap.get(name));
            try {
                registration.getFilterMapping().getFilter().init(
                        registration.getFilterConfig()
                );
            } catch (ServletException e) {
                logger.error("ServletContext: {} initializing failed", this);
            }
            nameToFilterMap.put(registration.getName(), registration.getFilterMapping().getFilter());
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
                FilterChain filterChain = buildFilterChain(servletMapping.getServlet(), collectFilters(servletMapping.getServlet().getServletConfig(), uri));
                filterChain.doFilter(req, resp);
                matchServlet = servletMapping.getServlet();
                break;
            }
        }
        if (matchServlet == null) {
            FilterChain filterChain = buildFilterChain(defaultServlet, collectFilters(defaultServlet.getServletConfig(), uri));
            filterChain.doFilter(req, resp);
        } else {
            matchServlet.service(req, resp);
        }
    }

    private FilterChain buildFilterChain(Servlet servlet, List<Filter> filters) {
        var filterChain = new ListHttpFilterChain(servlet);
        filterChain.initialize(filters);
        return filterChain;
    }

    private List<Filter> collectFilters(ServletConfig servletConfig, String uri) {
        return this.filterMappingList.stream().filter(
                f -> {
                    return f.match(uri) || f.getIsDefault();
                }
        ).filter(
                f -> f.matchServlet(servletConfig.getServletName())
        ).map(FilterMapping::getFilter).toList();
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
        ServletRegistration.Dynamic registration = null;
        try {
            Class<?> servletClazz = Class.forName(className);
            registration = addServlet(servletName, (Class<? extends Servlet>) servletClazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return registration;
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
        var registration = ServletRegistrationImpl.builder().name(servletName).context(this).servletClass(servlet.getClass()).servletMapping(new ServletMapping(servlet, webServlet.value())).build();
        registration.getServletParams().putAll(
                AnnoUtils.getInitParams(webServlet)
        );
        if (registration.getServletMapping().getIsDefault()) {
            if (defaultServlet == DEFAULT_SERVLET) {
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
        FilterRegistration.Dynamic registration = null;
        try {
            Class<? extends Filter> clazz = (Class<? extends Filter>) Class.forName(className);
            registration = addFilter(filterName, clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return registration;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        Filter filter = null;
        try {
            Constructor<Filter> constructor = (Constructor<Filter>) filterClass.getConstructor();
            filter = constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return addFilter(filterName, filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        var webFilter = filter.getClass().getAnnotation(WebFilter.class);
        var registration = FilterRegistrationImpl.builder().name(filterName).context(this).filterClass(filter.getClass()).filterMapping(new FilterMapping(filter, webFilter.value(), webFilter.servletNames())).build();
        registration.getFilterParams().putAll(
                AnnoUtils.getInitParams(webFilter)
        );
        this.filterMappingList.add(registration.getFilterMapping());
        return registration;
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