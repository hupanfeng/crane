package com.hdd.crane.config;

import java.util.Properties;

import com.hdd.crane.server.Server;

public class Configuration {

    private int monitorInterval;
    private Properties variables = new Properties();
    private Server server;

    public Properties getVariables() {
        return variables;
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    public int getMonitorInterval() {
        return monitorInterval;
    }

    public void setMonitorInterval(int monitorInterval) {
        this.monitorInterval = monitorInterval;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

}
