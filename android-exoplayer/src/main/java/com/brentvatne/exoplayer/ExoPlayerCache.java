package com.brentvatne.exoplayer;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class ExoPlayerCache extends ReactContextBaseJavaModule {

    private static SimpleCache instance = null;
    private static final String CACHE_KEY_PREFIX = "exoPlayerCacheKeyPrefix";
    private String TMP_EXPORT_PATH;

    public ExoPlayerCache(ReactApplicationContext reactContext) {
        super(reactContext);

        TMP_EXPORT_PATH = getReactApplicationContext().getCacheDir().toString() + "/video-tmp";
        File exportPath = new File(TMP_EXPORT_PATH);

        // Clear the temporary export files on launch to make sure this doesn't grow infinitely.
        if (exportPath.exists()) {
            for (File child: exportPath.listFiles()) {
                child.delete();
            }
        }
    }
    
    @Override
    public String getName() {
        return "ExoPlayerCache";
    }

    @ReactMethod
    public void exportVideo(final String url, final Promise promise) {
        Log.d(getName(), "exportVideo");

        Thread exportThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getName(), "Exporting...");
                Log.d(getName(), url);

                final Uri uri = Uri.parse(url);

                final SimpleCache downloadCache = VideoCache.getInstance().getSimpleCache();
                final DataSource dataSource = createDataSource(downloadCache);
                final DataSpec dataSpec = new DataSpec(uri, 0, C.LENGTH_UNSET, null);
                File targetFile = new File(TMP_EXPORT_PATH, uri.getLastPathSegment());

                // Create export dir if not exists.
                targetFile.getParentFile().mkdirs();


                // https://github.com/google/ExoPlayer/issues/5569
                try {
                     CacheUtil.getCached(
                        dataSpec,
                        downloadCache,
                        new ExoplayerCacheKeyFactory()
                    );

                    DataSourceInputStream inputStream = new DataSourceInputStream(createDataSource(downloadCache), dataSpec);
                    BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(targetFile), 64 * 1024);

                    try {
                        byte[] data = new byte[64 * 1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(data)) != C.RESULT_END_OF_INPUT) {
                            outStream.write(data, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        Log.d(getName(), "Write error");
                        e.printStackTrace();
                    } finally {
                        dataSource.close();
                        outStream.close();
                    }

                    Log.d(getName(), "Export succeeded");
                    Log.d(getName(), targetFile.getPath());

                    promise.resolve(targetFile.getPath());
                } catch (Exception e) {
                    Log.d(getName(), "Export error");
                    e.printStackTrace();
                    promise.reject(e);
                }
            }
        }, "export_thread");
        exportThread.start();
    }

    private CacheDataSource createDataSource(Cache cache) {
        return new CacheDataSourceFactory(cache, DataSourceUtil.getDefaultDataSourceFactory(
            getReactApplicationContext(),
            null,
            null
        )).createDataSource();
    }
}
