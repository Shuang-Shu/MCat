package com.mdc.mcat.engine.entity.request;

import com.mdc.mcat.adapter.httpexchange.HttpExchangeRequest;
import com.mdc.mcat.engine.context.ServletContextImpl;
import com.mdc.mcat.engine.session.impl.HttpSessionImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.security.Principal;
import java.util.*;

public class HttpServletRequestImpl implements HttpServletRequest {
    public final static String JSESSIONID_NAME = "JSESSIONID";

    private HttpExchangeRequest exchangeRequest;

    private Map<String, String> paramMap;
    private Map<String, Object> attributeMap = new HashMap<>();

    private ServletContextImpl servletContext;
    private HttpServletResponse httpServletResponse;

    public HttpServletRequestImpl(HttpExchangeRequest req, HttpServletResponse resp, ServletContextImpl servletContext) {
        this.exchangeRequest = req;
        this.servletContext = servletContext;
        this.httpServletResponse = resp;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return exchangeRequest.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return exchangeRequest.getRequestMethod();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return exchangeRequest.getRequestURI().toString();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(exchangeRequest.getRequestURI().toString());
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        var cookies = getCookies();
        HttpSession session = null;
        for (var cookie : cookies) {
            if (JSESSIONID_NAME.equals(cookie.getName())) {
                var sessionId = cookie.getValue();
                session = servletContext.getSessionManager().getSession(sessionId);
            }
        }
        if (session != null) {
            // 更新最新访问时间
            ((HttpSessionImpl) session).setLastAccessedTime(System.currentTimeMillis());
            httpServletResponse.addCookie(new Cookie(JSESSIONID_NAME, session.getId()));
            return session;
        } else {
            if (create) {
                var sessionId = servletContext.getSessionManager().createSession();
                session = servletContext.getSessionManager().getSession(sessionId);
                this.servletContext.getListenerWrapper().invokeSessionCreated(session);
                httpServletResponse.addCookie(new Cookie(JSESSIONID_NAME, sessionId));
                return session;
            } else {
                return null;
            }
        }
    }

    @Override
    public HttpSession getSession() {
        return getSession(false);
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        var inputStream = exchangeRequest.getRequestBody();
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return inputStream != null;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
        };
    }

    @Override
    public String getParameter(String name) {
        if (paramMap != null) {
            return paramMap.get(name);
        } else {
            parseUri(exchangeRequest.getRequestURI().toString());
        }
        return paramMap.get(name);
    }

    private void parseUri(String uriString) {
        this.paramMap = new HashMap<>();
        String paramString = uriString.substring(uriString.lastIndexOf("?") + 1);
        String[] params = paramString.split("&");
        Arrays.stream(params).forEach(
                p -> {
                    String[] kv = p.split("=");
                    if (kv.length == 2) {
                        paramMap.put(kv[0], kv[1]);
                    }
                }
        );
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (paramMap == null) {
            parseUri(exchangeRequest.getRequestURI().toString());
        }
        return Collections.enumeration(paramMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return paramMap.values().toArray(new String[0]);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> arrayValueMap = new HashMap<>();
        for (var key : paramMap.keySet()) {
            arrayValueMap.put(key, new String[]{paramMap.get(key)});
        }
        return arrayValueMap;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return servletContext.getConfiguration().getPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(exchangeRequest.getRequestBody(), "UTF-8"));
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {
        if (attributeMap.put(name, o) == null) {
            this.servletContext.getListenerWrapper().invokeAttributeAdded(new ServletRequestAttributeEvent(this.servletContext, this, name, o));
        } else {
            this.servletContext.getListenerWrapper().invokeAttributeReplaced(new ServletRequestAttributeEvent(this.servletContext, this, name, o));
        }
    }

    @Override
    public void removeAttribute(String name) {
        this.servletContext.getListenerWrapper().invokeAttributeRemoved(new ServletRequestAttributeEvent(this.servletContext, this, name, attributeMap.get(name)));
        attributeMap.remove(name);
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return this.servletContext.getConfiguration().getHost();
    }

    @Override
    public int getLocalPort() {
        return this.servletContext.getConfiguration().getPort();
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public String getRequestId() {
        return null;
    }

    @Override
    public String getProtocolRequestId() {
        return null;
    }

    @Override
    public ServletConnection getServletConnection() {
        return null;
    }
}
