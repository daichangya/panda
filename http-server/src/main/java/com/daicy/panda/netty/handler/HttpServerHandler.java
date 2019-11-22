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

import com.daicy.panda.netty.PandaStatus;
import com.daicy.panda.netty.servlet.ChannelThreadLocal;
import com.daicy.panda.netty.servlet.impl.ServletRequestImpl;
import com.daicy.panda.netty.servlet.impl.ServletResponseImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final PandaStatus status;

    public HttpServerHandler() {
        this.status = PandaStatus.get();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        status.totalRequestsIncrement();
//        if (asyncExecutor == null) {
//            handleRequest(ctx, msg);
//            return;
//        }
//
//        asyncExecutor.execute(() -> {
//            handleRequest(ctx, msg);
//        });
//    }
//
//    private void handleRequest(ChannelHandlerContext ctx, Object msg) {
        try {
            ChannelThreadLocal.set(ctx.channel());
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest) msg;
                FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
                HttpUtil.setKeepAlive(response, HttpUtil.isKeepAlive(request));
                ServletRequestImpl servletRequest = new ServletRequestImpl(request);
                ServletResponseImpl servletResponse = new ServletResponseImpl(ctx, response);
                NettyServletHandler.handleRequest(servletRequest,servletResponse);
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