package com.phantom.onetapvideodownload.Video;

import java.io.Serializable;
import android.content.Context;

public interface Video extends Serializable{
    String getUrl();
    String getTitle();
    long getDatabaseId();
    void setDatabaseId(long databaseId);
    void setContext(Context context);
}
