package com.phantom.onetapvideodownload;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collection;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class DownloadViewHolder extends RecyclerView.ViewHolder {
    private ImageView mApplicationImageView;
    private TextView mDownloadTitle;
    private TextView mDownloadUrl;
    private Context mContext;
    private View mView;
    private MaterialProgressBar mProgressBar;

    public DownloadViewHolder(View v) {
        super(v);
        mView = v;
        mContext = v.getContext();
        mApplicationImageView = (ImageView)itemView.findViewById(R.id.application_icon);
        mDownloadTitle = (TextView)itemView.findViewById(R.id.download_title);
        mDownloadUrl = (TextView)itemView.findViewById(R.id.download_url);
        mProgressBar = (MaterialProgressBar) itemView.findViewById(R.id.download_progress);
    }

    public void setDownloadTitle(String title) {
        mDownloadTitle.setText(title);
    }

    public void setDownloadUrl(String url) {
        mDownloadUrl.setText(url);
    }

    public void setImageForView(Drawable icon) {
        mApplicationImageView.setImageDrawable(icon);
    }

    public void setOnClickListener(final Collection<String> options, final MaterialDialog.ListCallback callback) {
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(mContext)
                        .items(options)
                        .itemsCallback(callback)
                        .show();
            }
        });
    }

    public void setProgressBarVisibility(boolean visibility) {
        if (visibility) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setProgressBarState(boolean indeterminate) {
        mProgressBar.setIndeterminate(indeterminate);
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }
}
