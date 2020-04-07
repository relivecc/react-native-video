package com.brentvatne.exoplayer;

import com.google.android.exoplayer2.upstream.cache.SimpleCache;

public class VideoCache {

    private static volatile VideoCache instance;
    private static SimpleCache cache;

    private VideoCache() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance()");
        }
    }

    public void setSimpleCache(SimpleCache cache) {
        this.cache = cache;
    }

    public SimpleCache getSimpleCache() {
        if (this.cache == null) {
            throw new RuntimeException("Tried to access video cache but no cache is set");
        }

        return this.cache;
    }

    public static VideoCache getInstance() {
        if (instance == null) {
            synchronized (VideoCache.class) {
                if (instance == null) instance = new VideoCache();
            }
        }

        return instance;
    }
}
