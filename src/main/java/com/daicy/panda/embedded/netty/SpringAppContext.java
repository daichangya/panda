package com.daicy.panda.embedded.netty;

import com.daicy.panda.util.SpringAppContextUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * 获得spring 上下文
 * 
 * @author: daicy
 */
public class SpringAppContext implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringAppContextUtil.setApplicationContextHolder(applicationContext);
    }
}
