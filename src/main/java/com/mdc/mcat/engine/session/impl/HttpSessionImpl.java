package com.mdc.mcat.engine.session.impl;

import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.engine.entity.request.HttpServletRequestImpl;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSessionImpl implements HttpSession {
    public static int DEFAULT_MAX_INACTIVE_INTERVAL = 600;
    public static int SECOND_TO_MS = 1000;
    private int maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL;
    private final String id;
    private final long createTime;
    private long lastAccessTime;
    private final ServletContext servletContext;
    Map<String, Object> attributes = new ConcurrentHashMap<>();

    public HttpSessionImpl(String id, ServletContext context) {
        this.id = id;
        this.createTime = System.currentTimeMillis();
        lastAccessTime = this.createTime;
        this.servletContext = context;
    }

    @Override
    public long getCreationTime() {
        return createTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessTime;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessTime = lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval * SECOND_TO_MS;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (attributes.put(name, value) == null) {
            ((ServletContextImpl) servletContext).getListenerWrapper().invokeAttributeAdded(new HttpSessionBindingEvent(this, name, value));
        } else {
            ((ServletContextImpl) servletContext).getListenerWrapper().invokeAttributeReplaced(new HttpSessionBindingEvent(this, name, value));
        }
    }

    @Override
    public void removeAttribute(String name) {
        ((ServletContextImpl) servletContext).getListenerWrapper().invokeAttributeRemoved(new HttpSessionBindingEvent(this, name, attributes.get(name)));
        attributes.remove(name);
    }

    @Override
    public void invalidate() {
        ((ServletContextImpl) this.servletContext).getSessionManager().remove(id);
        ((ServletContextImpl) this.servletContext).getListenerWrapper().invokeSessionDestroyed(this);
        attributes.clear();
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
