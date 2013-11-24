package com.hdd.crane.logistics.client.handler;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hdd.crane.logistics.Courier;
import com.hdd.crane.logistics.CourierBackup;
import com.hdd.crane.memcache.MemCacheDto;

public class MemCacheClientHandler extends IoHandlerAdapter {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final CopyOnWriteArraySet<IoSession> sessions = new CopyOnWriteArraySet<IoSession>();
    private long timeout = 10;

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        sessions.add(session);
        session.setAttribute("semaphore", new Semaphore(1));
        session.setAttribute("memCacheDto", new AtomicReference<MemCacheDto>());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        ((Semaphore) session.getAttribute("semaphore")).release();
        session.removeAttribute("semaphore");
        session.removeAttribute("memCacheDto");
        sessions.remove(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        ((Semaphore) session.getAttribute("semaphore")).release();
        MemCacheDto request = ((AtomicReference<MemCacheDto>) session.getAttribute("memCacheDto")).get();
        MemCacheDto reponse = (MemCacheDto) message;
        reponse.setId(request.getId());
        reponse.setSession(request.getSession());
        Courier.getInstance().putResponse(reponse);
    }

    private IoSession getWriteIdleSession() {
        for (IoSession session : sessions) {
            if (session.isWriterIdle()) {
                return session;
            }
        }
        return null;
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
                MemCacheDto request;
                try {
                    request = Courier.getInstance().takeRequest();
                } catch (InterruptedException e) {
                    continue;
                }
                IoSession session = null;
                do {
                    session = getWriteIdleSession();
                } while (session == null);

                try {
                    // waiting until last request has been responded
                    if (((Semaphore) session.getAttribute("semaphore")).tryAcquire(timeout, TimeUnit.SECONDS)) {
                        // send request to the memcached server
                        session.write(request);
                        // send request to the backup memcached server
                        CourierBackup.getInstance().putRequest(request);
                        // change the session's current request
                        ((AtomicReference<MemCacheDto>) session.getAttribute("memCacheDto")).set(request);
                    } else {
                        // if timeout,try to get the last requset's response
                        // from the backup memcached server(asynchronously)
                        request = ((AtomicReference<MemCacheDto>) session.getAttribute("memCacheDto")).get();
                        CourierBackup.getInstance().putRequest(request);
                        MemCacheDto response = null;
                        long lastime = System.currentTimeMillis();
                        long elapsedTime = 0;
                        do {
                            response = CourierBackup.getInstance().peekResponse();
                            elapsedTime = System.currentTimeMillis() - lastime;
                        } while (elapsedTime < timeout * 1000
                                && (null == response || response.getId() != request.getId()));
                        if (null != response) {
                            request.getSession().write(response);
                            CourierBackup.getInstance().removeResponse();
                        }
                    }

                } catch (InterruptedException e) {

                }

                // adjust the number of thread automaticly
                if (Courier.getInstance().exceedRequestUpperLimit()) {
                    Courier.getInstance().getExecutor().execute(new RequestWorker());
                } else if (Courier.getInstance().underRequestLowerLimit()) {
                    // terminate the thread by its self
                    if (count.get() > Courier.getInstance().getThreshold()) {
                        needRun = false;
                        count.decrementAndGet();
                    }
                }
            }
        }
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
