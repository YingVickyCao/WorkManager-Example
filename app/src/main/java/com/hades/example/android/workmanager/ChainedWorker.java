package com.hades.example.android.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * 一个Worker的输出，是下一个 Worker 的输入
 */
public class ChainedWorker extends Worker {
    private static final String TAG = "ChainedWorker";

    // Context is Application
    public ChainedWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data inputData = getInputData();
            int num = inputData.getInt("num", 0);
            int num2 = inputData.getInt("num2", 0);
            int num3 = inputData.getInt("num3", 0);
            Log.d(TAG, "doWork: num:" + num + ",num2:" + num2 + ",num3:" + num3);
            int sum = inputData.getInt("sum", 0);
            Log.d(TAG, "doWork: sum:" + sum);

            Data output = new Data.Builder()
                    .putInt("sum", sum + num)
                    .build();
            return Result.success(output);
        } catch (Exception exception) {
            return Result.failure();
        }
    }
}
