package com.daicy.panda.netty.servlet.impl.filter;

import com.google.common.collect.Lists;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-12
 */
public class FilterChainImpl implements FilterChain {

//    private Servlet servlet;

    private LinkedList<Filter> filters = Lists.newLinkedList();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        Filter filter = filters.poll();
        if (null != filter) {
            filter.doFilter(request, response, this);
        }
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

//    public void setServlet(Servlet servlet) {
//        this.servlet = servlet;
//    }
}
