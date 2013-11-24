package com.hdd.crane.client;

import java.net.InetSocketAddress;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.apr.AprSocketConnector;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseClient implements Client {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private String clientType = "NIO";
    protected IoConnector connector;

    private int bufferSize = 2048;
    private int idleTime = 10;
    private int connectTimeout = 60;

    protected int remotePort;
    protected String remoteIP;

    protected String localIP = "localhost";
    protected int localPort = 0;

    protected volatile IoSession session;

    @Override
    public void init() throws Exception {
        setClient();
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.setConnectTimeoutMillis(connectTimeout);
        connector.getSessionConfig().setReadBufferSize(this.bufferSize);
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, this.idleTime);
        innerInit();
    }

    protected abstract void innerInit() throws Exception;

    @Override
    public void start() throws Exception {
        for (;;) {
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(remoteIP, remotePort),
                        new InetSocketAddress(localIP, localPort));
                future.awaitUninterruptibly(connectTimeout);
                session = future.getSession();
                break;
            } catch (RuntimeIoException e) {
                log.error("Connect to remote server[" + remoteIP + ":" + remotePort + "] fail!", e);
                Thread.sleep(1000);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        session.close(true);
        connector.dispose();
    }

    /**
     * @throws Exception
     * 
     */
    private void setClient() throws Exception {
        switch (clientType) {
        case "NIO":
            this.connector = new NioSocketConnector();
            break;
        case "APR":
            this.connector = new AprSocketConnector();
            break;
        case "UDP":
            this.connector = new NioDatagramConnector();
            break;
        default:
            throw new Exception("clientType error!");
        }
    }

}
