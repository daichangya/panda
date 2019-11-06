package com.daicy.panda.method;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.method
 * @date:19-11-6
 */
@Slf4j
public class BeanContainer {

    private static final BeanContainer _instance = new BeanContainer();


    private final Map<String, Object> beanLookup = new LinkedHashMap<String, Object>();

    public static BeanContainer getInstance() {
        return _instance;
    }

    public Object getBean(Class clazz) {
        Object bean = beanLookup.get(clazz);
        if (null == bean) {
            try {
                bean = ConstructorUtils.invokeConstructor(clazz, null);
            } catch (NoSuchMethodException e) {
                log.error("getBean error :{}", clazz, e);
            } catch (IllegalAccessException e) {
                log.error("getBean error :{}", clazz, e);
            } catch (InvocationTargetException e) {
                log.error("getBean error :{}", clazz, e);
            } catch (InstantiationException e) {
                log.error("getBean error :{}", clazz, e);
            }
        }
        return bean;
    }

}
