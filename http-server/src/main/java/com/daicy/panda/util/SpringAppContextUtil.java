package com.daicy.panda.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 获得spring 上下文
 * 
 * @author: daicy
 */
@Slf4j
public class SpringAppContextUtil {

    private static ApplicationContext applicationContextHolder;


    public static void setApplicationContextHolder(ApplicationContext context) {
        applicationContextHolder = context;
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
}
