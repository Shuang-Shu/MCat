package com.mdc.mcat.adapter;

import com.sun.net.httpserver.Headers;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpExchangeResponse {
    Headers getResponseHeaders();

    void sendResponseHeaders(int rCode, long responseLength) throws IOException;

    void addCookie(Cookie cookie);

    OutputStream getResponseBody();
}
