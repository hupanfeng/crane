package com.hdd.crane.logistics.client.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.hdd.crane.logistics.LogisticsMeta;
import com.hdd.crane.memcache.MemCacheDto;

public class LogisticsEncoder extends ProtocolEncoderAdapter {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if (null != message && message instanceof LogisticsMeta) {
            LogisticsMeta logisticsMeta = (LogisticsMeta) message;
            out.write(logisticsMeta.getBytes());
        } else if (null != message && message instanceof MemCacheDto) {
            MemCacheDto memCacheDto = (MemCacheDto) message;
            out.write(memCacheDto.getBytes());
        }
    }
}
