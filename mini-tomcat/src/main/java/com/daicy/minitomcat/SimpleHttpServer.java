package com.daicy.minitomcat;
import com.google.common.io.Resources;

import java.io.*;
import java.net.*;

/**
 * @author 代长亚
 * 一个简单的 HTTP 服务器，用于处理 GET 请求并返回静态文件。
 */
public class SimpleHttpServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "webroot"; // 静态文件根目录

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("HTTP Server is running on port " + PORT);

            while (true) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from " + clientSocket.getInetAddress());

                // 处理请求并发送响应
                handleRequest(clientSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // 读取并解析请求行
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;
            System.out.println("Request: " + requestLine);

            // 解析请求方法和路径
            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            // 检查是否为 GET 请求
            if (!method.equals("GET")) {
                sendResponse(outputStream, 405, "Method Not Allowed", "Only GET method is supported.");
                return;
            }

            // 查找请求的静态文件
            URL url = Resources.getResource(WEB_ROOT+ path);
            File file = new File(url.getPath());
            if (file.exists() && !file.isDirectory()) {
                sendFileResponse(outputStream, file);
            } else {
                sendResponse(outputStream, 404, "Not Found", "The requested resource was not found.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 发送普通文本响应
    private static void sendResponse(OutputStream outputStream, int statusCode, String statusText, String message) throws IOException {
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println("HTTP/1.1 " + statusCode + " " + statusText);
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println();
        writer.println("<html><body><h1>" + statusCode + " " + statusText + "</h1><p>" + message + "</p></body></html>");
    }

    // 发送文件响应
    private static void sendFileResponse(OutputStream outputStream, File file) throws IOException {
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: " + getContentType(file));
        writer.println("Content-Length: " + file.length());
        writer.println();

        // 发送文件内容
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    // 根据文件后缀返回 Content-Type
    private static String getContentType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".html") || name.endsWith(".htm")) {
            return "text/html";
        } else if (name.endsWith(".css")) {
            return "text/css";
        } else if (name.endsWith(".js")) {
            return "application/javascript";
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (name.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
}