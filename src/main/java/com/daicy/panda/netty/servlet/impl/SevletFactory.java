package com.daicy.panda.netty.servlet.impl;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-12
 */
public class SevletFactory {

    public synchronized <T extends Servlet> T loadServlet(Class<?> clazz) throws ServletException {
        Servlet servlet = ServletContextImpl.get().getServlet(clazz.getName());
        if(null != servlet){
            return (T) servlet;
        }
        try {
            servlet = ConstructorUtils.invokeConstructor((Class<T>) clazz, null);
            // create and configure beans
            ServletConfigImpl servletConfig = new ServletConfigImpl(clazz.getName());
            servletConfig.setServletContext(ServletContextImpl.get());
            servlet.init(servletConfig);
        } catch (InvocationTargetException e) {
            throw new ServletException(e);
        } catch (ReflectiveOperationException e) {
            throw new ServletException(e);
        }
        return (T) servlet;
    }
}
