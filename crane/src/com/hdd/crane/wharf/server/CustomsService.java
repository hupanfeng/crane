package com.hdd.crane.wharf.server;

import org.apache.mina.filter.codec.ProtocolCodecFilter;

import com.hdd.crane.server.BaseService;
import com.hdd.crane.wharf.server.codec.CustomsCodecFactory;
import com.hdd.crane.wharf.server.handler.CustomsServiceHandler;

/**
 * 
 * @author David Hu
 * @version 1.0.0
 * @since 2013-11-13
 */
public class CustomsService extends BaseService {

    @Override
    protected void innerInit() throws Exception {
        server.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CustomsCodecFactory()));
        server.setHandler(new CustomsServiceHandler());
    }
}
