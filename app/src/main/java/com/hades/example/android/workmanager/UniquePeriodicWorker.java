package com.hades.example.android.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UniquePeriodicWorker extends Worker {
    private static final String TAG = UniquePeriodicWorker.class.getSimpleName();

    public UniquePeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork:id:" + getId() + ",tags:" + getTags().toString() + ",hashCode:" + hashCode());
        return Result.retry();
    }
}
