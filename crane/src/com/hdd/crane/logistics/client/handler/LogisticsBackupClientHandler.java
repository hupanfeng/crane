package com.hdd.crane.logistics.client.handler;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hdd.crane.logistics.CourierBackup;
import com.hdd.crane.memcache.MemCacheDto;

public class LogisticsBackupClientHandler extends IoHandlerAdapter {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final CopyOnWriteArraySet<IoSession> sessions = new CopyOnWriteArraySet<IoSession>();

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        sessions.remove(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        MemCacheDto reponse = (MemCacheDto) message;
        CourierBackup.getInstance().putResponse(reponse);
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
                    request = CourierBackup.getInstance().takeRequest();
                } catch (InterruptedException e) {
                    continue;
                }
                IoSession session = null;
                do {
                    session = getWriteIdleSession();
                } while (session == null);

                session.write(request);

                // adjust the number of thread automaticly
                if (CourierBackup.getInstance().exceedRequestUpperLimit()) {
                    CourierBackup.getInstance().getExecutor().execute(new RequestWorker());
                } else if (CourierBackup.getInstance().underRequestLowerLimit()) {
                    // terminate the thread by its self
                    if (count.get() > CourierBackup.getInstance().getThreshold()) {
                        needRun = false;
                        count.decrementAndGet();
                    }
                }
            }
        }
    }
}
