package com.hdd.crane.server;

public interface Server {

    public void addService(Service s) throws Exception;

    public void init() throws Exception;

    public void start() throws Exception;

    public void stop() throws Exception;
}
