package com.phantom;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by phantom on 11/09/16.
 */
public class HookFetchJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case HookFetchJob.TAG:
                return new HookFetchJob();
            default:
                return null;
        }
    }
}
