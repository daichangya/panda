###handle升级
HttpServerCodec= HttpRequestDecoder+HttpResponseEncoder
因此HttpServerCodec替换了
HttpRequestDecoder,HttpResponseEncoder

HttpObjectAggregator集合多个message为一个对象
例如
AggregatedFullHttpRequest 包含下面对象
    
    protected final HttpMessage message;
    private final ByteBuf content;
    private HttpHeaders trailingHeaders;
    
 而DefaultHttpRequest没有content
 
    private HttpMethod method;
    private String uri;
    private HttpVersion version;
    private final HttpHeaders headers;


HttpStaticFileServerHandler处理静态文件(目前仅包含.html结尾的)

        final String uri = request.uri();
        final String path = sanitizeUri(uri);
        if (path == null) {
            this.sendError(ctx, FORBIDDEN);
            return;
        } else if (!path.endsWith(".html")) {
            ctx.fireChannelRead(request);
            return;
        }
        
HttpServerInitializer代码如下:
```
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        // Uncomment the following line if you don't want to handle HttpChunks.
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpStaticFileServerHandler());

//        pipeline.addLast(new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        //pipeline.addLast(new HttpContentCompressor());
        pipeline.addLast(new HttpServerHandler());
        
```

静态资源处理逻辑
```
   File file = new File(Resources.getResource(path).getFile());
        if (file.isHidden() || !file.exists()) {
            this.sendError(ctx, NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                this.sendListing(ctx, file, uri);
            } else {
                this.sendRedirect(ctx, uri + '/');
            }
            return;
        }

        if (!file.isFile()) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        // Cache Validation
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                this.sendNotModified(ctx);
                return;
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            sendFileFuture =
                    ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            // Write the end marker.
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            sendFileFuture =
                    ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                            ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;
        }

        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) { // total unknown
                    System.err.println(future.channel() + " Transfer progress: " + progress);
                } else {
                    System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                System.err.println(future.channel() + " Transfer complete.");
            }
        });

        // Decide whether to close the connection or not.
        if (!keepAlive) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
```

压测报告1(直接返回字符串)
```
daichangya@daichangya:~$ wrk -t8 -c100 -d10s --latency   http://localhost:8080
Running 10s test @ http://localhost:8080
  8 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.42ms    4.03ms  90.19ms   93.31%
    Req/Sec    27.57k     9.12k   56.47k    74.62%
  Latency Distribution
     50%  326.00us
     75%  657.00us
     90%    3.23ms
     99%   19.74ms
  2198188 requests in 10.05s, 773.56MB read
Requests/sec: 218729.83
Transfer/sec:     76.97MB

```
压测报告2(读取静态文件)
```
daichangya@daichangya:~$ wrk -t8 -c100 -d10s --latency   http://localhost:8080/netty4.html
Running 10s test @ http://localhost:8080/netty4.html
  8 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     6.11ms    6.78ms 134.71ms   89.03%
    Req/Sec     2.50k   630.03     4.23k    77.50%
  Latency Distribution
     50%    4.21ms
     75%    8.11ms
     90%   13.48ms
     99%   32.52ms
  199248 requests in 10.01s, 68.22MB read
Requests/sec:  19895.54
Transfer/sec:      6.81MB
```