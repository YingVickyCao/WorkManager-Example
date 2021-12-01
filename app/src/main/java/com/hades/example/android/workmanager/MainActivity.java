package com.hades.example.android.workmanager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.ArrayCreatingInputMerger;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OverwritingInputMerger;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final String UNIQUE_WORK_NAME_OF_SUM = "SUM_TASK";
    private final String UNIQUE_WORK_1 = "UNIQUE_WORK_1";
    private final String UNIQUE_WORK_2 = "UNIQUE_WORK_2";
    private final String UNIQUE_WORK_3_CHAINED_WORK = "UNIQUE_WORK_3_CHAINED_WORK";
    private final String UNIQUE_WORK_4_CHAINED_WORK = "UNIQUE_WORK_4_CHAINED_WORK";
    private final String UNIQUE_UNIQUE_PERIODIC_WORK_1 = "UNIQUE_UNIQUE_PERIODIC_WORK_1";

    public static final String SUM_KEY = "SUM_KEY";
    private final String UNIQUE_WORK_3_CHAINED_WORK_TAG_1 = "UNIQUE_WORK_3_CHAINED_WORK_TAG_1";
    private final String UNIQUE_WORK_3_CHAINED_WORK_TAG_2 = "UNIQUE_WORK_3_CHAINED_WORK_TAG_2";
    private final String UNIQUE_WORK_3_CHAINED_WORK_TAG_3 = "UNIQUE_WORK_3_CHAINED_WORK_TAG_3";
    private UUID mWorkIdOfSumData1;
    public static final String WORK_REQUEST_TAG_PERIODIC_1 = "PERIODIC_WORK_REQUEST_1";
    public static final String WORK_REQUEST_TAG_2 = "WORK_REQUEST_2";

    public static final String KEY_PLANT_NAME_1 = "plant_name_1";
    public static final String KEY_PLANT_NAME_2 = "plant_name_2";
    public static final String KEY_PLANT_NAME_3 = "plant_name_3";
    public static final String KEY_CHAINING_1 = "chaining_1";
    public static final String KEY_CHAINING_2 = "chaining_2";
    public static final String KEY_CHAINING_3 = "chaining_3";


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
//        findViewById(R.id.chainingWork).setOnClickListener(view -> chainedWork());
        findViewById(R.id.chainingWork).setOnClickListener(view -> chainedWork2());
        findViewById(R.id.observeWork).setOnClickListener(view -> observeWork());
    }

    private void scheduleOneTimeWork() {
//        singleWork();
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

        // TODO: 2021/11/30  ListenableFuture
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
    }

    private void chainedWork() {
        Data.Builder dataBuilder1 = new Data.Builder();
        dataBuilder1.putInt("num", 3);
        OneTimeWorkRequest workRequest1 = new OneTimeWorkRequest
                .Builder(Chaining_1_1_Worker.class)
                .setInputData(dataBuilder1.build())
                .addTag(UNIQUE_WORK_3_CHAINED_WORK_TAG_1) // 使用string tag 而非 id 标记Work
                .build();

        Data.Builder dataBuilder2 = new Data.Builder();
        dataBuilder1.putInt("num2", 5);
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest
                .Builder(Chaining_1_2_Worker.class)
                .setInputData(dataBuilder2.build())
                .setInputMerger(OverwritingInputMerger.class)
//                .setInputMerger(ArrayCreatingInputMerger.class)
                .addTag(UNIQUE_WORK_3_CHAINED_WORK_TAG_2)
                .build();

        Data.Builder dataBuilder3 = new Data.Builder();
        dataBuilder1.putInt("num3", 10);
        OneTimeWorkRequest workRequest3 = new OneTimeWorkRequest
                .Builder(Chaining_1_3Worker.class)
                .setInputMerger(OverwritingInputMerger.class)
//                .setInputMerger(ArrayCreatingInputMerger.class)
                .setInputData(dataBuilder3.build())
                .addTag(UNIQUE_WORK_3_CHAINED_WORK_TAG_3)
                .build();

        /*
         Work入队时按次序入队，没有并列Wor时，.beginUniqueWork(UNIQUE_WORK_3_CHAINED_WORK, ExistingWorkPolicy.KEEP, workRequest1)
         (1)只有第一个入对的setInputData有效果。
         (2)后一个把前一个的输出作为输入，并忽略它本身的Input，e.g., Chaining_1_1_Worker -> Chaining_1_2_Worker
         (3) 不需要设置setInputMerger。及时设置了，也不改变值传输结果。
             E/Chaining_1_1_Worker: doWork: start:Data {num : 3, }
            E/Chaining_1_2_Worker: doWork: start:Data {plant_name_1 : A, }
            E/MainActivity: workRequest1:onChanged: Data {plant_name_1 : A, },state:SUCCEEDED
            E/Chaining_1_3Worker: doWork: start:Data {plant_name_1 : M, }
            E/MainActivity: workRequest2:onChanged: Data {plant_name_1 : M, },state:SUCCEEDED
            E/MainActivity: workRequest3:onChanged: Data {plant_name_3 : X, },state:SUCCEEDED
         */
        // 假如任务链只运行1次。
        WorkManager.getInstance(this)
                .beginUniqueWork(UNIQUE_WORK_3_CHAINED_WORK, ExistingWorkPolicy.KEEP, workRequest1)
                .then(workRequest2)
                .then(workRequest3)
                .enqueue();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest1.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (null != workInfo && workInfo.getState().isFinished()) {
                            Log.e(TAG, "workRequest1:onChanged: " + workInfo.getOutputData() + ",state:" + workInfo.getState());
                        }
                    }
                });
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest2.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (null != workInfo && workInfo.getState().isFinished()) {
                            Log.e(TAG, "workRequest2:onChanged: " + workInfo.getOutputData() + ",state:" + workInfo.getState());
                        }
                    }
                });
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest3.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (null != workInfo && workInfo.getState().isFinished()) {
                            Log.e(TAG, "workRequest3:onChanged: " + workInfo.getOutputData() + ",state:" + workInfo.getState());
                        }
                    }
                });

    }

    private void chainedWork2() {
        Data.Builder dataBuilder1 = new Data.Builder();
        dataBuilder1.putInt("num", 3);
        OneTimeWorkRequest workRequest1 = new OneTimeWorkRequest
                .Builder(Chaining_2_1_Worker.class)
                .setInputData(dataBuilder1.build())
                .build();

        Data.Builder dataBuilder2 = new Data.Builder();
        dataBuilder1.putInt("num2", 5);
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest
                .Builder(Chaining_2_2_Worker.class)
                .setInputData(dataBuilder2.build())
//                .setInputMerger(OverwritingInputMerger.class)
                .setInputMerger(ArrayCreatingInputMerger.class)
                .build();

        Data.Builder dataBuilder3 = new Data.Builder();
        dataBuilder1.putInt("num3", 10);
        OneTimeWorkRequest workRequest3 = new OneTimeWorkRequest
                .Builder(Chaining_2_3_Worker.class)
//                .setInputMerger(OverwritingInputMerger.class)
                .setInputMerger(ArrayCreatingInputMerger.class)
                .setInputData(dataBuilder3.build())
                .build();
        /*
           Work入队有并列时，
            (1)只有第一个入对的setInputData有效果。
           （2）OverwritingInputMerger，并列的Works，含有相同Key的Work，传出的是后完成的value。

            OverwritingInputMerger:
            E/Chaining_2_1_Worker: doWork: start:Data {num : 3, }
            E/Chaining_2_2_Worker: doWork: start:Data {}
            E/Chaining_2_2_Worker: doWork: end
            E/MainActivity: workRequest2:onChanged: Data {plant_name_1 : B, },state:SUCCEEDED
            E/Chaining_2_1_Worker: doWork: end
            E/Chaining_2_3_Worker: doWork: start:Data {plant_name_1 : B, }
            E/MainActivity: workRequest1:onChanged: Data {plant_name_1 : A, },state:SUCCEEDED
            E/Chaining_2_3_Worker: doWork: end
            E/MainActivity: workRequest3:onChanged: Data {plant_name_1 : C, },state:SUCCEEDED

            （3）ArrayCreatingInputMerger：并列的Works，含有相同Key的Work，传出的是按入队顺序的list。
            E/Chaining_2_1_Worker: doWork: start:Data {num : 3, }
            E/Chaining_2_2_Worker: doWork: start:Data {}
            E/Chaining_2_2_Worker: doWork: end
            E/MainActivity: workRequest2:onChanged: Data {plant_name_1 : B, },state:SUCCEEDED
            E/Chaining_2_1_Worker: doWork: end
            E/Chaining_2_3_Worker: doWork: start:Data {plant_name_1 : [A, B], }
            E/MainActivity: workRequest1:onChanged: Data {plant_name_1 : A, },state:SUCCEEDED
            E/Chaining_2_3_Worker: doWork: end
            E/MainActivity: workRequest3:onChanged: Data {plant_name_1 : C, },state:SUCCEEDED
         */
        // 假如任务链只运行1次。
        WorkManager.getInstance(this)
                // 并行执行 workRequestList 中的任务
                .beginUniqueWork(UNIQUE_WORK_4_CHAINED_WORK, ExistingWorkPolicy.KEEP, Arrays.asList(workRequest1, workRequest2))
                .then(workRequest3)
                .enqueue();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest1.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (null != workInfo && workInfo.getState().isFinished()) {
                            Log.e(TAG, "workRequest1:onChanged: " + workInfo.getOutputData() + ",state:" + workInfo.getState());
                        }
                    }
                });
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest2.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (null != workInfo && workInfo.getState().isFinished()) {
                            Log.e(TAG, "workRequest2:onChanged: " + workInfo.getOutputData() + ",state:" + workInfo.getState());
                        }
                    }
                });
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest3.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (null != workInfo && workInfo.getState().isFinished()) {
                            Log.e(TAG, "workRequest3:onChanged: " + workInfo.getOutputData() + ",state:" + workInfo.getState());
                        }
                    }
                });

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
        WorkManager.getInstance(this).cancelAllWorkByTag(UNIQUE_WORK_3_CHAINED_WORK_TAG_1);
        // way 3 : name
        WorkManager.getInstance(this).cancelUniqueWork(UNIQUE_WORK_NAME_OF_SUM);
        // way 4 :
        WorkManager.getInstance(this).cancelAllWork();
    }
}