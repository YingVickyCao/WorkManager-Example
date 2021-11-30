package com.hades.example.android.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ProgressWorker extends Worker {
    private static final String TAG = ProgressWorker.class.getSimpleName();
    public static final String PROGRESS = "PROGRESS";
    private static final long DELAY_MS = 1000L;

    public ProgressWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.e(TAG, "ProgressWorker: progress:999");
        setProgressAsync(new Data.Builder().putInt(PROGRESS, 999).build());
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            for (int i = 1; i <= 10; i++) {
                Thread.sleep(DELAY_MS);
                Log.e(TAG, "doWork: progress:" + (i * 10));
                setProgressAsync(new Data.Builder().putInt(PROGRESS, i * 10).build());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.failure();
    }
}
