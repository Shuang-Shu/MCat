package com.mdc.mcat.handler;

import com.mdc.mcat.adapter.HttpExchangeAdapter;
import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.engine.entity.request.HttpServletRequestImpl;
import com.mdc.mcat.engine.entity.response.HttpServletResponseImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class HttpConnector implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnector.class);
    private ServletContextImpl servletContext;

    public void initialize(List<Class<?>> servletClasses) {
        servletContext = new ServletContextImpl();
        servletContext.initialize(servletClasses);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        var adapter = new HttpExchangeAdapter(httpExchange);
        var request = new HttpServletRequestImpl(adapter);
        var response = new HttpServletResponseImpl(adapter);
        process(request, response);
    }

    void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            servletContext.process(request, response);
        } catch (ServletException e) {
            logger.error(e.getMessage());
            PrintWriter pw = new PrintWriter(response.getOutputStream(), true);
            pw.write("<h1>500 Internal Server Error</h1><p>" + e.getMessage() + "</p>");
            pw.close();
        }
    }
}
