###基础了解
* 注解 https://blog.csdn.net/novelly/article/details/19808593

Controller 同 Spring Controller
RequestMapping 同 Spring RequestMapping

####RequestMappingHandlerMapping类注册及查找

```
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

```

####BeanContainer 类容器

```
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
```

####简单处理逻辑

```
            String uri = request.uri();
            HandlerMethod handlerMethod = RequestMappingHandlerMapping.getInstance().get(uri);
            if(null != handlerMethod){
                Object bean = BeanContainer.getInstance().getBean(handlerMethod.getClazz());
                try {
                  String result = (String) handlerMethod.getMethod().invoke(bean,null);
                  buf.append(result);
                } catch (IllegalAccessException e) {
                    log.error("controller invoke uri:{}",uri,e);
                } catch (InvocationTargetException e) {
                    log.error("controller invoke uri:{}",uri,e);
                }
            }
```