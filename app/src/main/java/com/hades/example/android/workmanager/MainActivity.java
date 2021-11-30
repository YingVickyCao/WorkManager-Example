package com.hades.example.android.workmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import androidx.work.WorkRequest;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final String UNIQUE_WORK_NAME_OF_SUM = "SUM_TASK";
    private final String UNIQUE_WORK_1 = "UNIQUE_WORK_1";
    private final String UNIQUE_WORK_2 = "UNIQUE_WORK_2";
    private final String UNIQUE_UNIQUE_PERIODIC_WORK_1 = "UNIQUE_UNIQUE_PERIODIC_WORK_1";
    public static final String SUM_KEY = "SUM_KEY";
    private final String WORK_SUM_TAG_1 = "SUM_DATA_1";
    private final String WORK_SUM_TAG_2 = "SUM_DATA_2";
    private final String WORK_SUM_TAG_3 = "SUM_DATA_3";
    private UUID mWorkIdOfSumData1;
    public static final String WORK_REQUEST_TAG_PERIODIC_1 = "PERIODIC_WORK_REQUEST_1";
    public static final String WORK_REQUEST_TAG_2 = "WORK_REQUEST_2";


    public static long ms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.scheduleOneTimeWork).setOnClickListener(view -> scheduleOneTimeWork());
        findViewById(R.id.uniqueWork).setOnClickListener(view -> uniqueWork());
        findViewById(R.id.scheduleExpeditedWork).setOnClickListener(view -> scheduleExpeditedWork());
        findViewById(R.id.schedulePeriodicWork).setOnClickListener(view -> schedulePeriodicWork());
        findViewById(R.id.cancelWork).setOnClickListener(view -> cancelWork());
        findViewById(R.id.observeWork).setOnClickListener(view -> observeWork());
    }

    private void scheduleOneTimeWork() {
//        singleWork();
//        chainedWork();
    }

    private void uniqueWork() {
        // One-Time Work
        {
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UniqueWorker.class)
                    .addTag(UniqueWorker.TAG)
                    .build();
            // ExistingWorkPolicy, 告诉WorkManager当遇到没有完成的work时该怎么处理它？
            WorkManager.getInstance(this).enqueueUniqueWork(UNIQUE_WORK_1, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);
        }

        // PeriodicWork
        {
            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(UniquePeriodicWorker.class, 15, TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(UNIQUE_UNIQUE_PERIODIC_WORK_1, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
        }
    }

    private void observeWork() {
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ProgressWorker.class)
                .addTag(UniqueWorker.TAG)
                .build();
        WorkManager.getInstance(this).enqueueUniqueWork(UNIQUE_WORK_2, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);

        // TODO: 2021/11/30  observe work
//        ListenableFuture<WorkInfo> workInfo1 = WorkManager.getInstance(this).getWorkInfoById(oneTimeWorkRequest.getId());
//        ListenableFuture<List<WorkInfo>> workInfo2 = WorkManager.getInstance(this).getWorkInfosForUniqueWork(UNIQUE_WORK_1);
//        ListenableFuture<List<WorkInfo>> workInfo3 = WorkManager.getInstance(this).getWorkInfosByTag(UniqueWorker.TAG);

        // LiveData<WorkInfo>
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(oneTimeWorkRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null) {
                    Data progressData = workInfo.getProgress();
                    int progress = progressData.getInt(ProgressWorker.PROGRESS, 0);

//                    Log.e(TAG, "onChanged: progress:" + progress + ",state:" + workInfo.getState() + ",id:" + workInfo.getId() + ",tag:" + workInfo.getTags());
                    /*
                       E/ProgressWorker: ProgressWorker: progress:999
                        E/MainActivity: onChanged: progress:0,state:ENQUEUED
                        E/MainActivity: onChanged: progress:999,state:RUNNING
                        E/ProgressWorker: doWork: progress:10
                        E/MainActivity: onChanged: progress:10,state:RUNNING
                        ...
                        E/ProgressWorker: doWork: progress:90
                        E/MainActivity: onChanged: progress:90,state:RUNNING
                        E/ProgressWorker: doWork: progress:100
                        E/MainActivity: onChanged: progress:0,state:SUCCEEDED
                        /
                        E/MainActivity: onChanged: progress:0,state:FAILED
                     */
                    // If State.isFinished() ,WorkManager 不发布 progress。
                    Log.e(TAG, "onChanged: progress:" + progress + ",state:" + workInfo.getState());
                    Toast.makeText(MainActivity.this, String.valueOf(progress), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Work queries to support complex filters
        // 同种之间是OR，不同种之间是AND. Example : tag AND state AND ID AND name, (name1 OR name2 OR name3)
//        WorkQuery workQuery = WorkQuery.Builder
//                .fromTags(Arrays.asList(UniqueWorker.TAG))
//                .addStates(Arrays.asList(WorkInfo.State.FAILED, WorkInfo.State.CANCELLED))
//                .addIds(Arrays.asList(oneTimeWorkRequest.getId()))
//                .addUniqueWorkNames(Arrays.asList(UNIQUE_WORK_1))
//                .build();
//        ListenableFuture<List<WorkInfo>> workInfo5 = WorkManager.getInstance(this).getWorkInfos(workQuery);
    }

    private void scheduleExpeditedWork() {
        // TODO: 2021/11/30
    }

    private void schedulePeriodicWork() {
        Log.e(TAG, "schedulePeriodicWork: ");
        /**
         * repeat interval : 最小间隔时间为 15 minutes {@link PeriodicWorkRequest#MIN_PERIODIC_INTERVAL_MILLIS} ，同JobScheduler
         * flex Interval ： 允许执行的间隔时间最小为5分钟 {@link PeriodicWorkRequest#MIN_PERIODIC_FLEX_MILLIS}。不是执行时间。
         * **/
        // Interval duration lesser than minimum allowed value; Changed to 900000
//        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SaveImageToFileWorker.class, 15, TimeUnit.SECONDS)

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SaveImageToFileWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueue(request);
        WorkManager.getInstance(this).getWorkInfosByTag(WORK_REQUEST_TAG_PERIODIC_1);
    }

    // Submit work request to system
    private void singleWork() {
        // thread id:2,thread name:main
        Log.d(TAG, "submitWorkRequestToSystem: thread id:" + Thread.currentThread().getId() + ",thread name:" + Thread.currentThread().getName());

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("k1", "value1");
        dataBuilder.putInt("k2", 2);
        dataBuilder.putBoolean("k3", true);

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .setRequiresDeviceIdle(false)
//                .setRequiresStorageNotLow(false)
                .build();
        WorkRequest workRequest = new OneTimeWorkRequest.Builder(SingleWorker.class)
                .setInputData(dataBuilder.build())
                // 直到所有限制条件满足，才执行，否则终止本次执行，等待下次重试。
                .setConstraints(constraints)
                //  没有限制或所有限制条件满足，当Work进入系统队列，默认系统立即执行work。可以设置 InitialDelay 时间，阻止立即执行。
//                .setInitialDelay(0, TimeUnit.MINUTES)
                // 当Work 失败返回Result.retry()表示重试时.(1)retry interval 递增规律：EXPONENTIAL：10，20，40，80；LINEAR：10，20，30，40。
                // (3)重试等待时间最短为10s，最长为1小时。实际等待时间大于或等于设定等待时间。
                // WM-WorkSpec: Backoff delay duration exceeds maximum value
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MAX_BACKOFF_MILLIS, TimeUnit.DAYS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                // 每个WorkRequest有唯一标识符，用来取消Work或observer work's progress. 每个WorkRequest包含一个set of tag strings。
                .addTag(WORK_REQUEST_TAG_2)
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);

        // observe work
        // TODO: 2021/11/29
        ListenableFuture<WorkInfo> workInfo = WorkManager.getInstance(this).getWorkInfoById(workRequest.getId());
    }

    private void chainedWork() {
        Data.Builder dataBuilder1 = new Data.Builder();
        dataBuilder1.putInt("num", 3);
        OneTimeWorkRequest workRequest1 = new OneTimeWorkRequest
                .Builder(ChainedWorker.class)
                .setInputData(dataBuilder1.build())
                .addTag(WORK_SUM_TAG_1) // 使用string tag 而非 id 标记Work
                .build();

        Data.Builder dataBuilder2 = new Data.Builder();
        dataBuilder1.putInt("num2", 5);
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest
                .Builder(ChainedWorker.class)
                .setInputData(dataBuilder2.build())
                .addTag(WORK_SUM_TAG_2)
                .build();

        Data.Builder dataBuilder3 = new Data.Builder();
        dataBuilder1.putInt("num3", 10);
        OneTimeWorkRequest workRequest3 = new OneTimeWorkRequest
                .Builder(ChainedWorker.class)
                .setInputData(dataBuilder3.build())
                .addTag(WORK_SUM_TAG_3)
                .build();

        mWorkIdOfSumData1 = workRequest1.getId();
//        WorkManager.getInstance(this).beginWith(workRequest1).then(workRequest2).then(workRequest3).enqueue();

        // 假如任务链只运行1次。
        WorkManager.getInstance(this)
                .beginUniqueWork(UNIQUE_WORK_NAME_OF_SUM, ExistingWorkPolicy.REPLACE, workRequest1)
                .then(workRequest2)
                .then(workRequest3)
                .enqueue();

        // Work Info
//        WorkManager.getInstance(this).getWorkInfosByTag(WORK_SUM_TAG_1).addListener(new Run);
    }

    private void submitChainedWork2() {
        Data.Builder dataBuilder1 = new Data.Builder();
        dataBuilder1.putInt(FooWorker.FOO_KEY, 3);
        OneTimeWorkRequest workRequest1 = new OneTimeWorkRequest
                .Builder(FooWorker.class)
                .setInputData(dataBuilder1.build())
                .addTag(WORK_SUM_TAG_1) // 使用string tag 而非 id 标记Work
                .build();

        Data.Builder dataBuilder2 = new Data.Builder();
        dataBuilder1.putInt(BarWorker.BAR_KEY, 5);
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest
                .Builder(BarWorker.class)
                .setInputData(dataBuilder2.build())
                .addTag(WORK_SUM_TAG_2)
                .build();

        Data.Builder dataBuilder3 = new Data.Builder();
        dataBuilder1.putInt(BazWorker.BAZ_KEY, 10);
        OneTimeWorkRequest workRequest3 = new OneTimeWorkRequest
                .Builder(BazWorker.class)
                .setInputData(dataBuilder3.build())
                .addTag(WORK_SUM_TAG_3)
                .build();

        mWorkIdOfSumData1 = workRequest1.getId();
//        WorkManager.getInstance(this).beginWith(workRequest1).then(workRequest2).then(workRequest3).enqueue();
        WorkManager.getInstance(this).beginWith(workRequest1).then(workRequest2).then(workRequest3).enqueue();

        // 假如任务链只运行1次。
//        WorkManager.getInstance(this)
//                .beginUniqueWork(UNIQUE_WORK_NAME_OF_SUM, ExistingWorkPolicy.REPLACE, workRequest1)
//                .then(workRequest2)
//                .then(workRequest3)
//                .enqueue();

        // Work Info
//        WorkManager.getInstance(this).getWorkInfosByTag(WORK_SUM_TAG_1).addListener(new Run);
    }

    private void cancelWork() {
        // 当取消任务时，假如Work 不是 finished，这个work 以及依赖它的works，变成CANCELLED，将来不再执行。
        // 监听Work Info，ListenableWorker.onStopped 时执行cleanup。
        // way 1 : id
        if (null != mWorkIdOfSumData1) {
            WorkManager.getInstance(this).cancelWorkById(mWorkIdOfSumData1);
            mWorkIdOfSumData1 = null;
        }
        // way 2 : tag
        WorkManager.getInstance(this).cancelAllWorkByTag(WORK_SUM_TAG_1);
        // way 3 : name
        WorkManager.getInstance(this).cancelUniqueWork(UNIQUE_WORK_NAME_OF_SUM);
        // way 4 :
        WorkManager.getInstance(this).cancelAllWork();
    }
}