###基础了解
* netty https://juejin.im/post/5bea1d2e51882523d3163657
https://s0netty0io.icopy.site/wiki/native-transports.html
https://www.cnblogs.com/zou90512/p/3407192.html  
https://juejin.im/post/5add778bf265da0ba26697b3
https://skyao.gitbooks.io/learning-netty/content/channel/class_ChannelHandlerAdapter.html
https://www.jianshu.com/p/e5ac222f4847  
https://www.cnblogs.com/chenyangyao/p/5795100.html
* Http报文 https://blog.csdn.net/novelly/article/details/20001923
https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Messages
* curl https://man.linuxde.net/curl

###一个简单的HttpServer的必要元素
* Tcp 通讯接收器 即SocketServer
* 解析Http Request 
* Http 请求处理器
* 把结果信息写回到Http Response,使用tcp 把内容返回

SocketServer代码如下:
```
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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
public final class HttpServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new HttpServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

```
接受客户端tcp信息解析成Request
```
 HttpRequestDecoder
 HttpObjectDecoder
 
 @Override
     protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
         if (resetRequested) {
             resetNow();
         }
 
         switch (currentState) {
         case SKIP_CONTROL_CHARS: {
             log.debug(String.valueOf(currentState));
             if (!skipControlCharacters(buffer)) {
                 return;
             }
             currentState = State.READ_INITIAL;
         }
         case READ_INITIAL: try {
             log.debug(String.valueOf(currentState));
             //读取字节流，把消息行解析成字符串，AppendableCharSequence 是netty自己封装的功能类似String的对象
             //注意line这个对象和String不一样，是一个可变对象，即在别的地方的修改会同步修改其中内容
             //line GET / HTTP/1.1
             AppendableCharSequence line = lineParser.parse(buffer);
             if (line == null) {
                 return;
             }
             String[] initialLine = splitInitialLine(line);
             if (initialLine.length < 3) {
                 // Invalid initial line - ignore.
                 currentState = State.SKIP_CONTROL_CHARS;
                 return;
             }
 
             message = createMessage(initialLine);
             currentState = State.READ_HEADER;
             // fall-through
         } catch (Exception e) {
             out.add(invalidMessage(buffer, e));
             return;
         }
         case READ_HEADER: try {
             State nextState = readHeaders(buffer);
             if (nextState == null) {
                 return;
             }
             currentState = nextState;
             switch (nextState) {
             case SKIP_CONTROL_CHARS:
                 // fast-path
                 // No content is expected.
                 out.add(message);
                 out.add(LastHttpContent.EMPTY_LAST_CONTENT);
                 resetNow();
                 return;
             case READ_CHUNK_SIZE:
                 if (!chunkedSupported) {
                     throw new IllegalArgumentException("Chunked messages not supported");
                 }
                 // Chunked encoding - generate HttpMessage first.  HttpChunks will follow.
                 out.add(message);
                 return;
             default:
                 /**
                  * <a href="https://tools.ietf.org/html/rfc7230#section-3.3.3">RFC 7230, 3.3.3</a> states that if a
                  * request does not have either a transfer-encoding or a content-length header then the message body
                  * length is 0. However for a response the body length is the number of octets received prior to the
                  * server closing the connection. So we treat this as variable length chunked encoding.
                  */
                 long contentLength = contentLength();
                 if (contentLength == 0 || contentLength == -1 && isDecodingRequest()) {
                     out.add(message);
                     out.add(LastHttpContent.EMPTY_LAST_CONTENT);
                     resetNow();
                     return;
                 }
 
                 assert nextState == State.READ_FIXED_LENGTH_CONTENT ||
                         nextState == State.READ_VARIABLE_LENGTH_CONTENT;
 
                 out.add(message);
 
                 if (nextState == State.READ_FIXED_LENGTH_CONTENT) {
                     // chunkSize will be decreased as the READ_FIXED_LENGTH_CONTENT state reads data chunk by chunk.
                     chunkSize = contentLength;
                 }
 
                 // We return here, this forces decode to be called again where we will decode the content
                 return;
             }
         } catch (Exception e) {
             out.add(invalidMessage(buffer, e));
             return;
         }
         case READ_VARIABLE_LENGTH_CONTENT: {
             // Keep reading data as a chunk until the end of connection is reached.
             int toRead = buffer.readableBytes();
             if (toRead > 0) {
                 ByteBuf content = buffer.readRetainedSlice(toRead);
                 out.add(new DefaultHttpContent(content));
             }
             return;
         }
         case READ_FIXED_LENGTH_CONTENT: {
             int toRead = buffer.readableBytes();
 
             // Check if the buffer is readable first as we use the readable byte count
             // to create the HttpChunk. This is needed as otherwise we may end up with
             // create an HttpChunk instance that contains an empty buffer and so is
             // handled like it is the last HttpChunk.
             //
             // See https://github.com/netty/netty/issues/433
             if (toRead == 0) {
                 return;
             }
 
             if (toRead > chunkSize) {
                 toRead = (int) chunkSize;
             }
 
             ByteBuf content = buffer.readRetainedSlice(toRead);
             chunkSize -= toRead;
 
             if (chunkSize == 0) {
                 // Read all content.
                 out.add(new DefaultLastHttpContent(content, validateHeaders));
                 resetNow();
             } else {
                 out.add(new DefaultHttpContent(content));
             }
             return;
         }
         /**
          * everything else after this point takes care of reading chunked content. basically, read chunk size,
          * read chunk, read and ignore the CRLF and repeat until 0
          */
         case READ_CHUNK_SIZE: try {
             AppendableCharSequence line = lineParser.parse(buffer);
             if (line == null) {
                 return;
             }
             int chunkSize = getChunkSize(line.toString());
             this.chunkSize = chunkSize;
             if (chunkSize == 0) {
                 currentState = State.READ_CHUNK_FOOTER;
                 return;
             }
             currentState = State.READ_CHUNKED_CONTENT;
             // fall-through
         } catch (Exception e) {
             out.add(invalidChunk(buffer, e));
             return;
         }
         case READ_CHUNKED_CONTENT: {
             assert chunkSize <= Integer.MAX_VALUE;
             int toRead = (int) chunkSize;
             toRead = Math.min(toRead, buffer.readableBytes());
             if (toRead == 0) {
                 return;
             }
             HttpContent chunk = new DefaultHttpContent(buffer.readRetainedSlice(toRead));
             chunkSize -= toRead;
 
             out.add(chunk);
 
             if (chunkSize != 0) {
                 return;
             }
             currentState = State.READ_CHUNK_DELIMITER;
             // fall-through
         }
         case READ_CHUNK_DELIMITER: {
             final int wIdx = buffer.writerIndex();
             int rIdx = buffer.readerIndex();
             while (wIdx > rIdx) {
                 byte next = buffer.getByte(rIdx++);
                 if (next == HttpConstants.LF) {
                     currentState = State.READ_CHUNK_SIZE;
                     break;
                 }
             }
             buffer.readerIndex(rIdx);
             return;
         }
         case READ_CHUNK_FOOTER: try {
             LastHttpContent trailer = readTrailingHeaders(buffer);
             if (trailer == null) {
                 return;
             }
             out.add(trailer);
             resetNow();
             return;
         } catch (Exception e) {
             out.add(invalidChunk(buffer, e));
             return;
         }
         case BAD_MESSAGE: {
             // Keep discarding until disconnection.
             buffer.skipBytes(buffer.readableBytes());
             break;
         }
         case UPGRADED: {
             int readableBytes = buffer.readableBytes();
             if (readableBytes > 0) {
                 // Keep on consuming as otherwise we may trigger an DecoderException,
                 // other handler will replace this codec with the upgraded protocol codec to
                 // take the traffic over at some point then.
                 // See https://github.com/netty/netty/issues/2173
                 out.add(buffer.readBytes(readableBytes));
             }
             break;
         }
         }
     }
```
http请求处理逻辑
```
@Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            buf.setLength(0);
            buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            buf.append("===================================\r\n");

            buf.append("VERSION: ").append(request.protocolVersion()).append("\r\n");
            buf.append("HOSTNAME: ").append(request.headers().get(HttpHeaderNames.HOST, "unknown")).append("\r\n");
            buf.append("REQUEST_URI: ").append(request.uri()).append("\r\n\r\n");

            HttpHeaders headers = request.headers();
            if (!headers.isEmpty()) {
                for (Entry<String, String> h: headers) {
                    CharSequence key = h.getKey();
                    CharSequence value = h.getValue();
                    buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
                }
                buf.append("\r\n");
            }

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> params = queryStringDecoder.parameters();
            if (!params.isEmpty()) {
                for (Entry<String, List<String>> p: params.entrySet()) {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    for (String val : vals) {
                        buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                    }
                }
                buf.append("\r\n");
            }

            appendDecoderResult(buf, request);
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                buf.append("CONTENT: ");
                buf.append(content.toString(CharsetUtil.UTF_8));
                buf.append("\r\n");
                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                buf.append("END OF CONTENT\r\n");

                LastHttpContent trailer = (LastHttpContent) msg;
                if (!trailer.trailingHeaders().isEmpty()) {
                    buf.append("\r\n");
                    for (CharSequence name: trailer.trailingHeaders().names()) {
                        for (CharSequence value: trailer.trailingHeaders().getAll(name)) {
                            buf.append("TRAILING HEADER: ");
                            buf.append(name).append(" = ").append(value).append("\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                if (!writeResponse(trailer, ctx)) {
                    // If keep-alive is off, close the connection once the content is fully written.
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }
```
处理http请求,并把具体的信息写回Response
```
 HttpResponseEncoder
 HttpObjectEncoder
  @Override
     protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
         ByteBuf buf = null;
         if (msg instanceof HttpMessage) {
             if (state != ST_INIT) {
                 throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg)
                         + ", state: " + state);
             }
 
             @SuppressWarnings({ "unchecked", "CastConflictsWithInstanceof" })
             H m = (H) msg;
 
             buf = ctx.alloc().buffer((int) headersEncodedSizeAccumulator);
             // Encode the message.
             encodeInitialLine(buf, m);
             state = isContentAlwaysEmpty(m) ? ST_CONTENT_ALWAYS_EMPTY :
                     HttpUtil.isTransferEncodingChunked(m) ? ST_CONTENT_CHUNK : ST_CONTENT_NON_CHUNK;
 
             sanitizeHeadersBeforeEncode(m, state == ST_CONTENT_ALWAYS_EMPTY);
 
             encodeHeaders(m.headers(), buf);
             ByteBufUtil.writeShortBE(buf, CRLF_SHORT);
 
             headersEncodedSizeAccumulator = HEADERS_WEIGHT_NEW * padSizeForAccumulation(buf.readableBytes()) +
                                             HEADERS_WEIGHT_HISTORICAL * headersEncodedSizeAccumulator;
         }
 
         // Bypass the encoder in case of an empty buffer, so that the following idiom works:
         //
         //     ch.write(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
         //
         // See https://github.com/netty/netty/issues/2983 for more information.
         if (msg instanceof ByteBuf) {
             final ByteBuf potentialEmptyBuf = (ByteBuf) msg;
             if (!potentialEmptyBuf.isReadable()) {
                 out.add(potentialEmptyBuf.retain());
                 return;
             }
         }
 
         if (msg instanceof HttpContent || msg instanceof ByteBuf || msg instanceof FileRegion) {
             switch (state) {
                 case ST_INIT:
                     throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
                 case ST_CONTENT_NON_CHUNK:
                     final long contentLength = contentLength(msg);
                     if (contentLength > 0) {
                         if (buf != null && buf.writableBytes() >= contentLength && msg instanceof HttpContent) {
                             // merge into other buffer for performance reasons
                             buf.writeBytes(((HttpContent) msg).content());
                             out.add(buf);
                         } else {
                             if (buf != null) {
                                 out.add(buf);
                             }
                             out.add(encodeAndRetain(msg));
                         }
 
                         if (msg instanceof LastHttpContent) {
                             state = ST_INIT;
                         }
 
                         break;
                     }
 
                     // fall-through!
                 case ST_CONTENT_ALWAYS_EMPTY:
 
                     if (buf != null) {
                         // We allocated a buffer so add it now.
                         out.add(buf);
                     } else {
                         // Need to produce some output otherwise an
                         // IllegalStateException will be thrown as we did not write anything
                         // Its ok to just write an EMPTY_BUFFER as if there are reference count issues these will be
                         // propagated as the caller of the encode(...) method will release the original
                         // buffer.
                         // Writing an empty buffer will not actually write anything on the wire, so if there is a user
                         // error with msg it will not be visible externally
                         out.add(Unpooled.EMPTY_BUFFER);
                     }
 
                     break;
                 case ST_CONTENT_CHUNK:
                     if (buf != null) {
                         // We allocated a buffer so add it now.
                         out.add(buf);
                     }
                     encodeChunkedContent(ctx, msg, contentLength(msg), out);
 
                     break;
                 default:
                     throw new Error();
             }
 
             if (msg instanceof LastHttpContent) {
                 state = ST_INIT;
             }
         } else if (buf != null) {
             out.add(buf);
         }
     }

```
页面返回内容
```
WELCOME TO THE WILD WILD WEB SERVER
===================================
VERSION: HTTP/1.1
HOSTNAME: localhost:8080
REQUEST_URI: /

HEADER: Host = localhost:8080
HEADER: User-Agent = curl/7.58.0
HEADER: Accept = */*

END OF CONTENT
```