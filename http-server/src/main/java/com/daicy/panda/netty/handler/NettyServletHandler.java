package com.daicy.panda.netty.handler;

import com.daicy.panda.netty.servlet.impl.*;
import com.daicy.panda.netty.servlet.impl.filter.FilterChainFactory;
import com.daicy.panda.netty.servlet.impl.filter.FilterChainImpl;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.handler
 * @date:19-11-22
 */
@Slf4j
public class NettyServletHandler {


    public static void handleRequest(ServletRequest servletRequest, ServletResponse servletResponse) {
        ServletRequestImpl servletRequestImpl = (ServletRequestImpl) servletRequest;
        ServletResponseImpl servletResponseImpl = (ServletResponseImpl) servletResponse;
        try {
            handleRequest0(servletRequestImpl, servletResponseImpl);
        } finally {

        }
//        TracingThreadPoolExecutor asyncExecutor = ServletContextImpl.get().getPandaServerBuilder().executor();
//        if (asyncExecutor == null) {
//            handleRequest0(servletRequest, servletResponse);
//            return;
//        }
//
//        asyncExecutor.execute(() -> {
//            handleRequest0(servletRequest, servletResponse);
//        });
    }

    public static void handleRequest0(ServletRequestImpl servletRequestImpl, ServletResponseImpl servletResponseImpl) {
        try {
            RequestDispatcherImpl dispatcher = (RequestDispatcherImpl) ServletContextImpl.get().getRequestDispatcher(servletRequestImpl.getRequestURI());
            if (dispatcher == null) {
                servletResponseImpl.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            dispatcher.dispatch(servletRequestImpl, servletResponseImpl);
//            Servlet servlet = ServletContextImpl.get().getServlet(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
//            FilterChainImpl chain = FilterChainFactory.createFilterChain(servletRequestImpl, servlet);
//            chain.doFilter(servletRequest, servletResponse);
//            if (servletResponseImpl.getStatus() == HttpServletResponse.SC_OK) {
//                servlet.service(servletRequest, servletResponse);
//            }
        } catch (Exception e) {
            log.error("controller invoke uri:{}", servletRequestImpl.getRequestURI(), e);
        }
    }
}
