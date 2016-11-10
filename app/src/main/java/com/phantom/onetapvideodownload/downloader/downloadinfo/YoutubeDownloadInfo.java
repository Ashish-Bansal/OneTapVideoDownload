package com.phantom.onetapvideodownload.downloader.downloadinfo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.ui.MainActivity;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.Video.Video;
import com.phantom.onetapvideodownload.Video.YoutubeVideo;
import com.phantom.utils.YoutubeParserProxy;
import com.phantom.onetapvideodownload.databasehandlers.DownloadDatabase;
import com.phantom.onetapvideodownload.databasehandlers.VideoDatabase;
import com.phantom.utils.Invokable;

import java.util.Collection;
import java.util.List;

public class YoutubeDownloadInfo extends DownloadInfo implements Invokable<Video, Integer> {
    private final static String TAG = "YoutubeDownloadInfo";
    private String mParam, mVideoUrl, mDownloadLocation, mFilename, mPackageName;
    private int mItag;
    private long mDatabaseId = -1, mContentLength = -1, mDownloadedLength = 0;
    private Status mStatus;
    private Context mContext;
    private MaterialDialog mProgressDialog;

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
    public Context getContext() {
        return mContext;
    }

    @Override
    public Collection<String> getOptions() {
        List<String> options = super.getOptions(mContext, mStatus);
        switch (mStatus) {
            case Completed:
                options.add(mContext.getResources().getString(R.string.download_in_other_resolution));
                break;
            case Stopped:
                options.add(mContext.getResources().getString(R.string.download_in_other_resolution));
                break;
        }
        return options;
    }

    @Override
    public int findIdByString(Context context, String string) {
        int resourceId = super.findIdByString(context, string);
        if (resourceId != -1) {
            return resourceId;
        }

        if(mContext.getResources().getString(R.string.download_in_other_resolution).equals(string)) {
            return R.string.download_in_other_resolution;
        } else {
            return -1;
        }
    }

    @Override
    public boolean handleOptionClicks(Context context, int resId) {
        if (super.handleOptionClicks(context, resId)) {
            return true;
        }

        switch (resId) {
            case R.string.download_in_other_resolution:
                mProgressDialog = new MaterialDialog.Builder(context)
                        .title(R.string.progress_dialog)
                        .content(R.string.please_wait)
                        .progress(true, 0)
                        .show();
                YoutubeParserProxy.startParsing(mContext, mParam, YoutubeDownloadInfo.this);
                return true;
        }

        return false;
    }

    public void removeDatabaseEntry() {
        DownloadDatabase downloadDatabase = DownloadDatabase.getDatabase(mContext);
        downloadDatabase.deleteDownload(getDatabaseId());
    }

    private long saveVideoToDatabase(Video video) {
        VideoDatabase videoDatabase = VideoDatabase.getDatabase(mContext);
        return videoDatabase.addOrUpdateVideo(video);
    }

    @Override
    public Integer invoke(Video video) {
        if (video != null) {
            YoutubeVideo youtubeVideo = (YoutubeVideo)video;
            youtubeVideo.setPackageName("com.google.android.youtube");

            long id = saveVideoToDatabase(youtubeVideo);
            Intent downloadIntent = new Intent(mContext, MainActivity.class);
            downloadIntent.setAction(MainActivity.ACTION_SHOW_DOWNLOAD_DIALOG);
            downloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            downloadIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            downloadIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            downloadIntent.putExtra("videoId", id);
            mContext.startActivity(downloadIntent);
        } else {
            Toast.makeText(mContext, R.string.unable_to_fetch, Toast.LENGTH_LONG).show();
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        return 0;
    }

    @Override
    public String getPackageName() {
        return mPackageName;
    }

    @Override
    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }
}
