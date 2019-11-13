package com.daicy.panda.netty;

import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import com.daicy.panda.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty
 * @date:19-11-13
 */
@Slf4j
public class PandaServerBuilder {
    private int backlog = Constant.DEFAULT_BACKLOG;

    private int acceptors = Constant.DEFAULT_ACCEPTOR_COUNT;
    private int ioWorkers = Constant.DEFAULT_IO_WORKER_COUNT;

    private int minWorkers = Constant.DEFAULT_MIN_WORKER_THREAD;
    private int maxWorkers = Constant.DEFAULT_MAX_WORKER_THREAD;

    private int maxConnection = Integer.MAX_VALUE;
    private int maxPendingRequest = Constant.DEFAULT_MAX_PENDING_REQUEST;
    private int maxIdleTime = Constant.DEFAULT_MAX_IDLE_TIME;

    // 以下参数用于accept到服务器的socket
    private int sendBuffer = Constant.DEFAULT_SEND_BUFFER_SIZE;
    private int recvBuffer = Constant.DEFAULT_RECV_BUFFER_SIZE;

    private int maxPacketLength = Constant.DEFAULT_MAX_PACKET_LENGTH;

    private boolean devMode;

    private String contextPath = StringUtils.EMPTY;

    private TracingThreadPoolExecutor executor;

    private int port;
    private InetAddress inetAddress;

    private PandaServerBuilder(int port) {
        this.port = port;
    }

    public static PandaServerBuilder forPort(int port) {
        return new PandaServerBuilder(port);
    }

    public PandaServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public PandaServerBuilder inetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
        return this;
    }

    public PandaServerBuilder backlog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public PandaServerBuilder acceptors(int acceptors) {
        this.acceptors = acceptors;
        return this;
    }

    public PandaServerBuilder ioWorkers(int ioWorkers) {
        this.ioWorkers = ioWorkers;
        return this;
    }

    public PandaServerBuilder minWorkers(int minWorkers) {
        this.minWorkers = minWorkers;
        return this;
    }

    public PandaServerBuilder maxWorkers(int maxWorkers) {
        this.maxWorkers = maxWorkers;
        return this;
    }

    public PandaServerBuilder maxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
        return this;
    }

    public PandaServerBuilder sendBuffer(int sendBuffer) {
        this.sendBuffer = sendBuffer;
        return this;
    }

    public PandaServerBuilder recvBuffer(int recvBuffer) {
        this.recvBuffer = recvBuffer;
        return this;
    }

    public PandaServerBuilder maxConnection(int maxConnection) {
        this.maxConnection = maxConnection;
        return this;
    }

    public PandaServerBuilder maxPendingRequest(int maxPendingRequest) {
        this.maxPendingRequest = maxPendingRequest;
        return this;
    }

    public PandaServerBuilder maxPacketLength(int maxPacketLength) {
        this.maxPacketLength = maxPacketLength;
        return this;
    }


    public PandaServerBuilder devMode(boolean devMode) {
        this.devMode = devMode;
        return this;
    }


    public PandaServerBuilder contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getBacklog() {
        return backlog;
    }

    public int getAcceptors() {
        return acceptors;
    }

    public int getIoWorkers() {
        return ioWorkers;
    }

    public int getMinWorkers() {
        return minWorkers;
    }

    public int getMaxWorkers() {
        return maxWorkers;
    }

    public int getMaxConnection() {
        return maxConnection;
    }

    public int getMaxPendingRequest() {
        return maxPendingRequest;
    }

    public boolean isDevMode() {
        return devMode;
    }


    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public int getSendBuffer() {
        return sendBuffer;
    }

    public int getRecvBuffer() {
        return recvBuffer;
    }

    public int getMaxPacketLength() {
        return maxPacketLength;
    }


    public TracingThreadPoolExecutor executor() {
        return this.executor;
    }

    public int getPort() {
        return port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public HttpServer build() {
        if (minWorkers > maxWorkers) {
            throw new IllegalArgumentException("minWorkers is greater than maxWorkers");
        }

        if (maxPendingRequest <= 0) {
            throw new IllegalArgumentException("maxPendingRequest must be greater than 0");
        }

        executor = new TracingThreadPoolExecutor(minWorkers, maxWorkers, new LinkedBlockingQueue<>(maxPendingRequest));
        if (!devMode) {
            executor.prestartAllCoreThreads();
        }
        ServletContextImpl.get().setPandaServerBuilder(this);
        return new HttpServer(this);
    }
}

