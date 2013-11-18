package com.hdd.crane.server;

import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.apr.AprSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseService implements Service {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private String serverType = "NIO";
    private int bufferSize = 2048;
    private int idleTime = 10;
    protected IoAcceptor server;
    protected int port = 10086;
    protected String ip = "localhost";

    /**
     * set the server type,only 'NIO' or 'APR' accepted,if use 'APR' make sure
     * that your system has installed APR library beforehand.
     * 
     * @param serverType
     */
    @Override
    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setIP(String ip) throws Exception {
        this.ip = ip;
    }

    /**
     * @throws Exception
     * 
     */
    private void setServer() throws Exception {
        switch (serverType) {
        case "NIO":
            this.server = new NioSocketAcceptor();
            break;
        case "APR":
            this.server = new AprSocketAcceptor();
            break;
        default:
            throw new Exception("severType error!");
        }
    }

    @Override
    public void init() throws Exception {

        setServer();
        server.getFilterChain().addLast("logger", new LoggingFilter());
        server.getSessionConfig().setReadBufferSize(this.bufferSize);
        server.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, this.idleTime);
        innerInit();
    }

    protected abstract void innerInit() throws Exception;

    @Override
    public void start() throws Exception {
        server.bind(new InetSocketAddress(ip, port));
        log.info("Server started.");
    }

    @Override
    public void stop() throws Exception {
        server.dispose();
    }

}
