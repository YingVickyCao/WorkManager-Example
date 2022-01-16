package com.hades.example.android.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import io.reactivex.Single;

public class SingleWorker4RxJava extends RxWorker {
    private static final String TAG = SingleWorker4RxJava.class.getSimpleName();

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public SingleWorker4RxJava(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        Log.d(TAG, "constructor: thread id:" + Thread.currentThread().getId() + ",thread name:" + Thread.currentThread().getName());
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        // createWork: thread id:2,thread name:main
        // TODO: 2021/12/3
          Log.d(TAG, "createWork: thread id:" + Thread.currentThread().getId() + ",thread name:" + Thread.currentThread().getName());
        return Single.just(Result.success());
    }


}
