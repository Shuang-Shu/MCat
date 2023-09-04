package com.mdc.mcat.utils;

import com.mdc.mcat.anno.WebInitParam;
import com.mdc.mcat.anno.WebServlet;

import java.util.HashMap;
import java.util.Map;

public class AnnoUtils {
    public static Map<String, String> getInitAttributes(Class<?> clazz) {
        var webServlet = clazz.getAnnotation(WebServlet.class);
        var initAttributes = webServlet.initParams();
        Map<String, String> map = new HashMap<>();
        for (WebInitParam initAttribute : initAttributes) {
            map.put(initAttribute.name(), initAttribute.value());
        }
        return map;
    }
}
