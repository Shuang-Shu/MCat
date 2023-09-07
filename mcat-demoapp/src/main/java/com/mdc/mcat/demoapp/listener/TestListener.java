package com.mdc.mcat.demoapp.listener;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener(
    value = "/"
)
public class TestListener implements ServletContextListener { 

}
