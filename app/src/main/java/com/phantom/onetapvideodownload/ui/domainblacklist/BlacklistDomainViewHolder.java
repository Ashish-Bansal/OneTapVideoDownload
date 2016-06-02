package com.phantom.onetapvideodownload.ui.domainblacklist;

import android.view.View;
import android.widget.TextView;

import com.phantom.onetapvideodownload.R;

import co.dift.ui.SwipeToAction;

public class BlacklistDomainViewHolder extends SwipeToAction.ViewHolder<String> {
    private TextView mUrlView;

    public BlacklistDomainViewHolder(View v) {
        super(v);
        mUrlView = (TextView) v.findViewById(R.id.url);
    }

    public void setUrlText(String url) {
        mUrlView.setText(url);
    }
}