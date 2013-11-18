package com.hdd.crane.config;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hdd.crane.config.builder.XMLConfigBuilder;
import com.hdd.crane.io.Resources;

/**
 * 
 * @author 胡攀峰(David Hu)
 * @博客 http://blog.csdn.net/hupanfeng
 * @version 1.0.0
 * @since 2013年11月12日
 */
public class ConfigurationMonitor {
    private static Logger log = LoggerFactory.getLogger(ConfigurationMonitor.class);

    private Configuration configuration;
    private String resource;
    private int monitorInterval = 30;
    private long lastModifedTime = Long.MIN_VALUE;
    private long nextCheckTime = Long.MIN_VALUE;
    private MonitorThread monitor;

    public ConfigurationMonitor(String resource) {
        this.resource = resource;
        monitor = new MonitorThread();
    }

    public void start() {
        monitor.start();
    }

    private class MonitorThread extends Thread {
        @Override
        public void run() {
            build();
        }
    }

    private void build() {
        while (true) {
            if (System.currentTimeMillis() >= nextCheckTime) {
                try {
                    File file = Resources.getResourceAsFile(resource);
                    if (file.lastModified() > lastModifedTime) {
                        configuration = null;
                        Reader reader = null;
                        try {
                            reader = Resources.getResourceAsReader(resource);
                            XMLConfigBuilder parser = new XMLConfigBuilder(reader);
                            configuration = parser.parse();
                            lastModifedTime = file.lastModified();
                        } catch (Exception e) {
                            log.error("Parser Configuration fail", e);
                        } finally {
                            if (null != reader) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    // Intentionally ignore.
                                }
                            }
                        }
                    }
                } catch (IOException ie) {
                    log.error("Read file fail", ie);
                }
                nextCheckTime = System.currentTimeMillis() + monitorInterval * 1000;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Intentionally ignore.
                }
            }

        }
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
