package com.hades.example.android.workmanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BazWorker extends Worker {
    public static final String BAZ_KEY = "BAZ_KEY";

    public BazWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        int num = inputData.getInt(BAZ_KEY, 0);
        int sum = inputData.getInt(MainActivity.SUM_KEY, 0);
        int result = sum + num;
        Data outputData = new Data.Builder()
                .putInt(MainActivity.SUM_KEY, result)
                .build();
        return Result.success(outputData);
    }
}
