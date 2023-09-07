package com.mdc.mcat;

import com.mdc.mcat.engine.classloader.WebAppClassLoader;
import com.mdc.mcat.engine.connect.HttpConnector;
import com.mdc.mcat.utils.ArgUtils;
import com.mdc.mcat.utils.FileUtils;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final static String DEFAULT_HOST = "0.0.0.0";
    private final static int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException, ServletException, URISyntaxException {
        logger.info("server starting in path: {}", System.getProperty("user.dir"));
        try {
            var argsMap = ArgUtils.parseArgs(args);
            String[] warFiles = argsMap.get("--war").toArray(new String[0]);
            String host = DEFAULT_HOST;
            int port = DEFAULT_PORT;
            if (argsMap.containsKey("--host")) {
                host = argsMap.get("--host").get(0);
            }
            if (argsMap.containsKey("--port")) {
                port = Integer.parseInt(argsMap.get("--port").get(0));
            }
            for (String wf : warFiles) {
                String warName = wf.split("\\.")[0];
                Path base = Paths.get("tmp/" + warName).normalize().toAbsolutePath();
                // 清空目录
                FileUtils.deleteDir(base);
                // 解压war包
                FileUtils.unzipJars(new File(wf), base);
            }
            // 指定自定义ClassLoader的ClassPath
            List<Path> paths = new ArrayList<>();
            for (String wf : warFiles) {
                paths.add(Paths.get("tmp/" + wf.split("\\.")[0]).normalize().toAbsolutePath());
            }
            URL[] classes = paths.stream().map(
                    p -> {
                        p = p.resolve("WEB-INF/classes").normalize().toAbsolutePath();
                        return p;
                    }).map(Path::toUri).map(uri -> {
                        try {
                            return uri.toURL();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).toArray(URL[]::new);
            URL[] lib = paths.stream().map(
                    p -> {
                        p = p.resolve("WEB-INF/lib").normalize().toAbsolutePath();
                        return p;
                    }).map(Path::toUri).map(uri -> {
                        try {
                            return uri.toURL();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).toArray(URL[]::new);
            WebAppClassLoader classLoader = new WebAppClassLoader(classes, lib);
            // 创建HttpConnector
            HttpConnector connector = new HttpConnector(host, port);
            connector.setClassLoader(classLoader);
            connector.initialize(classes);
            connector.start();
        } finally {
            // 清空tmp目录
            Path tmp = Paths.get("tmp").normalize().toAbsolutePath();
            FileUtils.deleteDir(tmp);
        }
    }
}
