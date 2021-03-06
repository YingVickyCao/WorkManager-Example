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
        findViewById(R.id.testCoroutineWorker).setOnClickListener(view -> testCoroutineWorker());
        findViewById(R.id.testRxWorker).setOnClickListener(view -> testRxWorker());
        findViewById(R.id.longRunningWorkers).setOnClickListener(view -> longRunningWorkers());
    }

    private void scheduleOneTimeWork() {
        singleWork();
    }

    private void uniqueWork() {
        // One-Time Work
        {
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UniqueWorker.class)
                    .addTag(UniqueWorker.TAG)
                    .build();
            // ExistingWorkPolicy, ??????WorkManager????????????????????????work????????????????????????
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
                    // If State.isFinished() ,WorkManager ????????? progress???
                    Log.e(TAG, "onChanged: progress:" + progress + ",state:" + workInfo.getState());
                    Toast.makeText(MainActivity.this, String.valueOf(progress), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Work queries to support complex filters
        // ???????????????OR?????????????????????AND. Example : tag AND state AND ID AND name, (name1 OR name2 OR name3)
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
         * repeat interval : ????????????????????? 15 minutes {@link PeriodicWorkRequest#MIN_PERIODIC_INTERVAL_MILLIS} ??????JobScheduler
         * flex Interval ??? ????????????????????????????????????5?????? {@link PeriodicWorkRequest#MIN_PERIODIC_FLEX_MILLIS}????????????????????????
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
//                .setRequiresBatteryNotLow(true)
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .setRequiresDeviceIdle(false)
//                .setRequiresStorageNotLow(false)
                .build();
        WorkRequest workRequest = new OneTimeWorkRequest.Builder(SingleWorker.class)
                .setInputData(dataBuilder.build())
                // ?????????????????????????????????????????????????????????????????????????????????????????????
                .setConstraints(constraints)
                //  ?????????????????????????????????????????????Work?????????????????????????????????????????????work??????????????? InitialDelay ??????????????????????????????
//                .setInitialDelay(0, TimeUnit.MINUTES)
                // ???Work ????????????Result.retry()???????????????.(1)retry interval ???????????????EXPONENTIAL???10???20???40???80???LINEAR???10???20???30???40???
                // (3)???????????????????????????10s????????????1???????????????????????????????????????????????????????????????
                // WM-WorkSpec: Backoff delay duration exceeds maximum value
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MAX_BACKOFF_MILLIS, TimeUnit.DAYS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                // ??????WorkRequest?????????????????????????????????Work???observer work's progress. ??????WorkRequest????????????set of tag strings???
                .addTag(WORK_REQUEST_TAG_2)
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);
        WorkManager.getInstance(this).cancelWorkById(workRequest.getId());
    }

    private void chainedWork() {
        Data.Builder dataBuilder1 = new Data.Builder();
        dataBuilder1.putInt("num", 3);
        OneTimeWorkRequest workRequest1 = new OneTimeWorkRequest
                .Builder(Chaining_1_1_Worker.class)
                .setInputData(dataBuilder1.build())
                .addTag(UNIQUE_WORK_3_CHAINED_WORK_TAG_1) // ??????string tag ?????? id ??????Work
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
         Work???????????????????????????????????????Wor??????.beginUniqueWork(UNIQUE_WORK_3_CHAINED_WORK, ExistingWorkPolicy.KEEP, workRequest1)
         (1)????????????????????????setInputData????????????
         (2)??????????????????????????????????????????????????????????????????Input???e.g., Chaining_1_1_Worker -> Chaining_1_2_Worker
         (3) ???????????????setInputMerger???????????????????????????????????????????????????
             E/Chaining_1_1_Worker: doWork: start:Data {num : 3, }
            E/Chaining_1_2_Worker: doWork: start:Data {plant_name_1 : A, }
            E/MainActivity: workRequest1:onChanged: Data {plant_name_1 : A, },state:SUCCEEDED
            E/Chaining_1_3Worker: doWork: start:Data {plant_name_1 : M, }
            E/MainActivity: workRequest2:onChanged: Data {plant_name_1 : M, },state:SUCCEEDED
            E/MainActivity: workRequest3:onChanged: Data {plant_name_3 : X, },state:SUCCEEDED
         */
        // ????????????????????????1??????
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
           Work?????????????????????
            (1)????????????????????????setInputData????????????
           ???2???OverwritingInputMerger????????????Works???????????????Key???Work???????????????????????????value???

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

            ???3???ArrayCreatingInputMerger????????????Works???????????????Key???Work?????????????????????????????????list???
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
        // ????????????????????????1??????
        WorkManager.getInstance(this)
                // ???????????? workRequestList ????????????
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
        // ???????????????????????????Work ?????? finished?????????work ??????????????????works?????????CANCELLED????????????????????????
        // ??????Work Info???ListenableWorker.onStopped ?????????cleanup???
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

    private void testCoroutineWorker() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SingleWorker4Kotlin.class)
                .addTag("SingleWorker4Kotlin")
                .build();
        WorkManager.getInstance(this).enqueueUniqueWork("testCoroutineWorker", ExistingWorkPolicy.KEEP, request);
    }

    private void testRxWorker() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SingleWorker4RxJava.class)
                .addTag("SingleWorker4RxJava")
                .build();
        WorkManager.getInstance(this).enqueueUniqueWork("testRxWorker", ExistingWorkPolicy.KEEP, request);
    }

    private void longRunningWorkers() {
        new LongRunningWorkersExample().test();
    }
}