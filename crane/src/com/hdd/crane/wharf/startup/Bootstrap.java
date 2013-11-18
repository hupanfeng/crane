/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hdd.crane.wharf.startup;

import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hdd.crane.config.Configuration;
import com.hdd.crane.config.builder.XMLConfigBuilder;
import com.hdd.crane.io.Resources;
import com.hdd.crane.server.Server;

/**
 * Bootstrap loader for crane.
 * 
 * @author David Hu
 * @version 1.0
 * @since 2013-11-12
 */
public final class Bootstrap {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);
    private static Server server;
    private static Configuration confguration;

    public static void main(String args[]) {
        try {
            init();
        } catch (Exception e) {
            log.error("Wharf init fail.", e);
            System.exit(1);
        }
        try {
            start();
        } catch (Exception e) {
            log.error("Wharf start fail.", e);
            System.exit(2);
        }
    }

    public static void init() throws Exception {
        String resource = "crane-wharf-config.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        XMLConfigBuilder builder = new XMLConfigBuilder(reader);
        confguration = builder.parse();
        server = confguration.getServer();
        server.init();
    }

    /**
     * Start the crane daemon.
     */
    public static void start() throws Exception {
        server.start();
    }

    /**
     * Stop the crane Daemon.
     */
    public void stop() throws Exception {
        server.stop();
    }

    /**
     * Stop the standalone server.
     */
    public void stopServer() throws Exception {

    }

    /**
     * Stop the standalone server.
     */
    public void stopServer(String[] arguments) throws Exception {

    }

    public void destroy() {

    }

}
