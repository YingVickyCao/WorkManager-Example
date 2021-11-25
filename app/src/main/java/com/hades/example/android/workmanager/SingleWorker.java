package com.hades.example.android.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Map;

/**
 * 单独的task
 */
public class SingleWorker extends Worker {
    private static final String TAG = "UploadWorker";

    public SingleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Worker的doWork用来执行后台任务，运行在子线程 - WorkerManager自带线程池中的线程。
        // WorkerManager自带线程池。每次执行任务时，优先使用线程池的thread，若没有，则创建并使用新thread，并放入线程池。
        // thread id:1990,thread name:pool-2-thread-1
        // thread id:1992,thread name:pool-2-thread-2
        // thread id:1993,thread name:pool-2-thread-3
        // thread id:1994,thread name:pool-2-thread-4
        // thread id:1990,thread name:pool-2-thread-1
        // thread id:1992,thread name:pool-2-thread-2
        Log.d(TAG, "doWork: thread id:" + Thread.currentThread().getId() + ",thread name:" + Thread.currentThread().getName());

        Data inputData = getInputData();
        Map<String, Object> keyValueMapOfData = inputData.getKeyValueMap();
        Log.d(TAG, "doWork: all data:" + keyValueMapOfData.toString());
        Log.d(TAG, "doWork: key:k1,value:" + inputData.getString("k1"));

        try {
            // Do the heavy work
            for (int i = 0; i < 3; i++) {
                Log.d(TAG, "doWork: i=" + i);
                Thread.sleep(1000);
            }
            Log.d(TAG, "doWork:success ");

            // Result 有3种。
            // return Result.success();

            Data.Builder outputDataBuilder = new Data.Builder();
            outputDataBuilder.putBoolean("count_task_is_success", true);
            return Result.success(outputDataBuilder.build());
//        return Result.retry();
        } catch (Exception exception) {
            Log.d(TAG, "doWork:fail ");

//            return Result.failure();

            Data.Builder outputDataBuilder = new Data.Builder();
            outputDataBuilder.putBoolean("count_task_is_success", true);
            return Result.failure(outputDataBuilder.build());
        }
    }
}
