package com.mdc.mcat.servlet;

import com.mdc.mcat.engine.context.ServletContextImpl;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebServlet(
        value = "/logout"
)
public class LogoutServlet implements Servlet {
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
        var session = ((HttpServletRequest) req).getSession();
        if (session != null) {
            ((ServletContextImpl) req.getServletContext()).getSessionManager().remove(session.getId());
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
