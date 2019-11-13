package com.daicy.panda.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty
 * @date:19-10-31
 */
public class NettyServerTest {

    public static void main(String[] args) throws InterruptedException {
        //两个线程池
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        NioEventLoopGroup childEventLoopGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap
                .group(eventLoopGroup,childEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(9999))
                //对serversocketchannel的回调
                .handler(new ChannelInitializer<ServerSocketChannel>() {
                    @Override
                    protected void initChannel(ServerSocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new TestInboundChannelHandler("server  in "))
                                .addLast(new TestOutboundChannelHandler("server  out "));

                    }
                })
                //对socketchannel的回调
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new TestInboundChannelHandler("first in "))
                                .addLast(new TestInboundChannelHandler("second in "))
                                .addLast(new EchoChannelHandler())
                                .addLast(new TestOutboundChannelHandler("first out "))
                                .addLast(new TestOutboundChannelHandler("second out "));

                    }
                });
        //等到绑定完成
        ChannelFuture channelFuture = bootstrap.bind().sync();
        //等到serversocketchannel close在退出
        channelFuture.channel().closeFuture().sync();

    }

    static class TestInboundChannelHandler extends ChannelInboundHandlerAdapter {

        private String name;

        public TestInboundChannelHandler(String name) {
            this.name = name;
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println(name+"channelRegistered spread");
            ctx.fireChannelRegistered();
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println(name+"channelUnregistered not spread");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println(name+"channelActive not spread");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println(name+"channelInactive not spread");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println(name+"channelRead spread");
            ctx.fireChannelRead(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println(name+"channelReadComplete spread");
            ctx.fireChannelReadComplete();
        }
    }

    static class EchoChannelHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("echo channelRegistered");
            //ctx.writeAndFlush(Unpooled.copiedBuffer("Hello\r\n".getBytes()));
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("echo channelUnregistered");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("echo channelInactive");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("echo channelActive");
            ctx.writeAndFlush(Unpooled.copiedBuffer("Hello\r\n".getBytes()));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("echo channelRead");
            ByteBuf byteBuf = (ByteBuf)msg;
            System.out.println(new String(ByteBufUtil.getBytes(byteBuf)));
            //ctx.write(msg);
            ctx.channel().write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("echo channelReadComplete");
            ctx.channel().flush();
        }
    }

    static class TestOutboundChannelHandler extends ChannelOutboundHandlerAdapter {

        private String name;

        public TestOutboundChannelHandler(String name) {
            this.name = name;
        }

        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            //super.bind(ctx,localAddress,promise);
            System.out.println(name+ "bind");
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            super.connect(ctx,remoteAddress,localAddress,promise);
            System.out.println(name+"connect");
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            //super.disconnect(ctx,promise);
            System.out.println(name+"disconnect");
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            //super.close(ctx,promise);
            System.out.println(name+"close");
        }

        @Override
        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            System.out.println(name+"deregister");
        }

        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            ctx.read();
            System.out.println(name+"read");
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            //super.write(ctx,msg,promise);
            ctx.write(msg,promise);
            System.out.println(name+"write");
            //promise.setSuccess();
            promise.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println(name+" listener trigger");
                    }
                }
            });
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            System.out.println(name+"flush");
            ctx.flush();
        }
    }
}
