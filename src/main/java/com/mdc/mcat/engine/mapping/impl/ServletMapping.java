package com.mdc.mcat.engine.mapping.impl;

import com.mdc.mcat.engine.mapping.AbstractMapping;
import com.mdc.mcat.utils.RegUtils;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServletMapping extends AbstractMapping {
    private Servlet servlet;

    public ServletMapping() {
        super();
    }

    public ServletMapping(Servlet servlet, String[] patternUri) {
        super(patternUri);
        this.servlet = servlet;
    }

    public void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.service(req, resp);
    }
}
