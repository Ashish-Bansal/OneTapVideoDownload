package com.phantom.onetapvideodownload.ui.downloads;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;

import java.net.URLDecoder;
import java.util.Locale;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class DownloadViewHolder extends RecyclerView.ViewHolder {
    private ImageView mApplicationImageView, mStatusStopped, mStatusCompleted;
    private TextView mDownloadTitle, mDownloadUrl, mPercentageTextView;
    private MaterialProgressBar mIndeterminateProgressBar;
    private MaterialProgressBar mProgressBar;
    private RelativeLayout mStatusDownloading;
    private ImageView mSelectedTick;

    public DownloadViewHolder(View v) {
        super(v);
        mApplicationImageView = (ImageView)itemView.findViewById(R.id.application_icon);
        mDownloadTitle = (TextView)itemView.findViewById(R.id.download_title);
        mDownloadUrl = (TextView)itemView.findViewById(R.id.download_url);
        mPercentageTextView = (TextView)itemView.findViewById(R.id.percentage);
        mProgressBar = (MaterialProgressBar) itemView.findViewById(R.id.download_progress);
        mIndeterminateProgressBar = (MaterialProgressBar) itemView.findViewById(R.id.indeterminate_progress_bar);
        mStatusStopped = (ImageView) itemView.findViewById(R.id.status_stopped);
        mStatusCompleted = (ImageView) itemView.findViewById(R.id.status_completed);
        mStatusDownloading = (RelativeLayout) itemView.findViewById(R.id.status_downloading);
        mSelectedTick = (ImageView) itemView.findViewById(R.id.tick);
    }

    public void setDownloadTitle(String title) {
        mDownloadTitle.setText(title);
    }

    public void setDownloadUrl(String url) {
        try {
            mDownloadUrl.setText(URLDecoder.decode(url, "UTF-8").replace("%20", " "));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDownloadUrl.setText(url.replace("%20", " "));
        }
    }

    public void setImageForView(Drawable icon) {
        mApplicationImageView.setImageDrawable(icon);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        itemView.setOnClickListener(onClickListener);
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        itemView.setOnLongClickListener(onLongClickListener);
    }

    public void setProgressBarState(boolean visibility, boolean indeterminate) {
        if (visibility) {
            if (indeterminate) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mIndeterminateProgressBar.setVisibility(View.VISIBLE);
            } else {
                mIndeterminateProgressBar.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mIndeterminateProgressBar.setVisibility(View.INVISIBLE);
        }
        mProgressBar.postInvalidate();
        mIndeterminateProgressBar.postInvalidate();
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
        try {
            mPercentageTextView.setText(String.format(Locale.getDefault(), "%d%%", progress));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStatus(DownloadInfo.Status status) {
        if (status == DownloadInfo.Status.Downloading) {
            mStatusDownloading.setVisibility(View.VISIBLE);
            mStatusStopped.setVisibility(View.GONE);
            mStatusCompleted.setVisibility(View.GONE);
        } else if (status == DownloadInfo.Status.Completed) {
            mStatusCompleted.setVisibility(View.VISIBLE);
            mStatusDownloading.setVisibility(View.GONE);
            mStatusStopped.setVisibility(View.GONE);
        } else if (status == DownloadInfo.Status.Stopped
                || status == DownloadInfo.Status.NetworkProblem
                || status == DownloadInfo.Status.NetworkNotAvailable) {
            mStatusStopped.setVisibility(View.VISIBLE);
            mStatusDownloading.setVisibility(View.GONE);
            mStatusCompleted.setVisibility(View.GONE);
        }
    }

    public void setSelectedTickVisibility(boolean visibility) {
        if (visibility) {
            mSelectedTick.setVisibility(View.VISIBLE);
        } else {
            mSelectedTick.setVisibility(View.GONE);
        }
    }
}
