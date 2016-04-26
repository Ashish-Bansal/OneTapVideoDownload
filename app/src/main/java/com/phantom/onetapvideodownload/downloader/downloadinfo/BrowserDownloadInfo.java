package com.phantom.onetapvideodownload.downloader.downloadinfo;

import android.content.Context;

import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;

import java.util.Collection;

public class BrowserDownloadInfo extends DownloadInfo {
    private final static String TAG = "BrowserDownloadInfo";
    private String mVideoUrl, mDownloadLocation, mFilename, mPackageName;
    private long mDatabaseId = -1, mContentLength = -1, mDownloadedLength = 0;
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
    public Context getContext() {
        return mContext;
    }

    @Override
    public Collection<String> getOptions() {
        return super.getOptions(mContext, mStatus);
    }

    @Override
    public boolean handleOptionClicks(Context context, int resId) {
        return super.handleOptionClicks(context, resId);
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

    @Override
    public int findIdByString(Context context, String string) {
        return super.findIdByString(context, string);
    }
}
