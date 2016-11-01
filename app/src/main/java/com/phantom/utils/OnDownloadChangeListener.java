package com.phantom.utils;

public interface OnDownloadChangeListener {
    void onDownloadAdded(int index);
    void onDownloadRemoved(int index);
    void onDownloadInfoUpdated(int index);
    void onReset();
}
