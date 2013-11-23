package com.hdd.crane.wharf.server.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hdd.crane.hash.ConsistentHash;
import com.hdd.crane.logistics.LogisticsMeta;
import com.hdd.crane.memcache.MemCacheDto;
import com.hdd.crane.wharf.server.Conveyor;

public class CustomsServiceHandler implements IoHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final ConsistentHash hash = new ConsistentHash();;
    private final ReentrantLock lock = new ReentrantLock();
    private final int defaultRequestQueueCapacity=100;
    private final int defaultResponseQueueCapacity=100;

    public CustomsServiceHandler() {
        Conveyor.getInstance().setRequestCapacity(defaultRequestQueueCapacity);
        Conveyor.getInstance().setResponseCapacity(defaultResponseQueueCapacity);
    }

    public CustomsServiceHandler(int requestQueueCapacity, int responseQueueCapacity) {
        Conveyor.getInstance().setRequestCapacity(requestQueueCapacity);
        Conveyor.getInstance().setResponseCapacity(responseQueueCapacity);
    }

    public void init() {
        for (int i = Conveyor.getInstance().getThreshold(); i > 0; i--) {
            Conveyor.getInstance().getExecutor().execute(new RequestWorker());
            Conveyor.getInstance().getExecutor().execute(new SyncResponseWorker());
        }
    }

    private final ConcurrentHashMap<String, LogisticsMeta> logisticsCache = new ConcurrentHashMap<String, LogisticsMeta>();

    @Override
    public void sessionCreated(IoSession session) throws Exception {
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        ((LogisticsMeta) session.getAttribute("logistics")).removeSession(session);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.warn("Exception caught!", cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof LogisticsMeta) {
            LogisticsMeta logistics = (LogisticsMeta) message;
            lock.lock();
            try {
                LogisticsMeta mayExistedLogistics = logisticsCache.get(logistics.getIdentity());
                if (null != mayExistedLogistics) {
                    mayExistedLogistics.addSession(session);
                    session.setAttribute("logistics", mayExistedLogistics);
                } else {
                    logistics.addSession(session);
                    session.setAttribute("logistics", logistics);
                    logisticsCache.put(logistics.getIdentity(), logistics);
                    // notify the prev sibling node to reset its backup node
                    String prevNode = hash.addNode(logistics.getIdentity());
                    if (null != prevNode) {
                        LogisticsMeta prevLogistics = logisticsCache.get(prevNode);
                        Conveyor.getInstance().getExecutor().execute(new SendWorker(prevLogistics, logistics));
                    }
                    // notify logistics to set its backup node
                    session.write(getLogisticsBackup(logistics));
                }
            } finally {
                lock.unlock();
            }
        } else if (message instanceof MemCacheDto) {
            Conveyor.getInstance().putResponse((MemCacheDto) message);
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {

    }

    private LogisticsMeta getLogisticsBackup(LogisticsMeta logistics) {
        return getLogistics(hash.getNodeBackup(logistics.getIdentity()));
    }

    private LogisticsMeta getLogistics(String key) {
        return logisticsCache.get(hash.getNode(key));
    }

    /**
     * 
     * @author David Hu
     * @version 1.0.0
     * @since 2013-11-21
     */
    private class SendWorker implements Runnable {
        private Object message;
        private LogisticsMeta logistics;

        public SendWorker(LogisticsMeta logistics, Object message) {
            this.logistics = logistics;
            this.message = message;
        }

        @Override
        public void run() {
            IoSession session = null;
            if (logistics.isAvailable()) {
                do {
                    session = logistics.getWriteIdleSession();
                } while (session == null);
            }
            session.write(message);
        }
    }

    /**
     * Send request periodically.
     * 
     * @author David Hu
     * @version 1.0.0
     * @since 2013-11-21
     */
    private class RequestWorker implements Runnable {
        private volatile boolean needRun;
        private AtomicInteger count = new AtomicInteger(0);

        public RequestWorker() {
            count.incrementAndGet();
        }

        @Override
        public void run() {
            while (needRun) {
                try {
                    MemCacheDto request = Conveyor.getInstance().takeRequest();
                    LogisticsMeta logistics = getLogistics(request.getKey());
                    IoSession session = null;
                    if (logistics.isAvailable()) {
                        do {
                            session = logistics.getWriteIdleSession();
                        } while (session == null);
                    } else {
                        LogisticsMeta logisticsBackup = getLogisticsBackup(logistics);
                        if (logisticsBackup.isAvailable()) {
                            do {
                                session = logisticsBackup.getWriteIdleSession();
                            } while (session == null);
                        } else {
                            log.error("Logistics[" + logistics.getIdentity() + "] and its backup[" + logisticsBackup.getIdentity() + "] both unAvailable! Please check those two host! ");
                            log.error("Request[" + request.getKey() + "] was discarded!");
                            // TODO:should construct a fail response message
                            continue;
                        }
                    }
                    // send request to the memcached server
                    session.write(request);
                    // well,it's not the most right time to move the request.fix
                    // it in the future
                    Conveyor.getInstance().putSentRequest(request);

                    // adjust the number of thread automaticly
                    if (Conveyor.getInstance().exceedRequestUpperLimit()) {
                        Conveyor.getInstance().getExecutor().execute(new RequestWorker());
                    } else if (Conveyor.getInstance().underRequestLowerLimit()) {
                        // terminate the thread by its self
                        if (count.get() > Conveyor.getInstance().getThreshold()) {
                            needRun = false;
                            count.decrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
    }

    public void stop() {

    }
}
