package com.mdc.mcat;

import com.mdc.mcat.engine.connect.HttpConnector;
import com.mdc.mcat.servlet.HelloServlet;
import com.mdc.mcat.servlet.IndexServlet;

import java.io.IOException;
import java.util.List;

public class TestMain {
    public static void main(String[] args) throws IOException {
        HttpConnector httpConnector = new HttpConnector("0.0.0.0", 8080);
        httpConnector.initialize(
                List.of(
                        HelloServlet.class,
                        IndexServlet.class
                )
        );
        httpConnector.start();
    }
}
