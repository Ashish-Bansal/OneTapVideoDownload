package com.phantom.onetapvideodownload.downloader.downloadinfo;

public class YoutubeDownloadInfo implements DownloadInfo {
    private String mParam, mVideoUrl, mDownloadLocation, mTitle;
    private int mItag;
    private long mDatabaseId = -1;

    public YoutubeDownloadInfo(String title, String url, String downloadPath, String param, int itag) {
        mTitle = title;
        mItag = itag;
        mParam = param;
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

    public String getParam() {
        return mParam;
    }

    public int getItag() {
        return mItag;
    }

    @Override
    public String getDownloadLocation() {
        return mDownloadLocation;
    }
}
