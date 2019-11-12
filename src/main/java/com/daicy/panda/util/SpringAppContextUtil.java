package com.daicy.panda.util;

import com.daicy.panda.netty.servlet.impl.ServletConfigImpl;
import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;
import java.util.Map;

/**
 * 获得spring 上下文
 * 
 * @author: daicy
 */
@Slf4j
public class SpringAppContextUtil {

    private static XmlWebApplicationContext applicationContextHolder;

    private static DispatcherServlet dispatcherServlet;

    public static void setApplicationContextHolder(XmlWebApplicationContext context) {
        applicationContextHolder = context;
        dispatcherServlet = new DispatcherServlet(context);
        try {
            dispatcherServlet.init(context.getServletConfig());
        } catch (ServletException e) {
            log.error("dispatcherServlet init",e);
        }
    }

    public static <T> T getBean(Class<T> t) {
        return applicationContextHolder.getBean(t);

    }

    public static <T> T getBean(Class<T> clazz, String beanName) {
        return applicationContextHolder.getBean(beanName, clazz);
    }

    public static <T> Map<String, T> getBeanOfType(Class<T> clazz) {
        return applicationContextHolder.getBeansOfType(clazz);
    }

    public static <T> T getBean(String beanName) {
        return (T) applicationContextHolder.getBean(beanName);
    }

    public static <T> T getInstance(Class<T> tClass,T handle) {
        if(null==handle){
            handle=getBean(tClass);
        }
        return handle;
    }

    public static DispatcherServlet getDispatcherServlet() {
        return dispatcherServlet;
    }
}
