package com.hdd.crane.wharf.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.hdd.crane.memcache.MemCacheDto;

public class Conveyor {
    public static BlockingQueue<MemCacheDto> requestQueue = new LinkedBlockingQueue<MemCacheDto>(10000);
    public static BlockingQueue<MemCacheDto> responseQueue = new LinkedBlockingQueue<MemCacheDto>(10000);

}
