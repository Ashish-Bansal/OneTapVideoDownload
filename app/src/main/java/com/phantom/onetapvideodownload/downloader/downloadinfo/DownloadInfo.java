package com.phantom.onetapvideodownload.downloader.downloadinfo;

public interface DownloadInfo {
    String getUrl();
    String getFilename();
    String getDownloadLocation();
    long getDatabaseId();
    void setDatabaseId(long databaseId);
}
