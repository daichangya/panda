package com.daicy.panda.netty.servlet.impl;

import com.daicy.panda.netty.servlet.impl.filter.FilterChainFactory;
import com.daicy.panda.netty.servlet.impl.filter.FilterChainImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-19
 */
public class RequestDispatcherImpl implements RequestDispatcher {

    /**
     * The servlet name for a named dispatcher.
     */
    private String name = null;

    /**
     * The servlet path for this RequestDispatcher.
     */
    private String servletPath = null;


    private HttpServlet httpServlet;


    public RequestDispatcherImpl(String servletName, String servletPath, HttpServlet servlet) {
        this.name = servletName;
        this.servletPath = servletPath;
        this.httpServlet = servlet;
    }


    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        if (httpServlet != null) {
            //TODO Wrap
            httpServlet.service(servletRequest, servletResponse);
        } else {
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        if (httpServlet != null) {
            //TODO Wrap
            httpServlet.service(servletRequest, servletResponse);
        } else {
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void dispatch(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        ServletRequestImpl servletRequestImpl = (ServletRequestImpl) servletRequest;
        ServletResponseImpl servletResponseImpl = (ServletResponseImpl) servletResponse;
        FilterChainImpl chain = FilterChainFactory.createFilterChain(servletRequestImpl, httpServlet);
        chain.doFilter(servletRequest, servletResponse);
        if (servletResponseImpl.getStatus() == HttpServletResponse.SC_OK) {
            httpServlet.service(servletRequest, servletResponse);
        }
    }
}
