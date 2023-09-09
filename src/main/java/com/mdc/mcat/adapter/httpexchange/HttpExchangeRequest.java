package com.mdc.mcat.adapter.httpexchange;

import jakarta.servlet.http.Cookie;

import java.net.URI;

public interface HttpExchangeRequest {
    String getRequestMethod();

    URI getRequestURI();

    Cookie[] getCookies();
}
