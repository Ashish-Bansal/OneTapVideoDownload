package com.phantom.onetapvideodownload.ads;

import android.view.View;

import com.phantom.utils.Invokable;

interface Ad {
    enum Response {
        Success, Failed
    }

    void loadAd(Invokable<Response, Void> invokable, View adContainer);
    void destroy();
}
