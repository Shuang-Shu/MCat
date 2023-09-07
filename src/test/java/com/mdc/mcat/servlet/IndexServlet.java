package com.mdc.mcat.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(
        value = "/index",
        name = "indexServlet"
)
public class IndexServlet implements Servlet {
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
    public void service(ServletRequest req, ServletResponse res) throws IOException {
        var session = ((HttpServletRequest) req).getSession();
        if (session == null || session.getAttribute("name") == null) {
            ((HttpServletResponse) res).sendError(403, "Access Forbidden");
            return;
        }
        PrintWriter pw = res.getWriter();
        pw.write("<h1>Index</h1>");
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
