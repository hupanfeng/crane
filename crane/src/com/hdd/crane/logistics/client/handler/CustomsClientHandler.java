package com.hdd.crane.logistics.client.handler;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hdd.crane.logistics.Courier;
import com.hdd.crane.logistics.LogisticsMeta;
import com.hdd.crane.memcache.MemCacheDto;

public class CustomsClientHandler extends IoHandlerAdapter {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final LogisticsMeta logisticServer = new LogisticsMeta();
    private volatile LogisticsMeta logisticsBackup;

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        session.write(logisticServer);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        // Empty handler
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof MemCacheDto) {
            Courier.getInstance().putRequest((MemCacheDto) message);
        } else if (message instanceof LogisticsMeta) {
            logisticsBackup = (LogisticsMeta) message;
        }
    }
    
    /**
     * Send response back to client periodically.
     * 
     * @author David Hu
     * @version 1.0.0
     * @since 2013-11-21
     */
    private class ResponseWorker implements Runnable {
        private volatile boolean needRun = true;
        private AtomicInteger count = new AtomicInteger(0);

        public ResponseWorker() {
            count.incrementAndGet();
        }

        @Override
        public void run() {
            while (needRun) {
                try {
                    // send response to the client
                    MemCacheDto response = Courier.getInstance().takeResponse();
                    response.getSession().write(response);

                    // adjust the number of thread automaticly
                    if (Courier.getInstance().exceedResponseUpperLimit()) {
                        Courier.getInstance().getExecutor().execute(new ResponseWorker());
                    } else if (Courier.getInstance().underResponseLowerLimit()) {
                        // terminate the thread by its self
                        if (count.get() > Courier.getInstance().getThreshold()) {
                            needRun = false;
                            count.decrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {

                }
            }
        }
    }

}
