package com.hdd.crane.wharf.server;

import com.hdd.crane.server.BaseService;
import com.hdd.crane.wharf.server.handler.WharfServiceHandler;

/**
 * 
 * @author David Hu
 * @version 1.0.0
 * @since 2013-11-12
 */
public class WharfService extends BaseService {

    @Override
    protected void innerInit() throws Exception {
        //server.getFilterChain().addLast("codec", new ProtocolCodecFilter(new WharfCodecFactory()));
        server.setHandler(new WharfServiceHandler());
    }
}
