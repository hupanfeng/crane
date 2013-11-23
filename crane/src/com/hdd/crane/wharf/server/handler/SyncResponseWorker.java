package com.hdd.crane.wharf.server.handler;

import java.util.concurrent.atomic.AtomicInteger;

import com.hdd.crane.memcache.MemCacheDto;
import com.hdd.crane.wharf.server.Conveyor;

/**
 * Send response back to client periodically.
 * 
 * @author David Hu
 * @version 1.0.0
 * @since 2013-11-21
 */
public final class SyncResponseWorker implements Runnable {
    private volatile boolean needRun = true;
    private AtomicInteger count = new AtomicInteger(0);

    public SyncResponseWorker() {
        count.incrementAndGet();
    }

    @Override
    public void run() {
        while (needRun) {
            try {
                MemCacheDto sentRequest = Conveyor.getInstance().takeSentRequest();
                // send response to the client
                MemCacheDto response = null;
                do {
                    response = Conveyor.getInstance().peekResponse();
                } while (null == response || response.getId() != sentRequest.getId());

                sentRequest.getSession().write(response);
                Conveyor.getInstance().removeResponse();

                // adjust the number of thread automaticly
                if (Conveyor.getInstance().exceedSentRequestUpperLimit()) {
                    Conveyor.getInstance().getExecutor().execute(new SyncResponseWorker());
                } else if (Conveyor.getInstance().underSentRequestLowerLimit()) {
                    // terminate the thread by its self
                    if (count.get() > Conveyor.getInstance().getThreshold()) {
                        needRun = false;
                        count.decrementAndGet();
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }
}
