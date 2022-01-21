/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.daicy.panda.netty;

import com.daicy.panda.netty.handler.HttpServerInitializer;
import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
@Slf4j
public final class HttpServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));

    private final PandaServerBuilder builder;

    private Channel channel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final CompletableFuture<HttpServer> startFuture = new CompletableFuture<>();
    private final CompletableFuture<HttpServer> shutdownFuture = new CompletableFuture<>();
    private final CompletableFuture<HttpServer> channelsUpFuture = new CompletableFuture<>();
    private final CompletableFuture<HttpServer> channelsCloseFuture = new CompletableFuture<>();


    public static void main(String[] args) throws Exception {
        HttpServer httpServer = PandaServerBuilder.forPort(PORT).build();
//        httpServer.createConfig();
        httpServer.start()
                .thenAccept(ws -> {
                    System.out.println(
                            "WEB server is up! http://localhost:" + ws.getPort());
                    ws.whenShutdown().thenRun(()
                            -> System.out.println("WEB server is DOWN. Good bye!"));
                })
                .exceptionally(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                    return null;
                });

    }

    public HttpServer(PandaServerBuilder builder) {
        this.builder = builder;
        int port = (builder.getPort() >= 0) ? builder.getPort() : PORT;
        builder.port(port);
    }


//    private void createConfig() {
//
//        XmlWebApplicationContext context = new XmlWebApplicationContext();
//        context.setConfigLocation("classpath:/services.xml");
//        SpringAppContextUtil.setApplicationContextHolder(context);
//        DispatcherServlet dispatcherServlet = new DispatcherServlet();
//        getServletContext().addServlet("dispatcherServlet",dispatcherServlet);
//    }

    public synchronized CompletionStage<HttpServer> start() {
        channelsUpFuture.thenAccept(this::started);
        channelsCloseFuture.whenComplete((webServer, throwable) -> shutdown(throwable));
        // Configure the server.
        bossGroup = new NioEventLoopGroup(builder.getAcceptors());
        workerGroup = new NioEventLoopGroup(builder.getIoWorkers());
        try {
            // Configure SSL.
            final SslContext sslCtx;
            if (SSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } else {
                sslCtx = null;
            }

            ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer(sslCtx));

            bootstrap.option(ChannelOption.SO_BACKLOG, builder.getBacklog());
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            bootstrap.childOption(ChannelOption.SO_SNDBUF, builder.getSendBuffer());
            bootstrap.childOption(ChannelOption.SO_RCVBUF, builder.getRecvBuffer());

            bootstrap.bind(builder.getPort()).addListener(channelFuture -> {
                String name = bootstrap.toString();
                if (!channelFuture.isSuccess()) {
                    log.info("Channel '" + name + "' startup failed with message '"
                            + channelFuture.cause().getMessage() + "'.");
                    channelsUpFuture.completeExceptionally(new IllegalStateException("Channel startup failed: " + name,
                            channelFuture.cause()));
                    return;
                }

                channel = ((ChannelFuture) channelFuture).channel();
                log.info("Channel '" + name + "' started: " + channel);

                channel.closeFuture().addListener(future -> {
                    log.info("Channel '" + name + "' closed: " + channel);
                    if (channelsUpFuture.isCompletedExceptionally()) {
                        if (future.cause() != null) {
                            log.warn(
                                    "Startup failure channel close failure",
                                    new IllegalStateException(future.cause()));
                        }
                    } else {
                        if (!future.isSuccess()) {
                            channelsCloseFuture.completeExceptionally(new IllegalStateException("Channel stop failure.",
                                    future.cause()));
                        } else if (null == channel) {
                            channelsCloseFuture.complete(this);
                        }
                        // else we're waiting for the rest of the channels to start, successful branch
                    }
                });

                if (channelsUpFuture.isCompletedExceptionally()) {
                    channel.close();
                }

                if (null != channel) {
                    log.info("All channels started: ");
                    channelsUpFuture.complete(this);
                }
                log.info(
                        "\n(  ____ )(  ___  )( (    /|(  __  \\ (  ___  )\n" +
                        "| (    )|| (   ) ||  \\  ( || (  \\  )| (   ) |\n" +
                        "| (____)|| (___) ||   \\ | || |   ) || (___) |\n" +
                        "|  _____)|  ___  || (\\ \\) || |   | ||  ___  |\n" +
                        "| (      | (   ) || | \\   || |   ) || (   ) |\n" +
                        "| )      | )   ( || )  \\  || (__/  )| )   ( |\n" +
                        "|/       |/     \\||/    )_)(______/ |/     \\|\n"
                        + "Panda Netty server is up! http://localhost:" + builder.getPort());
            });

        } catch (Exception ex) {
            log.error("Start panda application failed, cause: " + ex.getMessage(), ex);
        }
        return startFuture;
    }

    private void started(HttpServer server) {
        startFuture.complete(server);
    }

    private void shutdown(Throwable cause) {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        shutdownFuture.complete(this);
        log.info("shutdowned!");
    }


    public CompletionStage<HttpServer> shutdown() {
        if (!startFuture.isDone()) {
            startFuture.cancel(true);
        }
        channel.close();
        log.info("shutdowning!");
        channelsCloseFuture.complete(this);
        return shutdownFuture;
    }

    public CompletionStage<HttpServer> whenShutdown() {
        return shutdownFuture;
    }


    public Integer getPort() {
        try {
            channelsUpFuture.get();
        } catch (Exception e) {
            log.error("getPort error", e);
        }
        return builder.getPort();
    }


    public ServletContextImpl getServletContext() {
        return ServletContextImpl.get();
    }
}
