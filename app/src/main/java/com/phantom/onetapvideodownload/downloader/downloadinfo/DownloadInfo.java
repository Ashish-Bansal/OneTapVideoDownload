package com.phantom.onetapvideodownload.downloader.downloadinfo;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collection;

public interface DownloadInfo {
    enum Status {
        Stopped(0), Completed(1), Downloading(2), NetworkProblem(3), NetworkNotAvailable(4),
        WriteFailed(5);

        int status;
        Status(int s) {
            status = s;
        }

        int getStatus() {
            return status;
        }
    }

    String getUrl();
    String getFilename();
    String getDownloadLocation();
    long getDatabaseId();
    void setDatabaseId(long databaseId);
    Status getStatus();
    void setStatus(Status status);
    long getContentLength();
    void setContentLength(long contentLength);
    long getDownloadedLength();
    void setDownloadedLength(long downloadedLength);
    void addDownloadedLength(long additionValue);
    Integer getProgress();
    void writeToDatabase();
    Collection<String> getOptions();
    MaterialDialog.ListCallback getOptionCallback();
    String getPackageName();
    void setPackageName(String packageName);
}
