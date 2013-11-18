package com.hdd.crane.wharf.server.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.hdd.crane.memcache.MemCacheDto;

public class WharfEncoder extends ProtocolEncoderAdapter {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if (null != message && message instanceof MemCacheDto) {
            MemCacheDto memCacheDto = (MemCacheDto) message;
            out.write(memCacheDto.getBytes());
        }
    }
}
