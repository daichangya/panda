package com.daicy.panda.netty.embedded;

import com.daicy.panda.netty.HttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletException;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.embedded.netty
 * @date:19-11-12
 */
public class NettyWebServer implements WebServer {

    private static final Log logger = LogFactory.getLog(NettyWebServer.class);

    private HttpServer httpServer;

    private ServletContextInitializer[] initializers;

    public NettyWebServer(ServletContextInitializer... initializers) {
        httpServer = new HttpServer();
        this.initializers = initializers;
        if(null !=initializers){
            for (ServletContextInitializer servletContextInitializer:initializers){
                try {
                    servletContextInitializer.onStartup(httpServer.getServletContext());
                } catch (ServletException e) {
                    throw new WebServerException("Unable to start Netty", e);
                }
            }
        }
    }

    @Override
    public void start() throws WebServerException {
        this.httpServer.start();
        logger.info("Netty started on port(s): " + getPort());
    }

    @Override
    public void stop() throws WebServerException {
        if (this.httpServer != null) {
            httpServer.getChannel().close();
            this.httpServer = null;
        }
    }

    @Override
    public int getPort() {
        return httpServer.address().getPort();
    }



}
