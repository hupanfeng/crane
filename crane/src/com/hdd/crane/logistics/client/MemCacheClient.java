package com.hdd.crane.logistics.client;

import org.apache.mina.filter.codec.ProtocolCodecFilter;

import com.hdd.crane.client.BaseClient;
import com.hdd.crane.logistics.client.codec.LogisticsCodecFactory;
import com.hdd.crane.logistics.client.handler.MemCacheClientHandler;

public class MemCacheClient extends BaseClient{
    @Override
    protected void innerInit() throws Exception {
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new LogisticsCodecFactory()));
        connector.setHandler(new MemCacheClientHandler());
    }
}
