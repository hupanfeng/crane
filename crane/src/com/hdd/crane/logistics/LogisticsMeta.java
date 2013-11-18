package com.hdd.crane.logistics;

import org.apache.mina.core.session.IoSession;

public class LogisticsMeta {

    private String ip;
    private int port;
    
    private IoSession session;

    public String getIdentity() {
        return ip + ":" + port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

   

}
