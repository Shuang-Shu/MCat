package com.mdc.mcat.adapter.httpexchange;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpExchangeAdapter implements HttpExchangeRequest, HttpExchangeResponse {
    public final static String SET_COOKIE = "Set-Cookie";
    public final static String COOKIE = "Cookie";
    private HttpExchange httpExchange;

    public HttpExchangeAdapter(HttpExchange exchange) {
        this.httpExchange = exchange;
    }

    @Override
    public String getRequestMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public URI getRequestURI() {
        return httpExchange.getRequestURI();
    }

    @Override
    public Cookie[] getCookies() {
        List<String> cookies = httpExchange.getRequestHeaders().get(COOKIE);
        if (cookies == null) {
            cookies = new ArrayList<>();
        }
        return cookies.stream().flatMap(
                cs -> {
                    return Arrays.stream(cs.split("; ")).map(
                            cookieStr -> {
                                String[] kv = cookieStr.split("=");
                                return new Cookie(kv[0], kv[1]);
                            }
                    );
                }
        ).toList().toArray(new Cookie[0]);
    }

    @Override
    public InputStream getRequestBody() {
        return httpExchange.getRequestBody();
    }

    @Override
    public Headers getResponseHeaders() {
        return httpExchange.getResponseHeaders();
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        httpExchange.sendResponseHeaders(rCode, responseLength);
    }

    @Override
    public void addCookie(Cookie cookie) {
        httpExchange.getResponseHeaders().add(
                SET_COOKIE,
                cookie.getName() + "=" + cookie.getValue()
        );
    }

    @Override
    public OutputStream getResponseBody() {
        return httpExchange.getResponseBody();
    }
}
