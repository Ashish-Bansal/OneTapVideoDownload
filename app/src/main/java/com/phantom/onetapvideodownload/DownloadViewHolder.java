package com.phantom.onetapvideodownload;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.net.URLDecoder;
import java.util.Collection;
import java.util.Locale;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class DownloadViewHolder extends RecyclerView.ViewHolder {
    private ImageView mApplicationImageView;
    private TextView mDownloadTitle, mDownloadUrl, mPercentageTextView;
    private Context mContext;
    private View mView;
    private MaterialProgressBar mIndeterminateProgressBar;
    private MaterialProgressBar mProgressBar;

    public DownloadViewHolder(View v) {
        super(v);
        mView = v;
        mContext = v.getContext();
        mApplicationImageView = (ImageView)itemView.findViewById(R.id.application_icon);
        mDownloadTitle = (TextView)itemView.findViewById(R.id.download_title);
        mDownloadUrl = (TextView)itemView.findViewById(R.id.download_url);
        mPercentageTextView = (TextView)itemView.findViewById(R.id.percentage);
        mProgressBar = (MaterialProgressBar) itemView.findViewById(R.id.download_progress);
        mIndeterminateProgressBar = (MaterialProgressBar) itemView.findViewById(R.id.indeterminate_progress_bar);
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
}
