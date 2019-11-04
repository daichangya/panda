ServerSocket NioServerSocketChannel(ChannelPipeline(ChannelHandlerContext head,tail))
Socket NioSocketChannel

ChannelHandlerContext(ChannelHandler)

ChannelHandler执行顺序

	private DefaultChannelHandlerContext findContextInbound(int mask) {
	    DefaultChannelHandlerContext ctx = this;
	    do {
	        ctx = ctx.next;
	    } while ((ctx.executionMask & mask) == 0 && ctx.isProcessInboundDirectly());
	    return ctx;
	}
	
	private DefaultChannelHandlerContext findContextOutbound(int mask) {
	    DefaultChannelHandlerContext ctx = this;
	    do {
	        ctx = ctx.prev;
	    } while ((ctx.executionMask & mask) == 0 && ctx.isProcessOutboundDirectly());
	    return ctx;
	}

ChannelHandler添加到链表里面

    private void addLast0(DefaultChannelHandlerContext newCtx) {
        DefaultChannelHandlerContext prev = tail.prev;
        newCtx.prev = prev;
        newCtx.next = tail;
        prev.next = newCtx;
        tail.prev = newCtx;
        callHandlerAdded0(newCtx);
    }
链表数据内容 HeadHandler->HttpRequestDecoder->HttpResponseEncoder->HttpServerHandler-TailHandler

input 消息传递
AbstractNioByteChannel.read()->ChannelPipeline.fireChannelRead(msg)->head[ChannelHandlerContext].fireChannelRead(msg)->head.handler().channelRead(this, m)
read 传递顺序()只找inbound
HeadHandler->HttpRequestDecoder->HttpServerHandler-TailHandler


    @Override
    public ChannelHandlerContext fireChannelRead(final Object msg) {
        invokeChannelRead(findContextInbound(MASK_CHANNEL_READ), msg);
        return this;
    }
    
output消息传递
HttpServerHandler->ChannelHandlerContext(HttpServerHandler).write(response)->
next[ChannelHandlerContext(HttpResponseEncoder)].invokeWrite(m, promise)->((ChannelOutboundHandler) handler[HttpResponseEncoder]()).write(this, msg, promise)
->HeadContext.write(this, msg, promise)

HttpServerCodec= HttpRequestDecoder+HttpResponseEncoder

