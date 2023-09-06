package com.mdc.mcat.engine.session.impl;

import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.engine.session.AbstractSessionManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManagerImpl implements AbstractSessionManager {
    private final Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();
    private final ServletContext servletContext;

    private class ClearExpiredRunnable implements Runnable {
        static final int SLEEP_TIME_MS = 1000;

        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(SLEEP_TIME_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                List<String> invalidKeys = new ArrayList<>();
                for (var key : sessionMap.keySet()) {
                    var session = sessionMap.get(key);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - session.getLastAccessedTime() >= session.getMaxInactiveInterval()) {
                        invalidKeys.add(key);
                    }
                }
                for (var invalidKey : invalidKeys) {
                    sessionMap.remove(invalidKey);
                }
            } while (true);
        }
    }

    public SessionManagerImpl(ServletContext servletContext) {
        this.servletContext = servletContext;
        Thread clearThread = new Thread(new ClearExpiredRunnable());
        clearThread.start();
    }

    @Override
    public HttpSession getSession(String id) {
        return sessionMap.get(id);
    }

    @Override
    public String createSession() {
        String sid = generateSessionId();
        var session = new HttpSessionImpl(sid, servletContext);
        sessionMap.put(sid, session);
        return sid;
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void remove(String id) {
        ((ServletContextImpl) servletContext).getListenerWrapper().invokeSessionDestroyed(sessionMap.remove(id));
    }
}
