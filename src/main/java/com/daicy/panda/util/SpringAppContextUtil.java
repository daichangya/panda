package com.daicy.panda.util;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Map;

/**
 * 获得spring 上下文
 * 
 * @author: daicy
 */
public class SpringAppContextUtil {

    private static ApplicationContext applicationContextHolder;

    private static DispatcherServlet dispatcherServlet;

    public static void setApplicationContextHolder(ApplicationContext context) {
        applicationContextHolder = context;
        dispatcherServlet = new DispatcherServlet();
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
