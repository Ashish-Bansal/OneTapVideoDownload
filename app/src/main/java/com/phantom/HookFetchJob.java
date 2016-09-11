package com.phantom;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.phantom.onetapvideodownload.ApplicationLogMaintainer;
import com.phantom.utils.HookClassNamesFetcher;

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
                .setPeriodic(100_000_000L)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .build()
                .schedule();
    }
}