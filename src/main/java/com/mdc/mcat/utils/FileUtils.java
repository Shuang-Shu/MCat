package com.mdc.mcat.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileUtils {
    public static void deleteDir(Path baseDir) throws IOException {
        if (Files.exists(baseDir)) {
            Files.walk(baseDir)
                    .map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(File::delete);
        }
    }

    public static void unzipJar(File file, Path baseDir) throws IOException {
        JarFile jarFile = new JarFile(file);
        List<JarEntry> jarEntries = jarFile.stream().toList();
        for (JarEntry jarEntry : jarEntries) {
            if (!jarEntry.isDirectory()) {
                System.out.println(jarEntry.getName());
                var is = jarFile.getInputStream(jarEntry);
                var dest = baseDir.resolve(jarEntry.getName());
                Files.createDirectories(dest.getParent());
                Files.copy(is, dest);
            }
        }
    }

    public static File loadResource(String path) {
        return new File(FileUtils.class.getClassLoader().getResource(path).getFile());
    }
}
