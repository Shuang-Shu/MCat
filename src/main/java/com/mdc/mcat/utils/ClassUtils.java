package com.mdc.mcat.utils;

public class ClassUtils {
    public static String getSimpleClassName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        var chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
