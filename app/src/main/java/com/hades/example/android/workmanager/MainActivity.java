package com.hades.example.android.workmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final String UNIQUE_WORK_NAME_OF_SUM = "SUM_TASK";
    private final String WORK_SUM_TAG_1 = "SUM_DATA_1";
    private final String WORK_SUM_TAG_2 = "SUM_DATA_2";
    private final String WORK_SUM_TAG_3 = "SUM_DATA_3";
    private UUID mWorkIdOfSumData1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.singleWork).setOnClickListener(view -> submitSingleWork());
        findViewById(R.id.chainWork).setOnClickListener(view -> submitChainedWork());
        findViewById(R.id.cancelWork).setOnClickListener(view -> cancelWork());
    }

    // Submit work request to system
    private void submitSingleWork() {
        // thread id:2,thread name:main
        Log.d(TAG, "submitWorkRequestToSystem: thread id:" + Thread.currentThread().getId() + ",thread name:" + Thread.currentThread().getName());

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("k1", "value1");
        dataBuilder.putInt("k2", 2);
        dataBuilder.putBoolean("k3", true);

        WorkRequest workRequest = new OneTimeWorkRequest
                .Builder(SingleWorker.class)
                .setInputData(dataBuilder.build())
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private void submitChainedWork() {
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


    private void cancelWork() {
        // way 1
        if (null != mWorkIdOfSumData1) {
            WorkManager.getInstance(this).cancelWorkById(mWorkIdOfSumData1);
            mWorkIdOfSumData1 = null;
        }
        // way 2
        WorkManager.getInstance(this).cancelAllWorkByTag(WORK_SUM_TAG_1);
        // way 3
        WorkManager.getInstance(this).cancelUniqueWork(UNIQUE_WORK_NAME_OF_SUM);
        // way 4
        WorkManager.getInstance(this).cancelAllWork();
    }
}