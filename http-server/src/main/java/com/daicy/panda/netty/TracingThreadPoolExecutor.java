package com.daicy.panda.netty;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty
 * @date:19-11-13
 */
public class TracingThreadPoolExecutor extends ThreadPoolExecutor {
    private final AtomicInteger pendingTasks = new AtomicInteger();
    private final PandaStatus serverStatus = PandaStatus.get();

    public TracingThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                     BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, workQueue);
        serverStatus.workerPool(this);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        serverStatus.pendingRequestsIncrement();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        serverStatus.pendingRequestsDecrement();
    }

    public int getPendingTasks() {
        return pendingTasks.get();
    }
}