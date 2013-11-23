package com.hdd.crane.wharf.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.hdd.crane.memcache.MemCacheDto;

/**
 * A thread-safe queue group.
 * 
 * @author David Hu
 * @version 1.0.0
 * @since 2013-11-22
 */
public class Conveyor {
    private AtomicReference<BlockingQueue<MemCacheDto>> requestQueue;
    private AtomicReference<BlockingQueue<MemCacheDto>> sentRequestQueue;
    private AtomicReference<BlockingQueue<MemCacheDto>> responseQueue;

    private AtomicInteger requestQueueCapacity;
    private AtomicInteger responseQueueCapacity;
    private final Executor executor;

    private final float upperLimitFactor = 0.5f;
    private final float lowerLimitFactor = 0.3f;
    private final int threshold = Runtime.getRuntime().availableProcessors();

    private static Conveyor instance = new Conveyor();

    private Conveyor() {
        executor = Executors.newCachedThreadPool();
    }

    public static Conveyor getInstance() {
        return instance;
    }

    public Executor getExecutor() {
        return executor;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setRequestCapacity(int capacity) {
        requestQueueCapacity.set(capacity);
        requestQueue.compareAndSet(null, new LinkedBlockingQueue<MemCacheDto>(capacity));
        sentRequestQueue.compareAndSet(null, new LinkedBlockingQueue<MemCacheDto>(capacity));
    }

    public void setResponseCapacity(int capacity) {
        responseQueueCapacity.set(capacity);
        responseQueue.compareAndSet(null, new LinkedBlockingQueue<MemCacheDto>(capacity));
    }

    public boolean exceedSentRequestUpperLimit() {
        return getSentRequestQueueSize() > requestQueueCapacity.get() * upperLimitFactor;
    }

    public boolean underSentRequestLowerLimit() {
        return getSentRequestQueueSize() < requestQueueCapacity.get() * lowerLimitFactor;
    }

    public boolean exceedRequestUpperLimit() {
        return getRequestQueueSize() > requestQueueCapacity.get() * upperLimitFactor;
    }

    public boolean underRequestLowerLimit() {
        return getRequestQueueSize() < requestQueueCapacity.get() * lowerLimitFactor;
    }

    public boolean exceedResponseUpperLimit() {
        return getResponseQueueSize() > responseQueueCapacity.get() * upperLimitFactor;
    }

    public boolean underResponseLowerLimit() {
        return getResponseQueueSize() < responseQueueCapacity.get() * lowerLimitFactor;
    }

    public void putRequest(MemCacheDto memcacheDto) throws InterruptedException {
        while (null == requestQueue.get()) {
            ;// nop
        }
        requestQueue.get().put(memcacheDto);
    }

    public MemCacheDto takeRequest() throws InterruptedException {
        while (null == requestQueue.get()) {
            ;// nop
        }
        return requestQueue.get().take();
    }

    public MemCacheDto peekRequest() {
        while (null == requestQueue.get()) {
            ;// nop
        }
        return requestQueue.get().peek();
    }

    public void removeRequest() {
        while (null == requestQueue.get()) {
            ;// nop
        }
        requestQueue.get().poll();
    }

    public int getRequestQueueSize() {
        while (null == requestQueue.get()) {
            ;// nop
        }
        return requestQueue.get().size();
    }

    public void putSentRequest(MemCacheDto memcacheDto) throws InterruptedException {
        while (null == sentRequestQueue.get()) {
            ;// nop
        }
        sentRequestQueue.get().put(memcacheDto);
    }

    public MemCacheDto takeSentRequest() throws InterruptedException {
        while (null == sentRequestQueue.get()) {
            ;// nop
        }
        return sentRequestQueue.get().take();
    }

    public MemCacheDto peekSentRequest() {
        while (null == sentRequestQueue.get()) {
            ;// nop
        }
        return sentRequestQueue.get().peek();
    }

    public void removeSentRequest() {
        while (null == sentRequestQueue.get()) {
            ;// nop
        }
        sentRequestQueue.get().poll();
    }

    public int getSentRequestQueueSize() {
        while (null == sentRequestQueue.get()) {
            ;// nop
        }
        return sentRequestQueue.get().size();
    }

    public void putResponse(MemCacheDto memcacheDto) throws InterruptedException {
        while (null == responseQueue.get()) {
            ;// nop
        }
        responseQueue.get().put(memcacheDto);
    }

    public MemCacheDto takeResponse() throws InterruptedException {
        while (null == responseQueue.get()) {
            ;// nop
        }
        return responseQueue.get().take();
    }

    public MemCacheDto peekResponse() {
        while (null == responseQueue.get()) {
            ;// nop
        }
        return responseQueue.get().peek();
    }

    public void removeResponse() {
        while (null == responseQueue.get()) {
            ;// nop
        }
        responseQueue.get().poll();
    }

    public int getResponseQueueSize() {
        while (null == responseQueue.get()) {
            ;// nop
        }
        return responseQueue.get().size();
    }
}
