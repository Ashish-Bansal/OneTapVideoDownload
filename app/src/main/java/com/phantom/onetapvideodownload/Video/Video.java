package com.phantom.onetapvideodownload.Video;

import android.content.Context;

import com.phantom.onetapvideodownload.ui.downloadoptions.DownloadOptionItem;
import com.phantom.onetapvideodownload.ui.downloadoptions.DownloadOptionAdapter;

import java.util.List;

public interface Video {
    String getPackageName();
    void setPackageName(String packageName);
    String getUrl();
    String getTitle();
    long getDatabaseId();
    void setDatabaseId(long databaseId);
    List<DownloadOptionItem> getOptions(Context context, DownloadOptionAdapter downloadOptionAdapter);
    boolean isResourceAvailable();
}
