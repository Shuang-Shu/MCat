package com.mdc.mcat.utils;

public class RegUtils {
    public static String formatPatternUri(String uri) {
        if (StringUtils.isEmpty(uri)) {
            throw new IllegalArgumentException("uri can not be null");
        }
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        return "^" + uri + ".*";
    }
}
