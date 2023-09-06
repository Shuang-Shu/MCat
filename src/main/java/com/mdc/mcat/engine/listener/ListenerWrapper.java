package com.mdc.mcat.engine.listener;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

@Data
public class ListenerWrapper {
    private List<ServletContextListener> servletContextListeners = new ArrayList<>();
    private List<HttpSessionListener> httpSessionListeners = new ArrayList<>();
    private List<ServletRequestListener> servletRequestListeners = new ArrayList<>();
    private List<ServletContextAttributeListener> servletContextAttributeListeners = new ArrayList<>();
    private List<HttpSessionAttributeListener> httpSessionAttributeListeners = new ArrayList<>();
    private List<ServletRequestAttributeListener> servletRequestAttributeListeners = new ArrayList<>();

    private final ServletContext servletContext;

    public ListenerWrapper(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public <T extends EventListener> void addListener(T listener) {
        if (listener instanceof ServletContextListener) {
            servletContextListeners.add((ServletContextListener) listener);
        } else if (listener instanceof HttpSessionListener) {
            httpSessionListeners.add((HttpSessionListener) listener);
        } else if (listener instanceof ServletRequestListener) {
            servletRequestListeners.add((ServletRequestListener) listener);
        } else if (listener instanceof ServletContextAttributeListener) {
            servletContextAttributeListeners.add((ServletContextAttributeListener) listener);
        } else if (listener instanceof HttpSessionActivationListener) {
            httpSessionAttributeListeners.add((HttpSessionAttributeListener) listener);
        } else if (listener instanceof ServletRequestAttributeListener) {
            servletRequestAttributeListeners.add((ServletRequestAttributeListener) listener);
        }
    }

    public void invokeContextInitialized() {
        servletContextListeners.forEach(t -> t.contextInitialized(new ServletContextEvent(servletContext)));
    }

    public void invokeContextDestroyed() {
        servletContextListeners.forEach(t -> t.contextDestroyed(new ServletContextEvent(servletContext)));
    }

    public void invokeSessionCreated(HttpSession session) {
        httpSessionListeners.forEach(t -> t.sessionCreated(new HttpSessionEvent(session)));
    }

    public void invokeSessionDestroyed(HttpSession session) {
        httpSessionListeners.forEach(t -> t.sessionDestroyed(new HttpSessionEvent(session)));
    }

    public void invokeRequestInitialized(ServletRequest request) {
        servletRequestListeners.forEach(t -> t.requestInitialized(new ServletRequestEvent(servletContext, request)));
    }

    public void invokeRequestDestroyed(ServletRequest request) {
        servletRequestListeners.forEach(t -> t.requestDestroyed(new ServletRequestEvent(servletContext, request)));
    }

    public void invokeAttributeAdded(ServletContextAttributeEvent event) {
        servletContextAttributeListeners.forEach(t -> t.attributeAdded(event));
    }

    public void invokeAttributeRemoved(ServletContextAttributeEvent event) {
        servletContextAttributeListeners.forEach(t -> t.attributeRemoved(event));
    }

    public void invokeAttributeReplaced(ServletContextAttributeEvent event) {
        servletContextAttributeListeners.forEach(t -> t.attributeReplaced(event));
    }

    public void invokeAttributeAdded(HttpSessionBindingEvent event) {
        httpSessionAttributeListeners.forEach(t -> t.attributeAdded(event));
    }

    public void invokeAttributeRemoved(HttpSessionBindingEvent event) {
        httpSessionAttributeListeners.forEach(t -> t.attributeRemoved(event));
    }

    public void invokeAttributeReplaced(HttpSessionBindingEvent event) {
        httpSessionAttributeListeners.forEach(t -> t.attributeReplaced(event));
    }

    public void invokeAttributeReplaced(ServletRequestAttributeEvent srae) {
        servletRequestAttributeListeners.forEach(t -> t.attributeReplaced(srae));
    }

    public void invokeAttributeAdded(ServletRequestAttributeEvent srae) {
        servletRequestAttributeListeners.forEach(t -> t.attributeAdded(srae));
    }

    public void invokeAttributeRemoved(ServletRequestAttributeEvent srae) {
        servletRequestAttributeListeners.forEach(t -> t.attributeRemoved(srae));
    }
}
