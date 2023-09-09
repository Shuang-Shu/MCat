package com.mdc.mcat;

import com.mdc.mcat.utils.YamlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/8 20:07
 */
public class TestYamlParser {
    public static void main(String[] args) throws URISyntaxException, FileNotFoundException {
        var url = TestYamlParser.class.getClassLoader().getResource("web.yaml");
        InputStream is = new FileInputStream(new File(url.toURI()));
        var yamlMap = YamlUtils.parseYaml(is);
        System.out.println(yamlMap);
    }
}
