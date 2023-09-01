package com.mdc.mcat;

import com.mdc.mcat.entity.request.HttpServletRequestImpl;
import com.mdc.mcat.handler.HttpConnector;
import com.mdc.mcat.handler.SimpleHandler;

public class Main {
    public static void main(String[] args) {
        var connector = new HttpServletRequestImpl(null);
        System.out.println(connector);
    }
}