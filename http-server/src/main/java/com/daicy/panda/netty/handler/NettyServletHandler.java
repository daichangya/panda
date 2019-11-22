package com.daicy.panda.netty.handler;

import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import com.daicy.panda.netty.servlet.impl.ServletRequestImpl;
import com.daicy.panda.netty.servlet.impl.ServletResponseImpl;
import com.daicy.panda.netty.servlet.impl.filter.FilterChainFactory;
import com.daicy.panda.netty.servlet.impl.filter.FilterChainImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

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
            Servlet servlet = ServletContextImpl.get().getServlet(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
            FilterChainImpl chain = FilterChainFactory.createFilterChain(servletRequestImpl, servlet);
            chain.doFilter(servletRequest, servletResponse);
            if (servletResponseImpl.getStatus() == HttpServletResponse.SC_OK) {
                servlet.service(servletRequest, servletResponse);
            }
        } catch (Exception e) {
            log.error("controller invoke uri:{}", servletRequestImpl.getRequestURI(), e);
        } finally {
            if (!servletRequest.isAsyncStarted()) {
                servletResponseImpl.close();
            }
        }
    }


}
