package com.ameerhamza6733.directmessagesaveandrepost;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.downloader.PRDownloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by AmeerHamza on 11/20/2017.
 */


public class application extends Application {
    public static RefWatcher getRefWatcher(Context context) {
        application application = (application) context.getApplicationContext();
        return application.refWatcher;
    }
    private RefWatcher refWatcher;
    @Override
    public void onCreate() {
        super.onCreate();
        setupLeakCanary();
        PRDownloader.initialize(getApplicationContext());

        // Normal app init code...
    }
    protected RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }
}


