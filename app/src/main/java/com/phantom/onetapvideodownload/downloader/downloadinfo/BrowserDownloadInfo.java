package com.phantom.onetapvideodownload.downloader.downloadinfo;

public class BrowserDownloadInfo implements DownloadInfo {
    private String mVideoUrl, mDownloadLocation, mTitle;
    private long mDatabaseId = -1;

    public BrowserDownloadInfo(String title, String url, String downloadPath) {
        mTitle = title;
        mDownloadLocation = downloadPath;
        mVideoUrl = url;
    }

    @Override
    public String getTitle() {
        return mTitle;
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

}
