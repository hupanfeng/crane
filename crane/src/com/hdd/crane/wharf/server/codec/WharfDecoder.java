package com.hdd.crane.wharf.server.codec;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.hdd.crane.memcache.MemCacheDto;

public class WharfDecoder implements ProtocolDecoder {
    private static AtomicLong sequence = new AtomicLong(0);

    @Override
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.hasArray()) {
            MemCacheDto memCacheDto = new MemCacheDto(session, in.array());
            memCacheDto.setId(sequence.incrementAndGet());
            out.write(memCacheDto);
        }
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
        // do nothing

    }

    @Override
    public void dispose(IoSession session) throws Exception {
        //do nothing
    }

}
