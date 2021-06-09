package com.brentvatne.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ExoPlayerCache extends ReactContextBaseJavaModule {

    private static SimpleCache instance = null;
    private static final String CACHE_KEY_PREFIX = "exoPlayerCacheKeyPrefix";

    public ExoPlayerCache(ReactApplicationContext reactContext) {
        super(reactContext);
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
                final DataSpec dataSpec = new DataSpec(uri, 0, C.LENGTH_UNSET, null);
                final SimpleCache downloadCache = ExoPlayerCache.getInstance(getReactApplicationContext());
                CacheKeyFactory cacheKeyFactory = ds -> CACHE_KEY_PREFIX + "." + CacheUtil.generateKey(ds.uri);;

                try {
                    CacheUtil.getCached(
                        dataSpec,
                        downloadCache,
                        cacheKeyFactory
                    );

                    DataSourceInputStream inputStream = new DataSourceInputStream(createDataSource(downloadCache), dataSpec);

                    File targetFile = new File(ExoPlayerCache.getCacheDir(getReactApplicationContext()) + "/" + uri.getLastPathSegment());
                    OutputStream outStream = new FileOutputStream(targetFile);

                    byte[] buffer = new byte[8 * 1024];
                    int bytesRead;
                    try {
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, bytesRead);
                            // TODO Add onProgress() callback here
                        }
                    } catch (IOException e) {
                        // TODO this exception should not be thrown
                        Log.d(getName(), "Read error");
                        e.printStackTrace();

                        throw e;
                    }

                    CacheUtil.getCached(
                        dataSpec,
                        downloadCache,
                        cacheKeyFactory
                    );

                    if (!targetFile.exists()) {
                        throw new Exception("Target file bytes (" + bytesRead + ") not present after writing `" + targetFile.getPath() + "`");
                    }

                    Log.d(getName(), "Export succeeded");
                    Log.d(getName(), targetFile.getPath());

                    WritableMap result =  Arguments.createMap();
                    result.putString("path", targetFile.getPath());
                    result.putDouble("bytesRead", inputStream.bytesRead());
                    result.putDouble("bytesWritten", targetFile.length());

                    promise.resolve(result);
                } catch (Exception e) {
                    Log.d(getName(), "Export error");
                    e.printStackTrace();

                    String className = e.getClass().getSimpleName();
                    promise.reject(className, className + ": " + e.getMessage());
                    return;
                }
            }
        }, "export_thread");
        exportThread.start();
    }

    @ReactMethod
    public void exportVideoImproved(final String url, final Promise promise) {
        Log.d(getName(), "exportVideoImproved");

        Thread exportThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getName(), "Exporting...");
                Log.d(getName(), url);
                final Uri uri = Uri.parse(url);
                final DataSpec dataSpec = new DataSpec(uri, 0, C.LENGTH_UNSET, null);
                final SimpleCache downloadCache = ExoPlayerCache.getInstance(getReactApplicationContext());
                CacheKeyFactory cacheKeyFactory = ds -> CACHE_KEY_PREFIX + "." + CacheUtil.generateKey(ds.uri);;
                BufferedOutputStream outStream = null;

                try {
                    try {
                        CacheUtil.getCached(
                            dataSpec,
                            downloadCache,
                            cacheKeyFactory
                        );

                        DataSourceInputStream inputStream = new DataSourceInputStream(createDataSource(downloadCache), dataSpec);
                        File targetFile = new File(ExoPlayerCache.getCacheDir(getReactApplicationContext()) + "/" + uri.getLastPathSegment());

                        // https://stackoverflow.com/questions/14378898/fileoutputstream-does-not-create-file
                        outStream = new BufferedOutputStream(new FileOutputStream(targetFile));

                        if (!targetFile.exists()) {
                            boolean parentExists = targetFile.getParentFile() != null && targetFile.getParentFile().exists();
                            boolean parentWritable = targetFile.getParentFile() != null && targetFile.getParentFile().canWrite();
                            boolean targetWriteable = targetFile.canWrite();
                            String parentLabel = "parent (" + (parentWritable ? "is writable" : "not writable") + ")";
                            String targetLabel = "target (" + (targetWriteable ? "is writable" : "not writable") + ")";

                            if (!parentExists) {
                                throw new Exception(targetLabel + " and " + parentLabel + " not present after stream creation `" + targetFile.getPath() + "`");
                            } else {
                                throw new Exception(targetLabel + " not present in " + parentLabel + " after stream creation `" + targetFile.getPath() + "`");
                            }
                        }

                        byte[] buffer = new byte[8 * 1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, bytesRead);
                            // TODO Add onProgress() callback here
                        }

                        CacheUtil.getCached(
                            dataSpec,
                            downloadCache,
                            cacheKeyFactory
                        );

                        if (!targetFile.exists()) {
                            throw new Exception("Target file bytes (" + bytesRead + ") not present after writing `" + targetFile.getPath() + "`");
                        }

                        Log.d(getName(), "Export succeeded");
                        Log.d(getName(), targetFile.getPath());

                        WritableMap result =  Arguments.createMap();
                        result.putString("path", targetFile.getPath());
                        result.putDouble("bytesRead", inputStream.bytesRead());
                        result.putDouble("bytesWritten", targetFile.length());

                        promise.resolve(result);
                    } finally {
                        if (outStream != null) {
                            // Flush streams and release system resources
                            outStream.close();
                        }
                    }

                } catch (Exception e) {
                    Log.d(getName(), "Export error");
                    e.printStackTrace();

                    String className = e.getClass().getSimpleName();
                    promise.reject(className, className + ": " + e.getMessage());
                    return;
                }
            }
        }, "export_thread");
        exportThread.start();
    }

    public static SimpleCache getInstance(Context context) {
        if(instance == null) {
            instance = new SimpleCache(new File(ExoPlayerCache.getCacheDir(context)), new NoOpCacheEvictor());
        }
        return instance;
    }

    private static String getCacheDir(Context context) {
        return context.getCacheDir().toString() + "/video";
    }

    private DataSource createDataSource(Cache cache) {
        return new CacheDataSourceFactory(cache, DataSourceUtil.getDefaultDataSourceFactory(
            getReactApplicationContext(),
            null,
            null
        )).createDataSource();
    }

}
