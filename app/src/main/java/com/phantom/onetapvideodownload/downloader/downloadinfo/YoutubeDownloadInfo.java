package com.phantom.onetapvideodownload.downloader.downloadinfo;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.Global;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YoutubeDownloadInfo implements DownloadInfo {
    private final static String TAG = "YoutubeDownloadInfo";
    private String mParam, mVideoUrl, mDownloadLocation, mFilename;
    private int mItag;
    private long mDatabaseId = -1, mContentLength = -1, mDownloadedLength = -1;
    private Status mStatus;
    private Context mContext;

    public YoutubeDownloadInfo(Context context, String filename, String url, String downloadPath, String param, int itag) {
        mContext = context;
        mFilename = filename;
        mItag = itag;
        mParam = param;
        mDownloadLocation = downloadPath;
        mVideoUrl = url;
        mStatus = Status.Stopped;
    }

    @Override
    public String getFilename() {
        return mFilename;
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

    @Override
    public Status getStatus() {
        return mStatus;
    }

    @Override
    public void setStatus(Status status) {
        Log.e(TAG, "Download Status changed from " + mStatus.name() + " to " + status.name());
        mStatus = status;
    }

    @Override
    public long getContentLength() {
        return mContentLength;
    }

    @Override
    public void setContentLength(long contentLength) {
        mContentLength = contentLength;
    }

    @Override
    public long getDownloadedLength() {
        return mDownloadedLength;
    }

    @Override
    public void setDownloadedLength(long downloadedLength) {
        mDownloadedLength = downloadedLength;
    }

    @Override
    public void addDownloadedLength(long additionValue) {
        mDownloadedLength += additionValue;
    }

    @Override
    public Integer getProgress() {
        return (int)((mDownloadedLength*100)/mContentLength);
    }

    @Override
    public void writeToDatabase() {
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(mContext);
        downloadDatabase.addOrUpdateDownload(this);
    }

    @Override
    public Collection<String> getOptions() {
        List<String> options = new ArrayList<>();
        switch (mStatus) {
            case Completed:
                options.add(mContext.getResources().getString(R.string.open));
                options.add(mContext.getResources().getString(R.string.share));
                options.add(mContext.getResources().getString(R.string.download_in_other_resolution));
                options.add(mContext.getResources().getString(R.string.remove_from_list));
                options.add(mContext.getResources().getString(R.string.delete_from_storage));
                options.add(mContext.getResources().getString(R.string.details));
                break;
            case Stopped:
                options.add(mContext.getResources().getString(R.string.resume));
                options.add(mContext.getResources().getString(R.string.download_in_other_resolution));
                options.add(mContext.getResources().getString(R.string.remove_from_list));
                options.add(mContext.getResources().getString(R.string.delete_from_storage));
                options.add(mContext.getResources().getString(R.string.details));
                break;
            case Downloading:
                options.add(mContext.getResources().getString(R.string.pause));
                options.add(mContext.getResources().getString(R.string.details));
                break;
        }
        return options;
    }

    int findIdByString(String string) {
        if (mContext.getResources().getString(R.string.open).equals(string)) {
            return R.string.open;
        } else if(mContext.getResources().getString(R.string.share).equals(string)) {
            return R.string.share;
        } else if(mContext.getResources().getString(R.string.download_in_other_resolution).equals(string)) {
            return R.string.download_in_other_resolution;
        } else if(mContext.getResources().getString(R.string.resume).equals(string)) {
            return R.string.resume;
        } else if(mContext.getResources().getString(R.string.remove_from_list).equals(string)) {
            return R.string.remove_from_list;
        } else if(mContext.getResources().getString(R.string.delete_from_storage).equals(string)) {
            return R.string.delete_from_storage;
        } else if(mContext.getResources().getString(R.string.pause).equals(string)) {
            return R.string.pause;
        } else if(mContext.getResources().getString(R.string.details).equals(string)) {
            return R.string.details;
        } else {
            return -1;
        }
    }

    @Override
    public MaterialDialog.ListCallback getOptionCallback() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                int resId = findIdByString((String) text);
                if (resId == -1) {
                    return;
                }

                switch (resId) {
                    case R.string.open:
                        Global.startOpenIntent(mContext, getDownloadLocation());
                        break;
                    case R.string.share:
                        Global.startFileShareIntent(mContext, getDownloadLocation());
                        break;
                    case R.string.download_in_other_resolution:
                    case R.string.resume:
                    case R.string.remove_from_list:
                    case R.string.delete_from_storage:
                    case R.string.pause:
                    case R.string.details:
                }
            }
        };
    }
}
