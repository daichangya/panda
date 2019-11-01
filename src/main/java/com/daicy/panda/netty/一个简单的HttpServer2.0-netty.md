###基础了解
* Socket https://blog.csdn.net/novelly/article/details/5644660
* Http报文 https://blog.csdn.net/novelly/article/details/20001923
https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Messages
* curl https://man.linuxde.net/curl
####一次简单的Http 请求
```
curl -v "www.baidu.com/info.html"
*   Trying 180.101.49.12...
* TCP_NODELAY set
* Connected to www.baidu.com (180.101.49.12) port 80 (#0)
> GET /info.html HTTP/1.1
> Host: www.baidu.com
> User-Agent: curl/7.58.0
> Accept: */*
> 
< HTTP/1.1 302 Found
< Cache-Control: max-age=86400
< Connection: Keep-Alive
< Content-Length: 222
< Content-Type: text/html; charset=iso-8859-1
< Date: Thu, 31 Oct 2019 08:09:40 GMT
< Expires: Fri, 01 Nov 2019 08:09:40 GMT
< Location: https://www.baidu.com/search/error.html
< Server: Apache
< 
<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
<html><head>
<title>302 Found</title>
</head><body>
<h1>Found</h1>
<p>The document has moved <a href="http://www.baidu.com/search/error.html">here</a>.</p>
</body></html>
* Connection #0 to host www.baidu.com left intact
```
从上面的信息我们可以看到以下三类信息
* 请求头
```
GET /info.html HTTP/1.1
Host: www.baidu.com
User-Agent: curl/7.58.0
Accept: */*
```
其中目前对我们有用的仅仅是 /info.html .得到这个信息以后,我们就知道了要加载那个文件返回.

* 响应头
```
HTTP/1.1 302 Found
Cache-Control: max-age=86400
Connection: Keep-Alive
Content-Length: 222
Content-Type: text/html; charset=iso-8859-1
Date: Thu, 31 Oct 2019 08:09:40 GMT
Expires: Fri, 01 Nov 2019 08:09:40 GMT
Location: https://www.baidu.com/search/error.html
Server: Apache
```
响应头是浏览器要用的,它会根据 http 状态码进行后面的处理.
200 就渲染页面.
302 就要进行页面跳转. 例如上面的响应就是info.html页面没有,要跳转到Location: https://www.baidu.com/search/error.html

* 响应体
```
<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
<html><head>
<title>302 Found</title>
</head><body>
<h1>Found</h1>
<p>The document has moved <a href="http://www.baidu.com/search/error.html">here</a>.</p>
</body></html>
* Connection #0 to host www.baidu.com left intact
```
响应体就是浏览器要渲染的内容

###一个简单的HttpServer的必要元素
* Tcp 通讯接收器 即SocketServer
* 解析Http Request 
* Http 请求处理器
* 把结果信息写回到Http Response,使用tcp 把内容返回

SocketServer代码如下:
```
package com.daicy.panda;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class HttpServer {

    public static final String WEB_ROOT = "static";

    public static final String INDEX = "/index.html";


    public static final int port = 8080;

    // shutdown command
    public static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

    // the shutdown command received
    private boolean shutdown = false;

    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            log.error("ServerSocket error", e);
        }
        server.await(serverSocket);
    }

    public void await(ServerSocket serverSocket) {
        // Loop waiting for a request
        while (!shutdown) {
            Socket socket = null;
            InputStream input = null;
            OutputStream output = null;
            try {
                socket = serverSocket.accept();
                input = socket.getInputStream();
                output = socket.getOutputStream();

                // create Request object and parse
                Request request = new Request(input);
                request.parse();

                // create Response object
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource();

                // Close the socket
                socket.close();

                //check if the previous URI is a shutdown command
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
            } catch (Exception e) {
                log.error("http handle error", e);
                continue;
            }
        }
    }
}

```
接受客户端tcp信息解析出我们想要的信息,目前我们主要需要uri
```
 public void parse() {
        // Read a set of characters from the socket
        StringBuffer request = new StringBuffer();
        int i;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            i = input.read(buffer);
        } catch (IOException e) {
            log.error("http request read error", e);
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        log.info("request:{}", request);
        uri = parseUri(request.toString());
    }

    private String parseUri(String requestString) {
        String[] strs = StringUtils.split(requestString, " ");
        if (CollectionUtils.size(strs) > 1) {
            return strs[1];
        }
        return null;
    }

    public String getUri() {
        return uri;
    }
```
处理http请求,并把具体的信息写回给客户端
```
FileInputStream fis = null;
        byte[] bytes = new byte[BUFFER_SIZE];
        try {
            String uri = request.getUri();
            if (StringUtils.equals("/", request.getUri())) {
                uri = HttpServer.INDEX;
            }
            URL url = Resources.getResource(HttpServer.WEB_ROOT + uri);
            fis = new FileInputStream(url.getFile());
            int ch = fis.read(bytes, 0, BUFFER_SIZE);
            while (ch != -1) {
                output.write(bytes, 0, ch);
                ch = fis.read(bytes, 0, BUFFER_SIZE);
            }
        } catch (Exception e) {
            // file not found
            String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: 23\r\n" +
                    "\r\n" +
                    "<h1>File Not Found</h1>";
            output.write(errorMessage.getBytes());
            log.error("http response error", e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
```
index.html内容
```
HTTP/1.1 200 OK
Content-Type: text/html

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
  Hello World!
</body>
</html>
```