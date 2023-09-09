package com.mdc.mcat.engine.session;

import jakarta.servlet.http.HttpSession;

public interface AbstractSessionManager {
    HttpSession getSession(String id);

    String createSession();

    void remove(String id);

    void mergeWith(AbstractSessionManager manager);
}
