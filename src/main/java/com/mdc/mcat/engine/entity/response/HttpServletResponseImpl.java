package com.mdc.mcat.engine.entity.response;

import com.mdc.mcat.adapter.httpexchange.HttpExchangeResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class HttpServletResponseImpl implements HttpServletResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpServletResponseImpl.class);
    private final HttpExchangeResponse exchangeResponse;
    private boolean hasSendHeader = false;

    public HttpServletResponseImpl(HttpExchangeResponse resp) {
        this.exchangeResponse = resp;
    }

    public void close() throws IOException {
        this.exchangeResponse.getResponseBody().close();
    }

    @Override
    public void addCookie(Cookie cookie) {
        exchangeResponse.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        exchangeResponse.sendResponseHeaders(sc, 0);
        PrintWriter pw = new PrintWriter(exchangeResponse.getResponseBody(), true, StandardCharsets.UTF_8);
        pw.write("<h1>" + sc + " " + msg + "</h1>");
        pw.close();
    }

    @Override
    public void sendError(int sc) throws IOException {
        exchangeResponse.sendResponseHeaders(sc, 0);
    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.exchangeResponse.getResponseHeaders().keySet();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        try {
            exchangeResponse.sendResponseHeaders(200, 0);
            hasSendHeader = true;
        } catch (IOException e) {
        }
        var outputStream = exchangeResponse.getResponseBody();
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return outputStream != null;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() {
        try {
            exchangeResponse.sendResponseHeaders(200, 0);
            hasSendHeader = true;
        } catch (IOException e) {
            return null;
        }
        return new PrintWriter(exchangeResponse.getResponseBody(), true, StandardCharsets.UTF_8);
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {
        exchangeResponse.getResponseHeaders().set(
                "Content-Type",
                type
        );
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {
        this.exchangeResponse.getResponseBody().flush();
    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    public void cleanup(int rCode) throws IOException {
        if (!hasSendHeader) {
            try {
                exchangeResponse.sendResponseHeaders(rCode, 0);
            } catch (IOException e) {
            }
        }
        exchangeResponse.getResponseBody().close();
    }
}
