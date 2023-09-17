package com.mdc.mcat.engine.connect;

import com.mdc.mcat.adapter.httpexchange.HttpExchangeAdapter;
import com.mdc.mcat.engine.config.Configuration;
import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.engine.entity.request.HttpServletRequestImpl;
import com.mdc.mcat.engine.entity.response.HttpServletResponseImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

public class HttpConnector implements HttpHandler, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnector.class);
    private final ServletContextImpl servletContext;
    private final HttpServer server;
    private volatile boolean isStarted = false;

    public HttpConnector() throws IOException {
        this(new ServletContextImpl(new Configuration()));
    }

    public HttpConnector(ServletContextImpl servletContext) throws IOException {
        this.servletContext = servletContext;
        server = HttpServer.create(new InetSocketAddress(servletContext.getConfiguration().getHost(), servletContext.getConfiguration().getPort()), 0);
        server.createContext(servletContext.getConfiguration().getContextPath(), this);
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

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        var adapter = new HttpExchangeAdapter(httpExchange);
        var response = new HttpServletResponseImpl(adapter);
        var request = new HttpServletRequestImpl(adapter, response, this.servletContext);
        servletContext.getListenerWrapper().invokeRequestInitialized(request);
        process(request, response);
        response.cleanup(200);
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

    public ServletContextImpl getServletContext() {
        return servletContext;
    }
}
