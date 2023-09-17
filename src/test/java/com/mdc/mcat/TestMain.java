package com.mdc.mcat;

import com.mdc.mcat.engine.builder.Impl.WarConfigBuilder;
import com.mdc.mcat.engine.builder.Impl.YamlConfigBuilder;
import com.mdc.mcat.engine.config.Configuration;
import com.mdc.mcat.engine.connect.HttpConnector;
import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.filter.HelloFilter;
import com.mdc.mcat.filter.LogFilter;
import com.mdc.mcat.listener.HelloHttpSessionAttributeListener;
import com.mdc.mcat.servlet.*;
import com.mdc.mcat.utils.FileUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

public class TestMain {
    public static void main(String[] args) throws IOException, ServletException, ClassNotFoundException {
        var configuration = new Configuration();
        var yamlDiffConfig = YamlConfigBuilder.build(new FileInputStream(FileUtils.loadResource("web.yaml")));
        // 4 基于classes和lib的URL构建Configuration
        // parse web.xml(TODO)
        configuration.mergeWith(yamlDiffConfig);
        ServletContext servletContext = new ServletContextImpl(configuration);
        // 创建HttpConnector
        HttpConnector connector = new HttpConnector((ServletContextImpl) servletContext);
        connector.getServletContext().getListenerWrapper().setContext(servletContext);
        connector.getServletContext().getListenerWrapper().invokeContextInitialized();
        connector.start();
    }
}
