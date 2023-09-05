package com.mdc.mcat.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(
        value = "/hello",
        name = "helloServlet"
)
public class HelloServlet implements Servlet {
    private ServletConfig config;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        PrintWriter pw = res.getWriter();
        String param = req.getParameter("name");
        pw.write("<h1>Hello! " + param + "</h1>");
        pw.flush();
        pw.close();
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
