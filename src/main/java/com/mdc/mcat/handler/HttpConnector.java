package com.mdc.mcat.handler;

import com.mdc.mcat.adapter.HttpExchangeAdapter;
import com.mdc.mcat.entity.request.HttpServletRequestImpl;
import com.mdc.mcat.entity.response.HttpServletResponseImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class HttpConnector implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        var adapter = new HttpExchangeAdapter(httpExchange);
        var request = new HttpServletRequestImpl(adapter);
        var response = new HttpServletResponseImpl(adapter);
        process(request, response);
    }

    void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // TODO
        String name = request.getParameter("name");
        String html = "<h1>Hello, " + (name == null ? "world" : name) + ".</h1>";
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        pw.write(html);
        pw.close();
    }
}
