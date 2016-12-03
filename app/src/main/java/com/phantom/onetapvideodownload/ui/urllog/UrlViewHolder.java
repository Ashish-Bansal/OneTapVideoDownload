package com.phantom.onetapvideodownload.ui.urllog;

import android.view.View;
import android.widget.TextView;

import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.Video.Video;

import co.dift.ui.SwipeToAction;

class UrlViewHolder extends SwipeToAction.ViewHolder<Video> {
    private TextView mUrlView;
    private TextView mMetadataView;

    UrlViewHolder(View v) {
        super(v);
        mUrlView = (TextView) v.findViewById(R.id.url);
        mMetadataView = (TextView) v.findViewById(R.id.metadata);
    }

    void setUrlText(String url) {
        mUrlView.setText(url);
    }

    void setMetadataText(String metadata) {
        mMetadataView.setText(metadata);
    }
}