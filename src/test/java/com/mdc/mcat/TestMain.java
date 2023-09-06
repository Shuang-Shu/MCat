package com.mdc.mcat;

import com.mdc.mcat.engine.connect.HttpConnector;
import com.mdc.mcat.filter.HelloFilter;
import com.mdc.mcat.filter.LogFilter;
import com.mdc.mcat.listener.HelloHttpSessionAttributeListener;
import com.mdc.mcat.servlet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

public class TestMain {
    public static void main(String[] args) throws IOException, ServletException {
        HttpConnector httpConnector = new HttpConnector("0.0.0.0", 8080);
        httpConnector.initialize(
                List.of(
                        HelloServlet.class,
                        IndexServlet.class,
                        CookieServlet.class,
                        LoginServlet.class,
                        LogoutServlet.class,
                        HelloHttpSessionAttributeListener.class,
//                        HelloFilter.class,
                        LogFilter.class
                )
        );
        httpConnector.start();
    }
}
