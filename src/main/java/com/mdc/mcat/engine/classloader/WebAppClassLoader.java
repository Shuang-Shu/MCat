package com.mdc.mcat.engine.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebAppClassLoader extends URLClassLoader {
    public WebAppClassLoader(URL[] classes, URL[] libs) {
        super(mergeURL(classes, libs), ClassLoader.getSystemClassLoader());
    }

    private static URL[] mergeURL(URL[] classes, URL[] libs) {
        List<URL> urls = new ArrayList<>();
        for (URL url : classes) {
            urls.add(url);
        }
        // 获取所有Jar包的路径
        List<URL> jarURLs = Arrays.stream(libs).flatMap(
                url -> {
                    List<URL> jarUrls = new ArrayList<>();
                    try {
                        File f = new File(url.toURI());
                        Path path = Path.of(f.toURI());
                        jarUrls.addAll(
                                Arrays.stream(f.list())
                                        .filter(name -> name.endsWith(".jar"))
                                        .map(n -> path.resolve(n).toFile())
                                        .map(ff -> {
                                            try {
                                                return ff.toURI().toURL();
                                            } catch (MalformedURLException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })
                                        .toList()
                        );
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    return jarUrls.stream();
                }
        ).toList();
        urls.addAll(jarURLs);
        return urls.toArray(new URL[0]);
    }
}
