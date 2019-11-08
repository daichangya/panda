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

import com.daicy.panda.http.ServletRequestImpl;
import com.daicy.panda.method.HandlerMethod;
import com.daicy.panda.method.RequestMappingHandlerMapping;
import com.daicy.panda.util.SpringAppContextUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.DataBinder;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        final StringBuilder buf = new StringBuilder();
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request =  (FullHttpRequest) msg;
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
            ServletRequestImpl servletRequest = new ServletRequestImpl(request);
            HandlerMethod handlerMethod = RequestMappingHandlerMapping.getInstance().get(servletRequest.getPath());
            if(null != handlerMethod){
                try {
                    Object[] args = getArgs(servletRequest, handlerMethod);
                    Object bean = SpringAppContextUtil.getBean(handlerMethod.getClazz());
                    Object result = handlerMethod.getMethod().invoke(bean,args);
                    buf.append(result);
                } catch (IllegalAccessException e) {
                    log.error("controller invoke uri:{}",request.uri(),e);
                } catch (InvocationTargetException e) {
                    log.error("controller invoke uri:{}",request.uri(),e);
                }
            }else {
                requestToStr(request,buf);
            }

            if (!writeResponse(buf,request, ctx)) {
                // If keep-alive is off, close the connection once the content is fully written.
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private Object[] getArgs(ServletRequestImpl servletRequest, HandlerMethod handlerMethod) {
        MethodParameter[] parameters = handlerMethod.getParameters();
        if(ObjectUtils.isEmpty(parameters)){
            return null;
        }
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            String parameterName = parameters[i].getParameterName();
            String[] paramValues = servletRequest.getParameterValues(parameterName);
            Object requestArg = null;
            if (paramValues != null) {
                requestArg = (paramValues.length == 1 ? paramValues[0] : paramValues);
            }
            args[i] = new DataBinder(null, parameterName)
                    .convertIfNecessary(requestArg, parameters[i].getParameterType(), parameters[i]);
        }
        return args;
    }

    private void requestToStr(FullHttpRequest request,StringBuilder buf) {
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

        ByteBuf content = request.content();
        if (content.isReadable()) {
            buf.append("CONTENT: ");
            buf.append(content.toString(CharsetUtil.UTF_8));
            buf.append("\r\n");
            appendDecoderResult(buf, request);
        }
    }

    private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
        DecoderResult result = o.decoderResult();
        if (result.isSuccess()) {
            return;
        }

        buf.append(".. WITH DECODER FAILURE: ");
        buf.append(result.cause());
        buf.append("\r\n");
    }

    private boolean writeResponse(StringBuilder buf,FullHttpRequest request, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, request.decoderResult().isSuccess()? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Encode the cookie.
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie: cookies) {
                    response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
                }
            }
        } else {
            // Browser sent no cookie.  Add some.
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key1", "value1"));
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key2", "value2"));
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
