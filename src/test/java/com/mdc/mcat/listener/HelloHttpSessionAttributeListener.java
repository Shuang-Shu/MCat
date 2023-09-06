package com.mdc.mcat.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class HelloHttpSessionAttributeListener implements HttpSessionListener {
    final Logger logger = LoggerFactory.getLogger(HelloHttpSessionAttributeListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        logger.info("creating session with id: {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        logger.info("destroying session with id: {}", se.getSession().getId());
    }
}
