package com.daicy.panda.sample;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.sample
 * @date:19-12-3
 */
@WebServlet("/TestServlet")
public class TestServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //content type must be set to text/event-stream
        response.setContentType("text/event-stream");
        //cache must be set to no-cache
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Transfer-Encoding", "chunked");
        //encoding is set to UTF-8
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();

        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            writer.write("data: " + i + "\n\n");
            writer.flush();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        writer.close();
    }
}