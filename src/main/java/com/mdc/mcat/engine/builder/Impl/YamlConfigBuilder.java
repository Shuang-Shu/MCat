package com.mdc.mcat.engine.builder.Impl;

import com.mdc.mcat.engine.config.Configuration;
import com.mdc.mcat.utils.YamlUtils;
import jakarta.servlet.ServletException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/8 9:46
 */
public class YamlConfigBuilder {
    public static Configuration build(InputStream is) throws ServletException, ClassNotFoundException {
        Configuration diffConfig = new Configuration();
        var yamlConfig = (Map<String, Object>) YamlUtils.parseYaml(is).get("web-app");
        if (yamlConfig == null) {
            throw new ServletException("web-app should be the root element of web.yaml");
        }
        // parse all context parameters
        var contextParams = (List<Map<String, String>>) yamlConfig.get("context-param");
        for (var param : contextParams) {
            diffConfig.getContextParams().put(param.get("param-name"), param.get("param-value"));
        }
        List<String> yamlClassNames = new ArrayList<>();
        List<Map<String, String>> servlets = (List<Map<String, String>>) yamlConfig.get("servlets");
        if (servlets != null) {
            yamlClassNames.addAll(servlets
                    .stream().map(e -> e.get("servlet-class")).toList());
        }
        List<Map<String, String>> listeners = (List<Map<String, String>>) yamlConfig.get("listeners");
        if (listeners != null) {
            yamlClassNames.addAll(listeners.
                    stream().map(e -> e.get("listener-class")).toList());
        }
        List<Map<String, String>> filters = (List<Map<String, String>>) yamlConfig.get("filters");
        if (filters != null) {
            yamlClassNames.addAll(filters.
                    stream().map(e -> e.get("filter-class")).toList());
        }
        List<Class<?>> classes = new ArrayList<>();
        for (String name : yamlClassNames) {
            classes.add(Class.forName(name, false, Thread.currentThread().getContextClassLoader()));
        }
        diffConfig.assembleWith(classes);
        return diffConfig;
    }
}
