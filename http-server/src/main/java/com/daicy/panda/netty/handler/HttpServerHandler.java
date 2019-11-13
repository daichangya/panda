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
package com.daicy.panda.netty.handler;

import com.daicy.panda.netty.PandaServerBuilder;
import com.daicy.panda.netty.PandaStatus;
import com.daicy.panda.netty.TracingThreadPoolExecutor;
import com.daicy.panda.netty.servlet.ChannelThreadLocal;
import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import com.daicy.panda.netty.servlet.impl.ServletRequestImpl;
import com.daicy.panda.netty.servlet.impl.ServletResponseImpl;
import com.daicy.panda.netty.servlet.impl.filter.FilterChainFactory;
import com.daicy.panda.netty.servlet.impl.filter.FilterChainImpl;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;

import javax.servlet.Servlet;

@Slf4j
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final TracingThreadPoolExecutor asyncExecutor;
    private final PandaStatus status;

    public HttpServerHandler(PandaServerBuilder pandaServerBuilder) {
        this.asyncExecutor = pandaServerBuilder.executor();
        this.status = PandaStatus.get();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        status.totalRequestsIncrement();
        if (asyncExecutor == null) {
            handleRequest(ctx, msg);
            return;
        }

        asyncExecutor.execute(() -> {
            handleRequest(ctx, msg);
        });
    }

    private void handleRequest(ChannelHandlerContext ctx, Object msg) {
        try {
            ChannelThreadLocal.set(ctx.channel());
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest) msg;
                boolean isKeepAlive = HttpUtil.isKeepAlive(request);
                FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
                ServletRequestImpl servletRequest = new ServletRequestImpl(request);
                ServletResponseImpl servletResponse = new ServletResponseImpl(response);

                try {
                    Servlet servlet = ServletContextImpl.get().getServlet(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
                    FilterChainImpl chain = FilterChainFactory.createFilterChain(servletRequest, servlet);
                    chain.doFilter(servletRequest, servletResponse);
                    servlet.service(servletRequest, servletResponse);
                } catch (Exception e) {
                    log.error("controller invoke uri:{}", request.uri(), e);
                }

                if (isKeepAlive) {
                    // Add 'Content-Length' header only for a keep-alive connection.
                    response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                    // Add keep alive header as per:
                    // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }
                ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);

                if (!isKeepAlive && channelFuture != null) {
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                }
            }
        } finally {
            status.handledRequestsIncrement();
            ChannelThreadLocal.unset();
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        status.connectionIncrement();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        status.connectionDecrement();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
