package com.hdd.crane.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseServer implements Server {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected List<Service> services;

    public BaseServer() {
        this.services = Collections.synchronizedList(new ArrayList<Service>());
    }

    @Override
    public void addService(Service s) {
        this.services.add(s);
    }

    @Override
    public void init() throws Exception {
        log.info("Begin to initialize server... ");
        for (Service service : services) {
            service.init();
        }
        log.info("Server initialized.");
    }

    @Override
    public void start() throws Exception {
        for (Service service : services) {
            service.start();
        }
    }

    @Override
    public void stop() throws Exception {
        for (Service service : services) {
            service.stop();
        }
    }
}
