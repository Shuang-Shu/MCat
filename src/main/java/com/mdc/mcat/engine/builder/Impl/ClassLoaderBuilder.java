package com.mdc.mcat.engine.builder.Impl;

import com.mdc.mcat.engine.builder.parse.WarParser;
import com.mdc.mcat.engine.classloader.WebAppClassLoader;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/9 11:08
 */
public class ClassLoaderBuilder {
    public static ClassLoader build(WarParser.URLSet urlSet) {
        return new WebAppClassLoader(urlSet.classes(), urlSet.lib());
    }
}
