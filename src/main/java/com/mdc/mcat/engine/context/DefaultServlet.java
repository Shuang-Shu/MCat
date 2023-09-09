package com.mdc.mcat.engine.context;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/8 20:36
 */
public class DefaultServlet implements Servlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
    }

    @Override
    public ServletConfig getServletConfig() {
        return new ServletConfig() {
            @Override
            public String getServletName() {
                return "defaultServlet";
            }

            @Override
            public ServletContext getServletContext() {
                return null;
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        ((HttpServletResponse) res).sendError(404, "Not Found");
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
