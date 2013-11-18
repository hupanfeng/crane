package com.hdd.crane.memcache;

import org.apache.mina.core.session.IoSession;

import com.hdd.crane.logistics.LogisticsMeta;

public class MemCacheDto {

    private IoSession session;
    private volatile boolean isHeaderParsed = false;
    private byte[] bytes;
    private Header header;

    private LogisticsMeta logistics;

    public MemCacheDto() {
    }

    public MemCacheDto(IoSession session, byte[] bytes) {
        this.session = session;
        this.bytes = bytes;
        this.header = new Header();
    }

    public void parseHeader() {
        if (!isHeaderParsed) {
            header.parseHeader(bytes);
            isHeaderParsed = true;
        }
    }

    public String getKey() {
        parseHeader();
        return header.getKey();
    }

    public class Header {

        private byte[] header;

        private String key;

        private short magic;
        private short opcode;
        private int keyLength;

        private short extrasLength;
        private short dataType;
        private int reserved;
        private int status;
        private long totalBodyLength;
        private long opaque;
        private long cas;

        public Header() {
        }

        public Header(byte[] bytes) {
            header = bytes;
        }

        public void parseHeader(byte[] bytes) {
            if (bytes.length < 24) {

            }

        }

        public void parseExtras() {

        }

        public void parseValue() {

        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public short getMagic() {
            return magic;
        }

        public void setMagic(short magic) {
            this.magic = magic;
        }

        public short getOpcode() {
            return opcode;
        }

        public void setOpcode(short opcode) {
            this.opcode = opcode;
        }

        public int getKeyLength() {
            return keyLength;
        }

        public void setKeyLength(int keyLength) {
            this.keyLength = keyLength;
        }

        public short getExtrasLength() {
            return extrasLength;
        }

        public void setExtrasLength(short extrasLength) {
            this.extrasLength = extrasLength;
        }

        public short getDataType() {
            return dataType;
        }

        public void setDataType(short dataType) {
            this.dataType = dataType;
        }

        public int getReserved() {
            return reserved;
        }

        public void setReserved(int reserved) {
            this.reserved = reserved;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public long getTotalBodyLength() {
            return totalBodyLength;
        }

        public void setTotalBodyLength(long totalBodyLength) {
            this.totalBodyLength = totalBodyLength;
        }

        public long getOpaque() {
            return opaque;
        }

        public void setOpaque(long opaque) {
            this.opaque = opaque;
        }

        public long getCas() {
            return cas;
        }

        public void setCas(long cas) {
            this.cas = cas;
        }

        public byte[] getHeader() {
            return header;
        }

        public void setHeader(byte[] header) {
            this.header = header;
        }
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

    public boolean isHeaderParsed() {
        return isHeaderParsed;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public LogisticsMeta getLogistics() {
        return logistics;
    }

    public void setLogistics(LogisticsMeta logistics) {
        this.logistics = logistics;
    }

}
