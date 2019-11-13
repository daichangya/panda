###基础了解
* Spring https://docs.spring.io/spring/docs/5.2.1.RELEASE/spring-framework-reference/core.html#spring-core
数据获取
数据类型转换
方法赋值

####request数据获取

```
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        if (!params.isEmpty()) {
            for (Entry<String, List<String>> p: params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                }
            }
            buf.append("\r\n");
        }
```

####数据类型转换

```
args[i] = new DataBinder(null, parameters[i].getParameterName())
                    .convertIfNecessary(requestArg, parameters[i].getParameterType(), parameters[i]);
```

####Controller赋值调用

```
  Object bean = SpringAppContextUtil.getBean(handlerMethod.getClazz());
                    Object result = handlerMethod.getMethod().invoke(bean,args);
```