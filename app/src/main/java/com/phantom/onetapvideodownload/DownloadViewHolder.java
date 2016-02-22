package com.phantom.onetapvideodownload;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DownloadViewHolder extends RecyclerView.ViewHolder {
    private ImageView mApplicationImageView;
    private TextView mDownloadTitle;
    private TextView mDownloadUrl;

    public DownloadViewHolder(View v) {
        super(v);
        mApplicationImageView = (ImageView)itemView.findViewById(R.id.application_icon);
        mDownloadTitle = (TextView)itemView.findViewById(R.id.download_title);
        mDownloadUrl = (TextView)itemView.findViewById(R.id.download_url);
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
}
