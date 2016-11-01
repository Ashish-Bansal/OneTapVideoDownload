package com.phantom;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.utils.HookClassNamesFetcher;

import java.util.concurrent.TimeUnit;

public class HookFetchJob extends Job {

    public static final String TAG = "hook_fetch_job";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        ApplicationLogMaintainer.sendBroadcast(getContext(), "Running fetch update job");
        HookClassNamesFetcher.startHookFileUpdateOnMainThread(getContext());
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(HookFetchJob.TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .build()
                .schedule();
    }
}