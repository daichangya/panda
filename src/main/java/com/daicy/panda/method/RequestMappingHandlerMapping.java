package com.daicy.panda.method;

import com.daicy.panda.annotation.Controller;
import com.daicy.panda.annotation.RequestMapping;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.method
 * @date:19-11-6
 */
public class RequestMappingHandlerMapping {

    private static final RequestMappingHandlerMapping _instance = new RequestMappingHandlerMapping();


    private final Map<String, HandlerMethod> urlLookup = new LinkedHashMap<String, HandlerMethod>();


    public static RequestMappingHandlerMapping getInstance() {
        return _instance;
    }

    public void register(Class<?> type) {
        Controller controller = type.getAnnotation(Controller.class);
        if (controller == null) {
            return;
        }
        Method[] controllerMethods = type.getDeclaredMethods();
        for (Method method : controllerMethods) {
            if (Modifier.isStatic(method.getModifiers()) || method.getAnnotations().length == 0
                    || !Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            register(type, method);
        }
    }

    public void register(Class<?> type, Method method) {
        RequestMapping classLevelPath = type.getAnnotation(RequestMapping.class);
        String[] controllerPaths = classLevelPath == null ? new String[]{StringUtils.EMPTY} : classLevelPath.value();
        RequestMapping methodLevelPath = method.getAnnotation(RequestMapping.class);
        String[] methodPaths = methodLevelPath == null ? new String[]{StringUtils.EMPTY} : methodLevelPath.value();
        for (String controllerPath : controllerPaths) {
            for (String methodPath : methodPaths) {
                String url = String.format("%s%s", controllerPath, methodPath);
                if(url.endsWith("/") && url.length()>1){
                    url = url.substring(0,url.length()-1);
                }
                urlLookup.put(url, new HandlerMethod(type, method));
            }
        }
    }

    public HandlerMethod get(String url) {
        HandlerMethod handlerMethod = urlLookup.get(url);
        return handlerMethod;
    }

}
