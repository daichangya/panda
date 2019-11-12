package com.daicy.panda.controller;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.controller
 * @date:19-11-12
 */
@WebFilter(filterName = "myFilter", urlPatterns = "/*")
public class HttpFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        System.out.println("-- In MyFilter --");
        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println("URI: " + req.getRequestURI());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}