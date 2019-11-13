package com.daicy.panda;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class Request {

    public static final int BUFFER_SIZE = 4096;

    private InputStream input;
    private String uri;

    public Request(InputStream input) {
        this.input = input;
    }

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

}