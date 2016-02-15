package com.phantom.onetapvideodownload.Video;

public interface Video {
    String getUrl();
    String getTitle();
    long getDatabaseId();
    void setDatabaseId(long databaseId);
}
