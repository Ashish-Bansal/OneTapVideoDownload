package com.phantom.onetapvideodownload.downloader.downloadinfo;

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
    int getStatusCode();
    void setStatus(Status status);
}
