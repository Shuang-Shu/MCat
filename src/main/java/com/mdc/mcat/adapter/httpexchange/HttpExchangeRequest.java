package com.mdc.mcat.adapter.httpexchange;

import jakarta.servlet.http.Cookie;

import java.io.InputStream;
import java.net.URI;

public interface HttpExchangeRequest {
    String getRequestMethod();

    URI getRequestURI();

    Cookie[] getCookies();

    InputStream getRequestBody();
}
