package com.phantom.onetapvideodownload.downloader.downloadinfo;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;

import java.util.Collection;

public class BrowserDownloadInfo extends DownloadInfo {
    private final static String TAG = "BrowserDownloadInfo";
    private String mVideoUrl, mDownloadLocation, mFilename, mPackageName;
    private long mDatabaseId = -1, mContentLength = -1, mDownloadedLength = -1;
    private Status mStatus;
    private Context mContext;

    public BrowserDownloadInfo(Context context, String filename, String url, String downloadPath) {
        mContext = context;
        mFilename = filename;
        mDownloadLocation = downloadPath;
        mVideoUrl = url;
        mStatus = Status.Stopped;
    }

    @Override
    public String getFilename() {
        return mFilename;
    }

    @Override
    public String getUrl() {
        return mVideoUrl;
    }

    @Override
    public long getDatabaseId() {
        return mDatabaseId;
    }

    @Override
    public void setDatabaseId(long databaseId) {
        mDatabaseId = databaseId;
    }

    @Override
    public String getDownloadLocation() {
        return mDownloadLocation;
    }

    @Override
    public Status getStatus() {
        return mStatus;
    }

    @Override
    public void setStatus(Status status) {
        Log.e(TAG, "Download Status changed from " + mStatus.name() + " to " + status.name());
        mStatus = status;
    }

    @Override
    public long getContentLength() {
        return mContentLength;
    }

    @Override
    public void setContentLength(long contentLength) {
        mContentLength = contentLength;
    }

    @Override
    public long getDownloadedLength() {
        return mDownloadedLength;
    }

    @Override
    public void setDownloadedLength(long downloadedLength) {
        mDownloadedLength = downloadedLength;
    }

    @Override
    public void addDownloadedLength(long additionValue) {
        mDownloadedLength += additionValue;
    }

    @Override
    public Integer getProgress() {
        return (int)((mDownloadedLength*100)/mContentLength);
    }

    @Override
    public void writeToDatabase() {
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(mContext);
        downloadDatabase.addOrUpdateDownload(this);
    }

    @Override
    public Collection<String> getOptions() {
        return getGenericOptions(mContext, mStatus);
    }

    int findIdByString(String string) {
        if (mContext.getResources().getString(R.string.open).equals(string)) {
            return R.string.open;
        } else if(mContext.getResources().getString(R.string.share).equals(string)) {
            return R.string.share;
        } else if(mContext.getResources().getString(R.string.resume).equals(string)) {
            return R.string.resume;
        } else if(mContext.getResources().getString(R.string.remove_from_list).equals(string)) {
            return R.string.remove_from_list;
        } else if(mContext.getResources().getString(R.string.delete_from_storage).equals(string)) {
            return R.string.delete_from_storage;
        } else if(mContext.getResources().getString(R.string.pause).equals(string)) {
            return R.string.pause;
        } else if(mContext.getResources().getString(R.string.details).equals(string)) {
            return R.string.details;
        } else {
            return -1;
        }
    }

    @Override
    public MaterialDialog.ListCallback getOptionCallback() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                int resId = findIdByString((String) text);
                if (resId == -1) {
                    return;
                }

                // Used Activity context instead of ApplicationContext
                if (!handleGenericOptionClicks(dialog.getContext(), resId)) {
                    switch (resId) {
                        // ToDo: Implement the resume and pause functionality
                        // case R.string.resume:
                        // ToDo: Implement the resume and pause functionality
                        // case R.string.pause:
                        // ToDo: Implement the resume and pause functionality
                        // case R.string.details:
                    }
                }
            }
        };
    }

    @Override
    public void removeDatabaseEntry() {
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(mContext);
        downloadDatabase.deleteDownload(getDatabaseId());
    }

    @Override
    public String getPackageName() {
        return mPackageName;
    }

    @Override
    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }
}
