package com.phantom.onetapvideodownload.ads;

import android.widget.RelativeLayout;

import com.phantom.utils.Invokable;

interface Ad {
    enum Response {
        Success, Failed
    }

    void loadAd(Invokable<Response, Void> invokable, RelativeLayout adContainer);
    void destroy();
}
