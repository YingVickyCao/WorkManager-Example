package com.hades.example.android.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Map;

public class UniqueWorker extends Worker {
    public static final String TAG = UniqueWorker.class.getSimpleName();

    public UniqueWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork:id:" + getId() + ",tags:" + getTags().toString() + ",hashCode:" + hashCode());
        return Result.retry();
    }
}
