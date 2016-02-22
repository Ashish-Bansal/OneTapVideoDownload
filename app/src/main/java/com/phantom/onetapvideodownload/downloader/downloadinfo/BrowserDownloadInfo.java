package com.phantom.onetapvideodownload.downloader.downloadinfo;

public class BrowserDownloadInfo implements DownloadInfo {
    private String mVideoUrl, mDownloadLocation, mFilename;
    private long mDatabaseId = -1;
    private Status mStatus;

    public BrowserDownloadInfo(String filename, String url, String downloadPath) {
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
    public int getStatusCode() {
        return mStatus.getStatus();
    }
}
