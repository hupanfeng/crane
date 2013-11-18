package com.hdd.crane.wharf.server.handler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hdd.crane.memcache.MemCacheDto;
import com.hdd.crane.wharf.server.Conveyor;

public class WharfServiceHandler implements IoHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Executor executor;

    public WharfServiceHandler() {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = Runtime.getRuntime().availableProcessors(); i > 0; i--) {
            executor.execute(new ResponseWoker());
        }
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        log.debug("session[id:" + session.getId() + "] created");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.debug("session[id:" + session.getId() + "] opened");

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        log.debug("Received one message.");
        if (message instanceof MemCacheDto) {
            Conveyor.requestQueue.offer((MemCacheDto) message);
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {

    }

    private class ResponseWoker implements Runnable {

        @Override
        public void run() {
            try {
                MemCacheDto response = Conveyor.responseQueue.take();
                response.getSession().write(response);
            } catch (InterruptedException e) {
                // do nothing
            }

        }

    }
}
