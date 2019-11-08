package com.daicy.panda.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.http
 * @date:19-11-8
 */
public class ByteBufServletOutputStream extends ServletOutputStream {

    private ByteBufOutputStream byteBufOutputStream;

    public ByteBufServletOutputStream() {
        ByteBuf directBuf =  Unpooled.directBuffer();
        this.byteBufOutputStream = new ByteBufOutputStream(directBuf);
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public void write(int b) throws IOException {
        byteBufOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byteBufOutputStream.write(b,off,len);
    }
}
