package com.hdd.crane.server;

public interface Service {

    public void setIP(String IP) throws Exception;

    public void setPort(int port) throws Exception;

    public void setBufferSize(int bufferSize) throws Exception;

    public void setIdleTime(int idleTime) throws Exception;

    public void setServerType(String serverType) throws Exception;

    public void init() throws Exception;

    public void start() throws Exception;

    public void stop() throws Exception;

}
