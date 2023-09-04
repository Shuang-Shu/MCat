package com.mdc.mcat.engine.mapping;

import com.mdc.mcat.utils.RegUtils;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.util.regex.Pattern;

@Data
public class ServletMapping {
    private Servlet servlet;
    private Pattern pattern;

    public ServletMapping(Servlet servlet, String patternUri) {
        this.servlet = servlet;
        this.pattern = Pattern.compile(RegUtils.formatPatternUri(patternUri));
    }

    public boolean match(String uri) {
        return pattern.matcher(uri).matches();
    }

    public void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.service(req, resp);
    }
}
