package com.mdc.mcat.utils;

import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AnnoUtils {
    private final static Logger logger = LoggerFactory.getLogger(AnnoUtils.class);

    public static Map<String, String> getInitParams(Annotation anno) {
        Map<String, String> map = new HashMap<>();
        try {
            Method method = anno.getClass().getMethod("initParams");
            var initAttributes = (WebInitParam[]) method.invoke(anno);
            for (WebInitParam initAttribute : initAttributes) {
                map.put(initAttribute.name(), initAttribute.value());
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            logger.error(e.getMessage());
        }
        return map;
    }
}
