package com.daicy.panda.netty.servlet.impl;

import com.daicy.panda.netty.TracingThreadPoolExecutor;
import com.daicy.panda.netty.handler.NettyServletHandler;
import com.daicy.panda.netty.servlet.AsyncListenerImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncContextImpl implements AsyncContext {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final List<AsyncListener> listeners = new ArrayList<AsyncListener>();

    private String dispatchedPath;

    private long timeout = 10 * 1000L;    // 10 seconds is Tomcat's default

    private final TracingThreadPoolExecutor asyncExecutor;


    public AsyncContextImpl(ServletRequest request, ServletResponse response) {
        this.asyncExecutor = ServletContextImpl.get().getPandaServerBuilder().executor();

        this.request = (HttpServletRequest) request;
        this.response = (HttpServletResponse) response;
    }

    @Override
    public ServletRequest getRequest() {
        return this.request;
    }

    @Override
    public ServletResponse getResponse() {
        return this.response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return (this.request instanceof ServletRequestImpl) && (this.response instanceof ServletResponseImpl);
    }

    @Override
    public void dispatch() {
        dispatch(this.request.getRequestURI());
    }

    @Override
    public void dispatch(String path) {
        dispatch(null, path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        this.dispatchedPath = path;
//        addListener(new AsyncListenerImpl());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                NettyServletHandler.handleRequest(request, response);
                complete();
            }
        };
        start(runnable);
    }

    public String getDispatchedPath() {
        return this.dispatchedPath;
    }

    @Override
    public void complete() {
        ServletRequestImpl fullRequest = WebUtils.getNativeRequest(request, ServletRequestImpl.class);
        if (fullRequest != null) {
            fullRequest.setAsyncStarted(false);
        }
        for (AsyncListener listener : this.listeners) {
            try {
                listener.onComplete(new AsyncEvent(this, this.request, this.response));
            } catch (IOException ex) {
                throw new IllegalStateException("AsyncListener failure", ex);
            }
        }
    }

    @Override
    public void start(Runnable runnable) {
        Future futureTask = asyncExecutor.submit(runnable);
        try {
            futureTask.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            futureTask.cancel(true);
        } catch (ExecutionException e) {
            futureTask.cancel(true);
        } catch (TimeoutException e) {
            futureTask.cancel(true);
        }
    }

    @Override
    public void addListener(AsyncListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
        this.listeners.add(listener);
    }

    public List<AsyncListener> getListeners() {
        return this.listeners;
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        return BeanUtils.instantiateClass(clazz);
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

}
