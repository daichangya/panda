package com.daicy.panda.netty.embedded;

import com.daicy.panda.netty.HttpServer;
import com.daicy.panda.netty.PandaServerBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletException;
import java.util.concurrent.CompletionStage;

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

    public NettyWebServer(PandaServerBuilder pandaServerBuilder, ServletContextInitializer... initializers) {
        httpServer = pandaServerBuilder.build();
        this.initializers = initializers;
        if (null != initializers) {
            for (ServletContextInitializer servletContextInitializer : initializers) {
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
        httpServer.start()
                .thenAccept(ws -> {
                    System.out.println(
                            "Netty server is up! http://localhost:" + ws.getPort());
                    ws.whenShutdown().thenRun(()
                            -> System.out.println("Netty server is DOWN. Good bye!"));
                })
                .exceptionally(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                    return null;
                });
    }


    @Override
    public void stop() throws WebServerException {
        if (this.httpServer != null) {
            httpServer.shutdown();
        }
    }

    @Override
    public int getPort() {
        return httpServer.getPort();
    }


}
