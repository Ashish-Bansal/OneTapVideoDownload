package com.phantom.onetapvideodownload.ads;

import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.phantom.utils.Invokable;

public class MoPubAd implements Ad, MoPubView.BannerAdListener {
    private final static String TAG = "MoPubAd";
    private MoPubView mAdView;
    private Context mContext;
    private Invokable mInvokable;

    public MoPubAd(Context context) {
        mContext = context;
    }

    @Override
    public void loadAd(Invokable<Response, Void> invokable, RelativeLayout adContainer) {
        Log.d(TAG, "Trying to load Facebook Banner Ad");

        mAdView = new MoPubView(mContext);
        mAdView.setAdUnitId("3ec5efe54bf04719b1f8706486f3c2d9");
        mAdView.setBannerAdListener(this);

        mInvokable = invokable;
        adContainer.addView(mAdView);
        mAdView.loadAd();
    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        Log.i("TAG", "MoPubAd Loaded successfully");
        if (mInvokable == null) {
            Log.e(TAG, "Invokable object is null; Returning");
            return;
        }

        mInvokable.invoke(Response.Success);
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        Log.e(TAG, "Unable to load MoPub Banner Ad");
        Log.e(TAG, errorCode.toString());
        if (mInvokable == null) {
            Log.e(TAG, "Invokable object is null; Returning");
            return;
        }

        mInvokable.invoke(Response.Failed);
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
    }

    @Override
    public void onBannerExpanded(MoPubView banner) {
    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {
    }

    public void destroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
    }
}
