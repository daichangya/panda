package com.daicy.panda.method;

import java.lang.reflect.Method;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.method
 * @date:19-11-6
 */
public class HandlerMethod {
    private final Class<?> clazz;
    private final Method method;

    public HandlerMethod(Class<?> clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Method getMethod() {
        return method;
    }
}
