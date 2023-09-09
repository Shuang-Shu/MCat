package com.mdc.mcat.engine.builder.parse;

import com.mdc.mcat.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/9 11:40
 */
public class WarParser {
    public static record URLSet(URL[] classes, URL[] lib) {
    }

    public static URLSet parse(String[] warFiles) throws IOException {
        for (String wf : warFiles) {
            String warName = wf.split("\\.")[0];
            Path base = Paths.get("tmp/" + warName).normalize().toAbsolutePath();
            // 清空目录
            FileUtils.deleteDir(base);
            // 解压war包
            FileUtils.unzipJar(new File(wf), base);
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
        return new URLSet(classes, lib);
    }
}
