package com.hdd.crane.logistics;

public class LogisticsStatus {

    private volatile boolean isRegisted = false;
    private static LogisticsStatus instance = new LogisticsStatus();

    private LogisticsStatus() {

    }

    public static LogisticsStatus getInstance() {
        return instance;
    }

    public boolean isRegisted() {
        return isRegisted;
    }

}
