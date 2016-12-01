package com.phantom.onetapvideodownload.ads;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.phantom.onetapvideodownload.R;
import com.phantom.utils.Invokable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class AdManager implements Invokable<Ad.Response, Void>{
    private Queue<Ad> mAds;
    private Ad mCurrentAd;
    private RelativeLayout mAdContainer;
    private Activity mAdActivity;
    private final static String TAG = "AdManager";

    public AdManager(Activity activity) {
        mAdActivity = activity;
        mAdContainer = (RelativeLayout) activity.findViewById(R.id.ad_container);
        mAdContainer.setVisibility(View.GONE);
        mAds = new LinkedBlockingQueue<>();
    }

    public void add(Ad ad) {
        mAds.add(ad);
    }

    public void processQueue() {
        if (mAds.isEmpty()) {
            Log.i(TAG, "Ad Queue is empty! Returning.");
            return;
        }

        mCurrentAd = mAds.remove();
        mAdContainer.removeAllViews();
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
