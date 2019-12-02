###基础了解
* https://www.cnblogs.com/davenkin/p/async-servlet.html
* https://www.jianshu.com/p/c73b35fa6f6f
* https://blog.csdn.net/zhurhyme/article/details/76228836

####AsyncContextImpl

```
     package com.daicy.panda.netty.servlet;
     
     import com.daicy.panda.netty.servlet.impl.ServletResponseImpl;
     
     import javax.servlet.AsyncEvent;
     import javax.servlet.AsyncListener;
     import java.io.IOException;
     
     /**
      * @author: create by daichangya
      * @version: v1.0
      * @description: com.daicy.panda.netty.servlet
      * @date:19-11-21
      */
     public class AsyncListenerImpl implements AsyncListener {
         @Override
         public void onComplete(AsyncEvent event) throws IOException {
             ServletResponseImpl servletResponse = (ServletResponseImpl) event.getSuppliedResponse();
             servletResponse.close();
         }
     
         @Override
         public void onTimeout(AsyncEvent event) throws IOException {
     
         }
     
         @Override
         public void onError(AsyncEvent event) throws IOException {
     
         }
     
         @Override
         public void onStartAsync(AsyncEvent event) throws IOException {
     
         }
     }

```
