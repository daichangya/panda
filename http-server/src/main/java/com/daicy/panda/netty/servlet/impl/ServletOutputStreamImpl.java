/*
 * Copyright 2013 by Maxim Kalina
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.daicy.panda.netty.servlet.impl;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

public class ServletOutputStreamImpl extends ServletOutputStream {

    private ServletResponseImpl servletResponse;

    private ByteBufOutputStream out;

    private int totalLength;//内容总长度

    private boolean flushed = false;

    public ServletOutputStreamImpl(ServletResponseImpl response) {
        this.servletResponse = response;
        this.out = new ByteBufOutputStream(Unpooled.buffer(0));
    }

    @Override
    public void write(int b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void write(byte[] b, int offset, int len) throws IOException {
        this.out.write(b, offset, len);
    }

    @Override
    public void flush() {
//        this.response.setContent(out.buffer());
//        servletResponse.getCtx().writeAndFlush(out.buffer().copy());
//        resetBuffer();
        boolean chunked = HttpUtil.isTransferEncodingChunked(servletResponse.getOriginalResponse());
        ChannelHandlerContext ctx = servletResponse.getCtx();
        if (chunked) {
            if (!flushed && ctx.channel().isActive()) {
                servletResponse.getCtx().writeAndFlush(servletResponse.getOriginalResponse());
            }
            if (out.buffer().writerIndex() > out.buffer().readerIndex() && ctx.channel().isActive()) {
                servletResponse.getCtx().writeAndFlush((new DefaultHttpContent(out.buffer().copy())));
                resetBuffer();
            }
            this.flushed = true;
        }
    }

    @Override
    public void close() {
        boolean chunked = HttpUtil.isTransferEncodingChunked(servletResponse.getOriginalResponse());
        ChannelHandlerContext ctx = servletResponse.getCtx();
        if (!chunked) {
            // 设置content-length头
            if (!HttpUtil.isContentLengthSet(servletResponse.getOriginalResponse())) {
                HttpUtil.setContentLength(servletResponse.getOriginalResponse(), this.out.buffer().readableBytes());
            }
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(servletResponse.getOriginalResponse());
            }
        }
        if (out.buffer().writerIndex() > out.buffer().readerIndex() && ctx.channel().isActive()) {
            ctx.writeAndFlush((new DefaultHttpContent(out.buffer())));
        }
    }

    public void resetBuffer() {
        this.out.buffer().clear();
    }

    public boolean isFlushed() {
        return flushed;
    }

    public int getBufferSize() {
        return this.out.buffer().capacity();
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
