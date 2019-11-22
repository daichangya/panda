package com.daicy.panda.netty.servlet.impl;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Locale;


/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.http
 * @date:19-11-8
 */
public class ServletResponseImpl implements HttpServletResponse {

    private final FullHttpResponse originalResponse;

    private ServletOutputStreamImpl servletOutputStream;

    private ChannelHandlerContext ctx;

    private PrintWriter printWriter;

    private boolean responseCommited = false;

    public ServletResponseImpl(ChannelHandlerContext ctx,FullHttpResponse originalResponse) {
        this.ctx = ctx;
        this.originalResponse = originalResponse;
        this.servletOutputStream = new ServletOutputStreamImpl(originalResponse);
        this.printWriter = new PrintWriter(servletOutputStream);
    }

    @Override
    public void addCookie(Cookie cookie) {
        String result = ServerCookieEncoder.LAX.encode(new DefaultCookie(cookie.getName(), cookie.getValue()));
        this.originalResponse.headers().add(HttpHeaderNames.SET_COOKIE, result);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.originalResponse.headers().contains(name);
    }

    @Override
    public String encodeURL(String url) {
        try {
            String characterEncoding = getCharacterEncoding();
            if (StringUtils.isEmpty(characterEncoding)) {
                return URLEncoder.encode(url);
            }
            return URLEncoder.encode(url, getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding url!", e);
        }
    }

    @Override
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.originalResponse.setStatus(new HttpResponseStatus(sc, msg));
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.originalResponse.setStatus(HttpResponseStatus.valueOf(sc));
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        setStatus(SC_FOUND);
        setHeader(HttpHeaderNames.LOCATION.toString(), location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.originalResponse.headers().set(name,date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.originalResponse.headers().add(name,date);
    }

    @Override
    public void setHeader(String name, String value) {
        this.originalResponse.headers().set(name,value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.originalResponse.headers().add(name,value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.originalResponse.headers().setInt(name,value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.originalResponse.headers().addInt(name,value);
    }

    @Override
    public int getStatus() {
        return this.originalResponse.getStatus().code();
    }


    @Override
    public void setStatus(int sc) {
        this.originalResponse.setStatus(HttpResponseStatus.valueOf(sc));
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.originalResponse.setStatus(new HttpResponseStatus(sc, sm));
    }


    @Override
    public String getHeader(String name) {
        return this.originalResponse.headers().get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        String value = this.getHeader(name);
        return null == value ? null : Lists.newArrayList(value);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.originalResponse.headers().names();
    }

    @Override
    public String getCharacterEncoding() {
        return this.originalResponse.headers().get(HttpHeaderNames.CONTENT_ENCODING);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.originalResponse.headers().set(HttpHeaderNames.CONTENT_ENCODING, charset);
    }


    @Override
    public String getContentType() {
        return this.originalResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public void setContentType(String type) {
        this.originalResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, type);
    }

    @Override
    public void setContentLength(int len) {
        HttpUtil.setContentLength(this.originalResponse, len);
    }

    @Override
    public ServletOutputStreamImpl getOutputStream()  {
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public void setContentLengthLong(long len) {
        HttpUtil.setContentLength(this.originalResponse, len);
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public void flushBuffer() throws IOException {
        this.getWriter().flush();
        boolean isKeepAlive = HttpUtil.isKeepAlive(originalResponse);
        if (isKeepAlive) {
            setContentLength(this.getOutputStream().getBufferSize());
        }
        ctx.channel().writeAndFlush(originalResponse);
        this.responseCommited = true;
    }

    @Override
    public int getBufferSize() {
        return this.servletOutputStream.getBufferSize();
    }

    @Override
    public void resetBuffer() {
        if (isCommitted())
            throw new IllegalStateException("Response already commited!");

        this.servletOutputStream.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return this.responseCommited;
    }

    @Override
    public void reset() {
        if (isCommitted())
            throw new IllegalStateException("Response already commited!");

        this.originalResponse.headers().clear();
        this.resetBuffer();
    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }


    public void close(){
        boolean isKeepAlive = HttpUtil.isKeepAlive(originalResponse);
        this.responseCommited = true;
        if (isKeepAlive) {
            setContentLength(this.getOutputStream().getBufferSize());
        }
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(originalResponse);

        if (!isKeepAlive && channelFuture != null) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
