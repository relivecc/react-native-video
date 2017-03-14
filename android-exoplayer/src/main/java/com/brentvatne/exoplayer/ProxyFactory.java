package com.brentvatne.exoplayer;

import android.content.Context;
import com.danikula.videocache.HttpProxyCacheServer;

/**
 * <strong>Not thread-safe</strong> {@link HttpProxyCacheServer} factory that returns single instance of proxy.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class ProxyFactory {
 
    private static HttpProxyCacheServer sharedProxy;
 
    private ProxyFactory() {
    }
 
    public static HttpProxyCacheServer getProxy(Context context) {
        return sharedProxy == null ? (sharedProxy = newProxy(context)) : sharedProxy;
    }
 
    private static HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer.Builder(context)
            .maxCacheSize(100 * 1024 * 1024)    // 100 MB for cache
            .maxCacheFilesCount(5)              // save max 5 videos
            .build();
    }
}