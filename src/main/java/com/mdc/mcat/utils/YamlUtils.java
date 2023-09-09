package com.mdc.mcat.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/8 20:00
 */
public class YamlUtils {
    public static Map<String, Object> parseYaml(InputStream is) {
        return new Yaml().load(is);
    }

    public static Map<String, Object> parseYaml(File f) throws FileNotFoundException {
        return parseYaml(new FileInputStream(f));
    }
}
