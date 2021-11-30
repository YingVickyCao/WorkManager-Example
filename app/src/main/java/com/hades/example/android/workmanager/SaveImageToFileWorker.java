package com.hades.example.android.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SaveImageToFileWorker extends Worker {
    public static final String TAG = "SaveImageToFileWorker";

    public SaveImageToFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (MainActivity.ms != 0) {
            long duration = System.currentTimeMillis() - MainActivity.ms;
            Log.e(TAG, "doWork: retry interval seconds is :" + duration / 1000);
        } else {
            Log.e(TAG, "doWork: ");
        }
        MainActivity.ms = System.currentTimeMillis();
//        for (int i = 0; i < 3; i++) {
////            Log.e(TAG, "doWork: i=" + i);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return Result.success();
        return Result.retry();
    }
}
