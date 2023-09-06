package com.mdc.mcat.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(
        value = "/login"
)
public class LoginServlet implements Servlet {
    Map<String, String> passwdMap = Map.of("lihua", "123", "ss", "1234");
    private ServletConfig servletConfig;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.servletConfig = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws IOException {
        String name = req.getParameter("name"), password = req.getParameter("passwd");
        if (passwdMap.get(name).equals(password)) {
            var session = ((HttpServletRequest) req).getSession(true);
            session.setAttribute("name", name);
        } else {
            ((HttpServletResponse) res).sendError(403, "403, Access Forbidden");
        }
        res.getWriter().close();
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
