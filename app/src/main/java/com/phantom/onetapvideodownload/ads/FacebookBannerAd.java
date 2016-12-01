package com.phantom.onetapvideodownload.ads;

import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;

import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.phantom.utils.Invokable;

public class FacebookBannerAd implements Ad, AdListener {
    private AdView mAdView;
    private Context mContext;
    private Invokable mInvokable;
    private static final String PLACEMENT_ID = "1076595859066838_1221421064584316";
    private static final String TAG = "FacebookBannerAd";

    public FacebookBannerAd(Context context) {
        mContext = context;
    }

    @Override
    public void loadAd(Invokable<Ad.Response, Void> invokable, RelativeLayout adContainer) {
        Log.d(TAG, "Trying to load Facebook Banner Ad");
        AdSettings.addTestDevice("7bb256905151fbeea6ba45b920643bf5");
        mAdView = new AdView(mContext, PLACEMENT_ID, AdSize.BANNER_HEIGHT_50);
        mAdView.setAdListener(this);
        mAdView.loadAd();
        adContainer.addView(mAdView);
        mInvokable = invokable;
    }

    @Override
    public void onError(com.facebook.ads.Ad ad, AdError error) {
        Log.e(TAG, "Unable to load Facebook Banner Ad");
        Log.e(TAG, error.getErrorMessage());
        if (mInvokable == null) {
            Log.e(TAG, "Invokable object is null; Returning");
            return;
        }

        mInvokable.invoke(Response.Failed);
    }

    @Override
    public void onAdLoaded(com.facebook.ads.Ad ad) {
        Log.d(TAG, "Facebook Banner Ad loaded successfully!");
        if (mInvokable == null) {
            Log.e(TAG, "Invokable object is null; Returning");
            return;
        }

        mInvokable.invoke(Response.Success);
    }

    @Override
    public void onAdClicked(com.facebook.ads.Ad ad) {
    }

    @Override
    public void destroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
    }
}
