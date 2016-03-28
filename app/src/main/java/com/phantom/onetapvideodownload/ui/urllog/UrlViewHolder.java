package com.phantom.onetapvideodownload.ui.urllog;

import android.view.View;
import android.widget.TextView;

import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.Video.Video;

import co.dift.ui.SwipeToAction;

public class UrlViewHolder extends SwipeToAction.ViewHolder<Video> {
    private TextView mUrlView;
    private TextView mMetadataView;

    public UrlViewHolder(View v) {
        super(v);
        mUrlView = (TextView) v.findViewById(R.id.url);
        mMetadataView = (TextView) v.findViewById(R.id.metadata);
    }

    public void setUrlText(String url) {
        mUrlView.setText(url);
    }

    public void setMetadataText(String metadata) {
        mMetadataView.setText(metadata);
    }
}