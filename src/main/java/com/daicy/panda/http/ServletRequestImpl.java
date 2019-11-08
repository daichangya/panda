package com.daicy.panda.http;

import com.google.common.collect.Maps;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.http
 * @date:19-11-8
 */
@Slf4j
public class ServletRequestImpl implements ServletRequest {

    private final FullHttpRequest fullHttpRequest;

    private Map<String, List<String>> parameters = new HashMap<>();

    private String path;


    public ServletRequestImpl(FullHttpRequest fullHttpRequest) {
        this.fullHttpRequest = fullHttpRequest;
        parseParameters();
    }

    /**
     * 解析请求参数
     *
     * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
     * @throws IOException
     */
    private void parseParameters() {
        HttpMethod method = fullHttpRequest.method();

        QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
        path = decoder.path();
        if (HttpMethod.GET == method) {
            // 是GET请求
            parameters.putAll(decoder.parameters());
        } else if (HttpMethod.POST == method) {
            // 是POST请求
            HttpPostRequestDecoder httpPostRequestDecoder = new HttpPostRequestDecoder(fullHttpRequest);
            httpPostRequestDecoder.offer(fullHttpRequest);

            List<InterfaceHttpData> parmList = httpPostRequestDecoder.getBodyHttpDatas();
            for (InterfaceHttpData parm : parmList) {
                Attribute data = (Attribute) parm;
                try {
                    parseAttribute(data);
                } catch (Exception e) {
                    log.error("HttpPostRequestDecoder error", e);
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
        }
    }

    @Override
    public Object getAttribute(String s) {
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
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

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
        return null;
    }

    @Override
    public String getParameter(String key) {
        List<String> parameterValues = parameters.get(key);
        return (parameterValues == null || parameterValues.isEmpty()) ? null : parameterValues.get(0);    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String key) {
        return this.parameters.get(key).toArray(new String[] {});
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = Maps.newHashMap();
        for (Map.Entry<String, List<String>> parameter :parameters.entrySet()){
            parameterMap.put(parameter.getKey(),parameter.getValue().toArray(new String[0]));
        }
        return parameterMap;
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
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
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
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

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
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
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
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
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

    public String getPath() {
        return path;
    }
}
