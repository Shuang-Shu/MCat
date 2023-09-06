package com.mdc.mcat.adapter;

import jakarta.servlet.http.Cookie;

import java.net.URI;

public interface HttpExchangeRequest {
    String getRequestMethod();

    URI getRequestURI();

    Cookie[] getCookies();
}
