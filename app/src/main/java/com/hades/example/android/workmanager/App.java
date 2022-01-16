package com.hades.example.android.workmanager;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import java.util.concurrent.Executors;

/*
Enable Logging
Step 2 : Provide custom WorkManager Configuration

Step 3 : see logs with log-tag prefix WM-
adb shell dumpsys jobscheduler > log.txt
 */
public class App extends Application implements Configuration.Provider {
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        // WorkManager will be init when call WorkManager,getInstance(Context) rather than auto at application startup.
        return new Configuration.Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .setExecutor(Executors.newFixedThreadPool(8))
                .build();
    }
}
