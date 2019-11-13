package com.daicy.panda.netty.embedded;

import com.daicy.panda.netty.PandaServerBuilder;
import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
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
        PandaServerBuilder pandaServerBuilder = PandaServerBuilder.forPort(getPort())
                .inetAddress(getAddress()).contextPath(getContextPath());
        ServletContextImpl.get().setContextPath(pandaServerBuilder.getContextPath());
        return new NettyWebServer(pandaServerBuilder, initializers);
    }
}
