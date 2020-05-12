package com.brentvatne_exp.exoplayer;

import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;

import java.net.URL;

public class ExoplayerCacheKeyFactory implements CacheKeyFactory {
    @Override
    public String buildCacheKey(DataSpec dataSpec) {
        String uri = dataSpec.uri.toString();

        // Strip query parameters for cache key since this breaks lookup.
        String queryStrippedURI = uri.substring(0, uri.indexOf("?"));

        return uri;
    }
}
