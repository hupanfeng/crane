package com.hdd.crane.logistics;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.mina.core.session.IoSession;

import com.hdd.common.util.IPUtil;
import com.hdd.common.util.UInteger;

public class LogisticsMeta {

    private byte[] bytes;
    private byte[] ip;
    private byte[] port;

    private final CopyOnWriteArraySet<IoSession> sessions = new CopyOnWriteArraySet<IoSession>();

    public LogisticsMeta(byte[] bytes) {
        this.bytes = bytes;
    }

    public void addSession(IoSession session) {
        sessions.add(session);
    }

    public void removeSession(IoSession session) {
        sessions.remove(session);
    }

    public boolean isAvailable() {
        return !sessions.isEmpty();
    }

    public IoSession getWriteIdleSession() {
        for (IoSession session : sessions) {
            if (session.isWriterIdle()) {
                return session;
            }
        }
        return null;
    }

    public String getIdentity() {
        try {
            return IPUtil.byteToIP(getIp()) + ":" + new UInteger(getPort());
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public byte[] getIp() {
        if (null == ip) {
            if (bytes[1] == 0x04) {
                // ip = new byte[4];
                ip = Arrays.copyOfRange(bytes, 2, 6);
                // System.arraycopy(bytes, 2, ip, 0, 4);
            } else if (bytes[1] == 0x06) {
                // ip = new byte[16];
                ip = Arrays.copyOfRange(bytes, 2, 18);
                // System.arraycopy(bytes, 2, ip, 0, 16);
            }
        }
        return ip;
    }

    public byte[] getPort() {
        if (null == port) {
            // port = new byte[4];
            if (bytes[1] == 0x04) {
                // System.arraycopy(bytes, 6, port, 0, 4);
                port = Arrays.copyOfRange(bytes, 6, 10);
            } else if (bytes[1] == 0x04) {
                // System.arraycopy(bytes, 18, ip, 0, 4);
                port = Arrays.copyOfRange(bytes, 18, 22);
            }
        }
        return port;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

}
