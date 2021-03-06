package com.daicy.panda.netty.servlet.impl;

import com.daicy.panda.netty.servlet.SessionThreadLocal;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.http
 * @date:19-11-8
 */
@Slf4j
public class ServletRequestImpl implements HttpServletRequest {

    private final HttpRequest originalRequest;

    private final ServletInputStreamImpl inputStream;

    private final Map<String, List<String>> parameters = new HashMap<>();

    private final Map<String, Object> attributes = new HashMap<String, Object>();

    private BufferedReader reader;

    private URIParser uriParser;

    private Cookie[] headCookies = null;

    private final ChannelHandlerContext ctx;

    public ServletRequestImpl(ChannelHandlerContext ctx,HttpRequest originalRequest) {
        this.ctx = ctx;
        this.originalRequest = originalRequest;
        if (originalRequest instanceof FullHttpRequest) {
            this.inputStream = new ServletInputStreamImpl((FullHttpRequest) originalRequest);
        } else {
            this.inputStream = new ServletInputStreamImpl(originalRequest);
        }
        this.reader = new BufferedReader(new InputStreamReader(inputStream));

        this.uriParser = new URIParser();
        this.uriParser.parse(originalRequest.getUri());
        parseParameters();
        parseSessionCookiesId();
    }

    private void parseSessionCookiesId() {
        getCookies();
        if (null != headCookies) {
            String sessionCookieName = ServletContextImpl.get().getPandaServerBuilder().getSssionCookieName();
            for (int i = 0; i < headCookies.length; i++) {
                Cookie scookie = headCookies[i];
                if (scookie.getName().equals(sessionCookieName)) {
                    // Override anything requested in the URL
                    if (!this.isRequestedSessionIdFromCookie()) {
                        requestedSessionId =
                                (scookie.getValue().toString());
                        requestedSessionCookie = true;
                        if (log.isDebugEnabled()) {
                            log.debug(" Requested cookie session id is " +
                                    this.getRequestedSessionId());
                        }
                    } else {
                        if (!isRequestedSessionIdValid()) {
                            requestedSessionId =
                                    (scookie.getValue().toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * 解析请求参数
     *
     * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
     * @throws IOException
     */
    private void parseParameters() {
        HttpMethod method = originalRequest.method();

        QueryStringDecoder decoder = new QueryStringDecoder(originalRequest.uri());
        if (HttpMethod.GET == method) {
            // 是GET请求
            parameters.putAll(decoder.parameters());
        } else if (HttpMethod.POST == method) {
            // 是POST请求
            HttpPostRequestDecoder httpPostRequestDecoder = new HttpPostRequestDecoder(originalRequest);
            try {
                List<InterfaceHttpData> parmList = httpPostRequestDecoder.getBodyHttpDatas();
                for (InterfaceHttpData parm : parmList) {
                    Attribute data = (Attribute) parm;
                    try {
                        parseAttribute(data);
                    } catch (Exception e) {
                        log.error("HttpPostRequestDecoder error", e);
                    }
                }
            } finally {
                if (httpPostRequestDecoder != null) {
                    httpPostRequestDecoder.destroy();
                }
            }

        }
    }

    private void parseAttribute(Attribute attribute) throws Exception {
        if (this.parameters.containsKey(attribute.getName())) {
            this.parameters.get(attribute.getName()).add(attribute.getValue());
        } else {
            List<String> values = new ArrayList<>();
            values.add(attribute.getValue());
            this.parameters.put(attribute.getName(), values);
            this.attributes.put(attribute.getValue(), values);
        }
    }


    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
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
        return (int) HttpUtil.getContentLength(this.originalRequest, -1);
    }

    @Override
    public long getContentLengthLong() {
        return getContentLength();
    }

    @Override
    public String getContentType() {
        return this.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }


    @Override
    public String getParameter(String key) {
        List<String> parameterValues = parameters.get(key);
        return (parameterValues == null || parameterValues.isEmpty()) ? null : parameterValues.get(0);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String key) {
        List<String> values = this.parameters.get(key);
        if (values == null || values.isEmpty())
            return null;
        return values.toArray(new String[values.size()]);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = Maps.newHashMap();
        for (Map.Entry<String, List<String>> parameter : parameters.entrySet()) {
            parameterMap.put(parameter.getKey(), parameter.getValue().toArray(new String[0]));
        }
        return parameterMap;
    }

    @Override
    public String getProtocol() {
        return this.originalRequest.protocolVersion().toString();
    }

    @Override
    public String getScheme() {
        return this.isSecure() ? "https" : "http";
    }

    @Override
    public boolean isSecure() {
        return ctx.pipeline().get(SslHandler.class) != null;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return reader;
    }

    @Override
    public void setAttribute(String s, Object o) {
        this.attributes.put(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        this.attributes.remove(s);
    }

    @Override
    public Locale getLocale() {

        String language = originalRequest.headers().get(HttpHeaderNames.ACCEPT_LANGUAGE);

        // handle no locale
        if (StringUtils.isEmpty(language))
            return Locale.getDefault();

        return getLocale(language);

    }

    private Locale getLocale(String language) {
        if (language == null) return null;

        int i = language.indexOf(';');
        if (i >= 0) language = language.substring(0, i).trim();
        String country = "";
        int dash = language.indexOf('-');
        if (dash > -1) {
            country = language.substring(dash + 1).trim();
            language = language.substring(0, dash).trim();
        }
        return new Locale(language, country);
    }


    @Override
    public Enumeration<Locale> getLocales() {

        List<String> acceptable = originalRequest.headers().getAll(HttpHeaderNames.ACCEPT_LANGUAGE);

        // handle no locale
        if (acceptable.isEmpty())
            return Collections.enumeration(Lists.newArrayList(Locale.getDefault()));

        List<Locale> locales = acceptable.stream().map(language ->
        {
            return new Locale(language);
        }).collect(Collectors.toList());

        return Collections.enumeration(locales);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        throw new IllegalStateException(
                "Method 'getRequestDispatcher' not yet implemented!");
    }

    @Override
    public String getRealPath(String s) {
        throw new IllegalStateException(
                "Method 'getRealPath' not yet implemented!");
    }


    @Override
    public String getRemoteAddr() {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel()
                .remoteAddress();
        return addr.getAddress().getHostAddress();
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel()
                .remoteAddress();
        return addr.getHostName();
    }

    @Override
    public int getRemotePort() {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel()
                .remoteAddress();
        return addr.getPort();
    }

    @Override
    public String getLocalAddr() {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel()
                .localAddress();
        return addr.getAddress().getHostAddress();
    }

    @Override
    public String getLocalName() {
        return getServerName();
    }

    @Override
    public int getLocalPort() {
        return getServerPort();
    }


    @Override
    public String getServerName() {
        String host = originalRequest.headers().get(HttpHeaderNames.HOST);
        if (host != null) {
            host = host.trim();
            if (host.startsWith("[")) {
                host = host.substring(1, host.indexOf(']'));
            } else if (host.contains(":")) {
                host = host.substring(0, host.indexOf(':'));
            }
        }
        return host;
    }

    @Override
    public int getServerPort() {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel()
                .localAddress();
        return addr.getPort();
    }


    @Override
    public ServletContext getServletContext() {
        return ServletContextImpl.get();
    }

    private boolean asyncStarted = false;

    private AsyncContextImpl asyncContext;

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        this.asyncStarted = true;
        this.setDispatcherType(DispatcherType.ASYNC);
        this.asyncContext = new AsyncContextImpl(this, null);
        return this.asyncContext;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        this.asyncStarted = true;
        this.setDispatcherType(DispatcherType.ASYNC);
        this.asyncContext = new AsyncContextImpl(servletRequest, servletResponse);
        return this.asyncContext;
    }

    @Override
    public boolean isAsyncStarted() {
        return asyncStarted;
    }

    public void setAsyncStarted(boolean asyncStarted) {
        this.asyncStarted = asyncStarted;
    }

    @Override
    public boolean isAsyncSupported() {
        return true;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    private DispatcherType dispatcherType = DispatcherType.REQUEST;

    public void setDispatcherType(DispatcherType dispatcherType) {
        this.dispatcherType = dispatcherType;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return dispatcherType;
    }


    @Override
    public String getAuthType() {
        throw new IllegalStateException(
                "Method 'getAuthType' not yet implemented!");
    }

    @Override
    public Cookie[] getCookies() {
        if (null != headCookies) {
            return headCookies;
        }
        String cookieString = this.originalRequest.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<io.netty.handler.codec.http.Cookie> cookies = CookieDecoder
                    .decode(cookieString);
            if (!cookies.isEmpty()) {
                headCookies = new Cookie[cookies.size()];
                int indx = 0;
                for (io.netty.handler.codec.http.Cookie c : cookies) {
                    Cookie cookie = new Cookie(c.getName(), c.getValue());
                    cookie.setComment(c.getComment());
                    cookie.setDomain(StringUtils.defaultString(c.getDomain()));
                    cookie.setMaxAge((int) c.getMaxAge());
                    cookie.setPath(c.getPath());
                    cookie.setSecure(c.isSecure());
                    cookie.setVersion(c.getVersion());
                    headCookies[indx] = cookie;
                    indx++;
                }
                return headCookies;
            }
        }
        return null;
    }

    @Override
    public long getDateHeader(String name) {
        return Long.valueOf(originalRequest.headers().get(name, "-1"));
    }

    @Override
    public String getHeader(String name) {
        return originalRequest.headers().get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(originalRequest.headers().getAll(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(originalRequest.headers().names());
    }

    @Override
    public int getIntHeader(String name) {
        return originalRequest.headers().getInt(name);
    }

    @Override
    public String getMethod() {
        return originalRequest.getMethod().name();
    }

    @Override
    public String getPathInfo() {
        return this.uriParser.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        throw new IllegalStateException(
                "Method 'getPathTranslated' not yet implemented!");
    }

    @Override
    public String getContextPath() {
        return ServletContextImpl.get().getContextPath();
    }

    @Override
    public String getRemoteUser() {
       return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    private PrincipalImpl principal;

    @Override
    public Principal getUserPrincipal() {
        if(principal instanceof PrincipalImpl){
            throw new IllegalStateException(
                    "Method 'getUserPrincipal' not yet implemented!");
        }
        return principal;
    }

    @Override
    public String getQueryString() {
        return this.uriParser.getQueryString();
    }

    @Override
    public String getRequestURI() {
        return this.uriParser.getRequestUri();
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = this.getScheme();
        int port = this.getServerPort();
        String urlPath = this.getRequestURI();

        url.append(scheme); // http, https
        url.append("://");
        url.append(this.getServerName());
        if ((scheme.equals("http") && port != 80)
                || (scheme.equals("https") && port != 443)) {
            url.append(':');
            url.append(this.getServerPort());
        }
        url.append(urlPath);
        return url;
    }

    @Override
    public String getServletPath() {
        String servletPath = this.uriParser.getServletPath();
        if (servletPath.equals("/"))
            return "";

        return servletPath;
    }

    private String requestedSessionId;

    private boolean requestedSessionCookie = false;

    @Override
    public String getRequestedSessionId() {
        if (StringUtils.isNotBlank(requestedSessionId)) {
            return requestedSessionId;
        }
        SessionImpl session = SessionThreadLocal.get(requestedSessionId);
        requestedSessionId = session != null ? session.getId() : null;
        return requestedSessionId;
    }

    @Override
    public HttpSession getSession() {
        HttpSession s = SessionThreadLocal.getOrCreate(requestedSessionId);
        return s;
    }

    @Override
    public HttpSession getSession(boolean create) {
        HttpSession session = SessionThreadLocal.get(requestedSessionId);
        if (session == null && create) {
            session = SessionThreadLocal.getOrCreate(requestedSessionId);
        }
        return session;
    }

    @Override
    public String changeSessionId() {
        SessionImpl session = SessionThreadLocal.get(requestedSessionId);
        if (session == null) {
            throw new IllegalStateException("coyoteRequest.changeSessionId");
        }
        session = ServletContextImpl.get().getContext().changeSessionId(session);
        SessionThreadLocal.unset();
        SessionThreadLocal.set(session);
        return session.getId();
    }

    /**
     * @return <code>true</code> if the session identifier included in this
     * request identifies a valid session.
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        if (requestedSessionId == null) {
            return false;
        }
        HttpSession session = SessionThreadLocal.get(requestedSessionId);
        if ((session == null)) {
            return false;
        }
        return true;
    }

    /**
     * @return <code>true</code> if the session identifier included in this
     * request came from a cookie.
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        if (requestedSessionId == null) {
            return false;
        }
        return requestedSessionCookie;
    }


    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        if (response.isCommitted()) {
            throw new IllegalStateException("coyoteRequest.authenticate.ise");
        }
        throw new IllegalStateException(
                "Method 'authenticate' not yet implemented!");
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new IllegalStateException(
                "Method 'login' not yet implemented!");
    }

    @Override
    public void logout() throws ServletException {
        throw new IllegalStateException(
                "Method 'logout' not yet implemented!");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new IllegalStateException(
                "Method 'getParts' not yet implemented!");
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new IllegalStateException(
                "Method 'getPart' not yet implemented!");
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new IllegalStateException(
                "Method 'upgrade' not yet implemented!");
    }
}
