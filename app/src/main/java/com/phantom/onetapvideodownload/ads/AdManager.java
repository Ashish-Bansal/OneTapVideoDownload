package com.phantom.onetapvideodownload.ads;

import android.app.Activity;
import android.view.View;

import com.phantom.onetapvideodownload.R;
import com.phantom.utils.Invokable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class AdManager implements Invokable<Ad.Response, Void>{
    private Queue<Ad> mAds;
    private Ad mCurrentAd;
    private View mAdContainer;
    private Activity mAdActivity;

    public AdManager(Activity activity) {
        mAdActivity = activity;
        mAdContainer = activity.findViewById(R.id.ad_container);
        mAdContainer.setVisibility(View.GONE);
        mAds = new LinkedBlockingQueue<>();
    }

    public void add(Ad ad) {
        mAds.add(ad);
    }

    public void processQueue() {
        if (mAds.isEmpty()) {
            return;
        }

        mCurrentAd = mAds.remove();
        mCurrentAd.loadAd(this, mAdContainer);
    }

    private void showAd() {
        mAdContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public Void invoke(final Ad.Response response) {
        mAdActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (response == Ad.Response.Success) {
                    showAd();
                } else {
                    processQueue();
                }
            }
        });
        return null;
    }

    public void destroy() {
        if (mCurrentAd != null) {
            mCurrentAd.destroy();
        }
    }
}
