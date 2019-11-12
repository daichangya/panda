package com.daicy.panda.embedded.netty;

import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.embedded.netty
 * @date:19-11-12
 */
public class NettyServletWebServerFactory extends AbstractServletWebServerFactory {
    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        return new NettyWebServer(initializers);
    }
}
