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

import com.daicy.panda.netty.servlet.impl.PandaContext;
import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import com.daicy.panda.util.SpringAppContextUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.net.InetSocketAddress;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
@Slf4j
public final class HttpServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));

    private Channel channel;

    private ServletContextImpl servletContext;

    public static void main(String[] args) throws Exception {
        HttpServer httpServer = new HttpServer();
        httpServer.createConfig();
        httpServer.start();
    }

    public HttpServer() {
        servletContext = new ServletContextImpl(new PandaContext());
    }

    private void createConfig(){

        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setConfigLocation("classpath:/services.xml");
        SpringAppContextUtil.setApplicationContextHolder(context);
    }

    public void start() {

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // Configure SSL.
            final SslContext sslCtx;
            if (SSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } else {
                sslCtx = null;
            }

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer(sslCtx));

            channel = b.bind(PORT).sync().channel();

            log.info("Open your web browser and navigate to " +
                    (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            channel.closeFuture().sync();
        } catch (Exception ex) {
            log.error("Start panda application failed, cause: " + ex.getMessage(), ex);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public InetSocketAddress address() {
        Channel c = getChannel();
        if (c instanceof SocketChannel) {
            return ((SocketChannel) c).remoteAddress();
        }
        if (c instanceof ServerSocketChannel) {
            return ((ServerSocketChannel) c).localAddress();
        }
        if (c instanceof DatagramChannel) {
            InetSocketAddress a = ((DatagramChannel) c).remoteAddress();
            return a != null ? a : ((DatagramChannel) c).localAddress();
        }
        throw new IllegalStateException("Does not have an InetSocketAddress");
    }


    public Channel getChannel() {
        return channel;
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }
}
