package com.hdd.crane.logistics.client.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.hdd.crane.logistics.LogisticsMeta;
import com.hdd.crane.memcache.MemCacheDto;

public class LogisticsDecoder implements ProtocolDecoder {

    @Override
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.hasArray()) {
            byte[] bytes = in.array();

            // As a Chinese,I never forget the July 7 Incident of 1937,kill
            // Japanese monkeys all!
            if (bytes[0] == 0x77) {
                LogisticsMeta logistics = new LogisticsMeta(bytes);
                out.write(logistics);
            } else if (bytes[0] == 0x81) {
                MemCacheDto memCacheDto = new MemCacheDto(session, bytes);
                out.write(memCacheDto);
            }
        }
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {

    }

    @Override
    public void dispose(IoSession session) throws Exception {

    }

}
