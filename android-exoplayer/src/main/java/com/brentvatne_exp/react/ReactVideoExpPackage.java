package com.brentvatne_exp.react;

import com.brentvatne_exp.exoplayer.DefaultReactExoplayerConfig;
import com.brentvatne_exp.exoplayer.ReactExoplayerConfig;
import com.brentvatne_exp.exoplayer.ReactExoplayerViewManager;
import com.brentvatne_exp.exoplayer.ExoPlayerCache;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReactVideoExpPackage implements ReactPackage {

    private ReactExoplayerConfig config;

    public ReactVideoExpPackage() {
    }

    public ReactVideoExpPackage(ReactExoplayerConfig config) {
        this.config = config;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new ExoPlayerCache(reactContext));

        return modules;
    }

    // Deprecated RN 0.47
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }


    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        if (config == null) {
            config = new DefaultReactExoplayerConfig(reactContext);
        }
        return Collections.singletonList(new ReactExoplayerViewManager(config));
    }
}
