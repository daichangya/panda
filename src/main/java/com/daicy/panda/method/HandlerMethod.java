package com.daicy.panda.method;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;

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
    private final MethodParameter[] parameters;

    private static final DefaultParameterNameDiscoverer defaultParameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public HandlerMethod(Class<?> clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
        this.parameters = initMethodParameters();
    }

    private MethodParameter[] initMethodParameters() {
        int count = this.method.getParameterCount();
        MethodParameter[] result = new MethodParameter[count];
        for (int i = 0; i < count; i++) {
            MethodParameter parameter = new MethodParameter(method, i);
            GenericTypeResolver.resolveParameterType(parameter, this.clazz);
            parameter.initParameterNameDiscovery(defaultParameterNameDiscoverer);
            result[i] = parameter;
        }
        return result;
    }


    public Class<?> getClazz() {
        return clazz;
    }

    public Method getMethod() {
        return method;
    }

    public MethodParameter[] getParameters() {
        return parameters;
    }
}
