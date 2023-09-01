package com.mdc.mcat.server;

import com.mdc.mcat.handler.HttpConnector;
import com.mdc.mcat.handler.SimpleHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SimpleServer implements HttpHandler, AutoCloseable {
    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 8080;
        try (SimpleServer connector = new SimpleServer(host, port)) {
            for (; ; ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final String host;
    final int port;
    final HttpServer httpServer;
    final HttpHandler httpConnector;

    public SimpleServer(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
//        this.httpServer = HttpServer.create();
        var inetSocket = new InetSocketAddress(this.host, this.port);
        this.httpServer = HttpServer.create(inetSocket, 0);
        this.httpServer.createContext("/", this);
        this.httpConnector = new HttpConnector();
        this.httpServer.start();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        this.httpConnector.handle(httpExchange);
    }

    @Override
    public void close() throws Exception {

    }
}
