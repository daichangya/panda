package com.daicy.panda.netty;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty
 * @date:19-11-13
 */
public class PandaStatus {
	private static PandaStatus INSTANCE = new PandaStatus();

	private AtomicInteger connections = new AtomicInteger(0);
	private AtomicInteger pendingRequests = new AtomicInteger(0);
	private AtomicLong totalRequests = new AtomicLong(0);
	private AtomicLong handledRequests = new AtomicLong(0);
	private int workerPoolSize;
	private int activePoolSize;

	private TracingThreadPoolExecutor workerPool;

	private PandaStatus() {
	}

	public static PandaStatus get() {
		return INSTANCE;
	}

	public PandaStatus totalRequestsIncrement() {
		totalRequests.incrementAndGet();
		return this;
	}

	public PandaStatus handledRequestsIncrement() {
		handledRequests.incrementAndGet();
		return this;
	}

	public PandaStatus connectionIncrement() {
		connections.incrementAndGet();
		return this;
	}

	public PandaStatus connectionDecrement() {
		connections.decrementAndGet();
		return this;
	}

	public PandaStatus pendingRequestsIncrement() {
		pendingRequests.incrementAndGet();
		return this;
	}

	public PandaStatus pendingRequestsDecrement() {
		pendingRequests.decrementAndGet();
		return this;
	}

	public int getConnections() {
		return connections.get();
	}

	public int getPendingRequests() {
		return pendingRequests.get();
	}

	public long getTotalRequests() {
		return totalRequests.get();
	}

	public long getHandledRequests() {
		return handledRequests.get();
	}

	public int getWorkerPoolSize() {
		this.workerPoolSize = workerPool.getPoolSize();
		return workerPoolSize;
	}

	public int getActivePoolSize() {
		this.activePoolSize = workerPool.getActiveCount();
		return activePoolSize;
	}

	public void workerPool(TracingThreadPoolExecutor workerPool) {
		this.workerPool = workerPool;
	}
}
