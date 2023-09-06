package com.mdc.mcat.engine.connect;

import com.mdc.mcat.adapter.HttpExchangeAdapter;
import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.engine.entity.request.HttpServletRequestImpl;
import com.mdc.mcat.engine.entity.response.HttpServletResponseImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;

public class HttpConnector implements HttpHandler, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnector.class);
    private ServletContextImpl servletContext;
    private final HttpServer server;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private final String contextPath;
    private volatile boolean isStarted = false;

    public HttpConnector() throws IOException {
        server = HttpServer.create(new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT), 0);
        contextPath = "/";
        server.createContext(contextPath, this);
    }

    public HttpConnector(String host, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        contextPath = "/";
        server.createContext(contextPath, this);
    }

    public HttpConnector(String host, int port, String contextPath) throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.contextPath = contextPath;
        server.createContext(this.contextPath, this);
    }

    public void start() {
        if (servletContext == null) {
            throw new IllegalStateException("servletContext is not initialized");
        }
        isStarted = true;
        Thread startThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        server.start();
                        do {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } while (isStarted);
                    }
                }
        );
        startThread.start();
    }

    public void initialize(List<Class<?>> servletClasses) throws ServletException {
        servletContext = new ServletContextImpl(this.contextPath);
        servletContext.initialize(servletClasses);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        var adapter = new HttpExchangeAdapter(httpExchange);
        var response = new HttpServletResponseImpl(adapter);
        var request = new HttpServletRequestImpl(adapter, response, this.servletContext);
        servletContext.getListenerWrapper().invokeRequestInitialized(request);
        process(request, response);
    }

    void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            servletContext.process(request, response);
            servletContext.getListenerWrapper().invokeRequestDestroyed(request);
        } catch (ServletException e) {
            logger.error(e.getMessage());
            PrintWriter pw = new PrintWriter(response.getOutputStream(), true);
            pw.write("<h1>500 Internal Server Error</h1><p>" + e.getMessage() + "</p>");
            pw.close();
        }
    }

    @Override
    public void close() throws Exception {
        server.stop(0);
    }
}
