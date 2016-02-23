package com.phantom.onetapvideodownload.downloader.downloadinfo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.Global;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BrowserDownloadInfo implements DownloadInfo {
    private final static String TAG = "BrowserDownloadInfo";
    private String mVideoUrl, mDownloadLocation, mFilename;
    private long mDatabaseId = -1, mContentLength = -1, mDownloadedLength = -1;
    private Status mStatus;
    private Context mContext;

    public BrowserDownloadInfo(Context context, String filename, String url, String downloadPath) {
        mContext = context;
        mFilename = filename;
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
                options.add(mContext.getResources().getString(R.string.remove_from_list));
                options.add(mContext.getResources().getString(R.string.delete_from_storage));
                options.add(mContext.getResources().getString(R.string.details));
                break;
            case Stopped:
                options.add(mContext.getResources().getString(R.string.resume));
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
                    case R.string.resume:
                    case R.string.remove_from_list:
                        // Used Activity context instead of ApplicationContext
                        new MaterialDialog.Builder(dialog.getContext())
                                .title(R.string.remove_from_list_confirmation)
                                .content(R.string.remove_from_list_confirmation_content)
                                .positiveText(R.string.yes)
                                .negativeText(R.string.no)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        removeDatabaseEntry();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        break;
                    case R.string.delete_from_storage:
                        // Used Activity context instead of ApplicationContext
                        new MaterialDialog.Builder(dialog.getContext())
                                .title(R.string.delete_confirmation)
                                .content(R.string.delete_confirmation_content)
                                .positiveText(R.string.yes)
                                .negativeText(R.string.no)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        Global.deleteFile(mContext, getDownloadLocation());
                                        removeDatabaseEntry();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        break;
                    case R.string.pause:
                    case R.string.details:
                }
            }
        };
    }

    private void removeDatabaseEntry() {
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(mContext);
        downloadDatabase.deleteDownload(getDatabaseId());
    }
}
