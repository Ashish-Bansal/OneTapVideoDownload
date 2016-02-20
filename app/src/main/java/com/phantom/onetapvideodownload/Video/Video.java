package com.phantom.onetapvideodownload.Video;

import java.io.Serializable;

public interface Video extends Serializable{
    String getUrl();
    String getTitle();
    long getDatabaseId();
    void setDatabaseId(long databaseId);
}
