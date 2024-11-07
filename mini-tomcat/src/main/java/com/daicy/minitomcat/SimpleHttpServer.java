package com.daicy.minitomcat;

import java.io.*;
import java.net.*;

public class SimpleHttpServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("HTTP Server is running on port " + PORT);

            while (true) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from " + clientSocket.getInetAddress());

                // 获取输入流，读取客户端请求
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String requestLine = reader.readLine();
                if (requestLine != null) {
                    System.out.println("Request: " + requestLine);
                }

                // 构建一个简单的 HTTP 响应
                OutputStream outputStream = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                writer.println("HTTP/1.1 200 OK");
                writer.println("Content-Type: text/html; charset=UTF-8");
                writer.println();  // 空行，表示响应头结束
                writer.println("<html>");
                writer.println("<head><title>Simple HTTP Server</title></head>");
                writer.println("<body><h1>Hello, World!</h1></body>");
                writer.println("</html>");

                // 关闭连接
                clientSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
