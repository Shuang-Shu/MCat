package com.mdc.mcat;

import com.mdc.mcat.engine.entity.request.HttpServletRequestImpl;

public class Main {
    public static void main(String[] args) {
        var connector = new HttpServletRequestImpl(null);
        System.out.println(connector);
    }
}