package com.mdc.mcat.engine.config;

import com.mdc.mcat.engine.context.DefaultServlet;
import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.engine.context.ServletRegistrationImpl;
import com.mdc.mcat.engine.filter.http.FilterRegistrationImpl;
import com.mdc.mcat.engine.listener.ListenerWrapper;
import com.mdc.mcat.engine.mapping.impl.FilterMapping;
import com.mdc.mcat.engine.mapping.impl.ServletMapping;
import com.mdc.mcat.engine.session.AbstractSessionManager;
import com.mdc.mcat.engine.session.impl.SessionManagerImpl;
import com.mdc.mcat.utils.AnnoUtils;
import com.mdc.mcat.utils.ClassUtils;
import com.mdc.mcat.utils.StringUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: All configurations of MCat
 * @date 2023/9/8 9:19
 */
@Data
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    public final static Servlet DEFAULT_SERVLET = new DefaultServlet();
    private Servlet defaultServlet = DEFAULT_SERVLET;

    // defined in web.yaml
    private String contextPath = "/";
    private String host = "0.0.0.0";
    private int port = 8080;

    private ServletContextImpl servletContext = null;
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private ListenerWrapper listenerWrapper = new ListenerWrapper();
    private Map<String, ServletRegistration.Dynamic> servletRegistrationMap = new HashMap<>();
    private Map<String, FilterRegistration.Dynamic> filterRegistrationMap = new HashMap<>();
    private Map<String, Servlet> nameToServletMap = new HashMap<>();
    private Map<String, Filter> nameToFilterMap = new HashMap<>();
    private List<ServletMapping> servletMappingList = new ArrayList<>();
    private List<FilterMapping> filterMappingList = new ArrayList<>();
    private Map<String, Object> contextParams = new HashMap<>();
    private AbstractSessionManager sessionManager = new SessionManagerImpl();

    public Configuration mergeWith(Configuration configuration) {
        this.contextPath = configuration.contextPath;
        this.host = configuration.host;
        this.port = configuration.port;
        this.listenerWrapper.mergeWith(configuration.listenerWrapper);
        this.servletRegistrationMap.putAll(configuration.servletRegistrationMap);
        this.filterRegistrationMap.putAll(configuration.filterRegistrationMap);
        this.nameToServletMap.putAll(configuration.nameToServletMap);
        this.nameToFilterMap.putAll(configuration.nameToFilterMap);
        this.servletMappingList.addAll(configuration.servletMappingList);
        this.filterMappingList.addAll(configuration.filterMappingList);
        this.contextParams.putAll(configuration.contextParams);
        this.sessionManager.mergeWith(configuration.sessionManager);
        return this;
    }

    public void assembleWith(URL[] classURLs, URL[] libs) throws URISyntaxException, IOException {
        List<Class<?>> classes = new ArrayList<>();
        for (var classURL : classURLs) {
            Path basePath = Path.of(classURL.toURI());
            classes.addAll(
                    Files.walk(Path.of(classURL.toURI()))
                            .filter(p -> !p.toFile().isDirectory())
                            .map(Path::toFile)
                            .filter(f -> f.getName().endsWith(".class"))
                            .map(f -> {
                                String relativePath = basePath.relativize(Path.of(f.getPath())).toFile().getPath();
                                return relativePath.substring(0, relativePath.lastIndexOf(".")).replace("/", ".");
                            })
                            .map(cn -> {
                                try {
                                    return Class.forName(cn, false, Thread.currentThread().getContextClassLoader());
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }).filter(
                                    c -> c.isAnnotationPresent(WebServlet.class) || c.isAnnotationPresent(WebFilter.class) || c.isAnnotationPresent(WebListener.class)
                            )
                            .toList()
            );
        }
    }

    public void assembleWith(List<Class<?>> annotationedClasses) throws ServletException {
        // 1 注册所有Servlet（同时进行实例化）
        for (var clazz : annotationedClasses) {
            if (clazz.isAnnotationPresent(WebServlet.class)) {
                if (!Servlet.class.isAssignableFrom(clazz)) {
                    throw new ServletException("Declared servlet's class: " + clazz.getName() + " should be sub-class of Servlet");
                }
                WebServlet webServlet = clazz.getAnnotation(WebServlet.class);
                var servletRegistration = (ServletRegistrationImpl) addServlet(webServlet.name(), (Class<? extends Servlet>) clazz);
                String servletName = webServlet.name();
                if (StringUtils.isEmpty(servletName)) {
                    servletName = ClassUtils.getSimpleClassName(clazz);
                }
                servletRegistrationMap.put(servletName, servletRegistration);
                var initParams = AnnoUtils.getInitParams(clazz.getAnnotation(WebServlet.class));
                servletRegistration.setInitParameters(initParams);
            } else if (clazz.isAnnotationPresent(WebFilter.class)) {
                if (!Filter.class.isAssignableFrom(clazz)) {
                    throw new ServletException("Declared filter's class: " + clazz.getName() + " should be sub-class of Filter");
                }
                WebFilter webFilter = clazz.getAnnotation(WebFilter.class);
                var filterRegistraion = (FilterRegistrationImpl) addFilter(webFilter.filterName(), (Class<? extends Filter>) clazz);
                String filterName = webFilter.filterName();
                if (StringUtils.isEmpty(filterName)) {
                    filterName = ClassUtils.getSimpleClassName(clazz);
                }
                filterRegistrationMap.put(filterName, filterRegistraion);
                var initParams = AnnoUtils.getInitParams(clazz.getAnnotation(WebFilter.class));
                filterRegistraion.setInitParameters(initParams);
            } else if (clazz.isAnnotationPresent(WebListener.class)) {
                if (!EventListener.class.isAssignableFrom(clazz)) {
                    throw new ServletException("Declared listener's class: " + clazz.getName() + " should be sub-class of EventListener");
                }
                addListener((Class<? extends EventListener>) clazz);
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
    }

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

    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        var webServlet = servlet.getClass().getAnnotation(WebServlet.class);
        var registration = ServletRegistrationImpl.builder().name(servletName).servletClass(servlet.getClass()).servletMapping(new ServletMapping(servlet, webServlet.value())).configuration(this).build();
        registration.getServletParams().putAll(
                AnnoUtils.getInitParams(webServlet)
        );
        if (registration.getServletMapping().getIsDefault()) {
            if (defaultServlet == DEFAULT_SERVLET) {
                defaultServlet = registration.getServletMapping().getServlet();
                return registration;
            } else {
                throw new IllegalStateException("Only one default servlet can be used");
            }
        }
        servletMappingList.add(registration.getServletMapping());
        Collections.sort(servletMappingList);
        return registration;
    }

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

    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        var webFilter = filter.getClass().getAnnotation(WebFilter.class);
        var registration = FilterRegistrationImpl.builder().name(filterName).filterClass(filter.getClass()).filterMapping(new FilterMapping(filter, webFilter.value(), webFilter.servletNames())).configuration(this).build();
        registration.getFilterParams().putAll(
                AnnoUtils.getInitParams(webFilter)
        );
        filterMappingList.add(registration.getFilterMapping());
        return registration;
    }

    public void addListener(String className) {
        try {
            Class<? extends EventListener> clazz = (Class<? extends EventListener>) Class.forName(className);
            addListener(clazz);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    public <T extends EventListener> void addListener(T t) {
        listenerWrapper.addListener(t);
    }

    @SuppressWarnings("unchecked")
    public void addListener(Class<? extends EventListener> listenerClass) {
        EventListener listener = null;
        try {
            Constructor<EventListener> constructor = (Constructor<EventListener>) listenerClass.getConstructor();
            listener = constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        addListener(listener);
    }
}
