package com.mdc.mcat;

import com.mdc.mcat.engine.builder.Impl.ClassLoaderBuilder;
import com.mdc.mcat.engine.builder.Impl.WarConfigBuilder;
import com.mdc.mcat.engine.builder.Impl.YamlConfigBuilder;
import com.mdc.mcat.engine.builder.parse.WarParser;
import com.mdc.mcat.engine.config.Configuration;
import com.mdc.mcat.engine.connect.HttpConnector;
import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.utils.ArgUtils;
import com.mdc.mcat.utils.FileUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Boot {
    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) throws IOException, ServletException, URISyntaxException, ClassNotFoundException {
        logger.info("server starting in path: {}", System.getProperty("user.dir"));
        try {
            var argsMap = ArgUtils.parseArgs(args);
            String[] warFiles = argsMap.get("--war").toArray(new String[0]);
            Configuration configuration = new Configuration();
            // parse args
            if (argsMap.containsKey("--host")) {
                configuration.setHost(argsMap.get("--host").get(0));
            }
            if (argsMap.containsKey("--port")) {
                configuration.setPort(Integer.parseInt(argsMap.get("--port").get(0)));
            }
            // 1 解析war包
            WarParser.URLSet urlSet = WarParser.parse(warFiles);
            // 2 构建自定义ClassLoader
            var classLoader = ClassLoaderBuilder.build(urlSet);
            configuration.setClassLoader(classLoader);
            Thread.currentThread().setContextClassLoader(classLoader);
            // 3 基于web.yaml构建Configuration
            var yamlDiffConfig = YamlConfigBuilder.build(new FileInputStream(FileUtils.loadResource("web.yaml")));
            // 4 基于classes和lib的URL构建Configuration
            var warDiffConfig = WarConfigBuilder.build(urlSet);
            // parse web.xml(TODO)
            configuration.mergeWith(yamlDiffConfig).mergeWith(warDiffConfig);
            ServletContext servletContext = new ServletContextImpl(configuration);
            // 创建HttpConnector
            HttpConnector connector = new HttpConnector((ServletContextImpl) servletContext);
            connector.getServletContext().getListenerWrapper().setContext(servletContext);
            connector.getServletContext().getListenerWrapper().invokeContextInitialized();
            connector.start();
        } finally {
            // 清空tmp目录
            Path tmp = Paths.get("tmp").normalize().toAbsolutePath();
            FileUtils.deleteDir(tmp);
        }
    }
}
