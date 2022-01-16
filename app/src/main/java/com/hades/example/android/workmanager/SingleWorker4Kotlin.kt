package com.hades.example.android.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//class SingleWorker4Kotlin(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
class SingleWorker4Kotlin : CoroutineWorker {
    private val TAG = "SingleWorker4Kotlin"

    // 重写构造函数
    constructor(appContext: Context, params: WorkerParameters) : super(appContext, params) {
        Log.d(TAG, "constructor: thread id:" + Thread.currentThread().id + ",thread name:" + Thread.currentThread().name)
        // constructor: thread id:1048,thread name:pool-3-thread-3
//        Log.d(TAG, "constructor: context:" + appContext) // Context is app context
    }

    /*
    override suspend fun doWork(): Result {
        // doWork:id:5d485be9-c592-472d-8790-75f3fe9df645,tags:[com.hades.example.android.workmanager.SingleWorker4Kotlin],hashCode:68951799

        // addTag
        // doWork:id:8e2d9570-7871-44a8-aaa9-3adf78a7384a,tags:[com.hades.example.android.workmanager.SingleWorker4Kotlin, SingleWorker4Kotlin],hashCode:68951799
        Log.d(TAG, "doWork:id:" + id + ",tags:" + tags.toString() + ",hashCode:" + hashCode())
        // TODO: doWork: thread id:1024,thread name:DefaultDispatcher-worker-1
        // TODO:default runs on Dispatchers.Default
        Log.d(TAG, "doWork: thread id:" + Thread.currentThread().id + ",thread name:" + Thread.currentThread().name)
        return Result.success()
    }
    */

    // TODO: 2021/12/3
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            // TODO: doWork: thread id:1024,thread name:DefaultDispatcher-worker-1
            // TODO:default runs on Dispatchers.Default
            Log.d(TAG, "doWork: thread id:" + Thread.currentThread().id + ",thread name:" + Thread.currentThread().name)
            return@withContext Result.success()
        }
    }
}