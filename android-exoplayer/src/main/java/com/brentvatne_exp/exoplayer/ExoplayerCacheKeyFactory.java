package com.brentvatne_exp.exoplayer;

import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory;

public class ExoplayerCacheKeyFactory implements CacheKeyFactory {
    @Override
    public String buildCacheKey(DataSpec dataSpec) {
        String uri = dataSpec.uri.toString();

        // Strip query parameters for cache key since this breaks lookup.
        int queryIndex = uri.indexOf("?");
        if (queryIndex != -1) {
            uri = uri.substring(0, queryIndex);
        }

        return uri;
    }
}
