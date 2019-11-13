/*
 * Copyright 2017 The OpenDSP Project
 *
 * The OpenDSP Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.daicy.panda.netty;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author wangwp
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
