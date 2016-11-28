package com.phantom.onetapvideodownload.ads;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.millennialmedia.InlineAd;
import com.phantom.utils.Invokable;

public class MillennialBannerAd implements Ad, InlineAd.InlineListener {
    private final static String PLACEMENT_ID = "229962";
    private InlineAd mInlineAd;
    private Invokable mInvokable;
    private Context mContext;

    public final static String TAG = "MillennialBannerAd";

    public MillennialBannerAd(Context context) {
        mContext = context;
    }

    @Override
    public void loadAd(Invokable<Ad.Response, Void> invokable, View adContainer) {
        Log.d(TAG, "Trying to load Millennial Banner Ad");
        try {
            mInlineAd = InlineAd.createInstance(PLACEMENT_ID, (ViewGroup) adContainer);
            mInlineAd.setListener(this);
        } catch (com.millennialmedia.MMException e) {
            e.printStackTrace();
        }

        mInvokable = invokable;
        if (mInlineAd != null) {
            //The AdRequest instance is used to pass additional metadata to the server to improve ad selection
            final InlineAd.InlineAdMetadata inlineAdMetadata = new InlineAd.InlineAdMetadata().
                    setAdSize(InlineAd.AdSize.BANNER);

            //Request ads from the server.  If automatic refresh is enabled for your placement new ads will be shown
            //automatically
            mInlineAd.request(inlineAdMetadata);
        } else {
            // emit failure to process other ads
            onRequestFailed(null, null);
        }
    }

    @Override
    public void onRequestSucceeded(InlineAd inlineAd) {
        Log.d(TAG, "Inline Banner Ad loaded.");
        if (mInvokable == null) {
            Log.e(TAG, "Invokable object is null; Returning");
            return;
        }

        mInvokable.invoke(Response.Success);
    }

    @Override
    public void onRequestFailed(InlineAd inlineAd, InlineAd.InlineErrorStatus errorStatus) {
        Log.e(TAG, "Failed to load Millennial Banner Ad.");
        Log.e(TAG, errorStatus.toString());
        if (mInvokable == null) {
            Log.e(TAG, "Invokable object is null; Returning");
            return;
        }

        mInvokable.invoke(Response.Failed);
    }

    @Override
    public void onClicked(InlineAd inlineAd) {
        Log.i(TAG, "Inline Banner Ad clicked.");
    }

    @Override
    public void onResize(InlineAd inlineAd, int width, int height) {
        Log.i(TAG, "Inline Banner Ad starting resize.");
    }

    @Override
    public void onResized(InlineAd inlineAd, int width, int height, boolean toOriginalSize) {
        Log.i(TAG, "Inline Banner Ad resized.");
    }

    @Override
    public void onExpanded(InlineAd inlineAd) {
        Log.i(TAG, "Inline Banner Ad expanded.");
    }

    @Override
    public void onCollapsed(InlineAd inlineAd) {
        Log.i(TAG, "Inline Banner Ad collapsed.");
    }

    @Override
    public void onAdLeftApplication(InlineAd inlineAd) {
        Log.i(TAG, "Inline Banner Ad left application.");
    }

    @Override
    public void destroy() {
    }

}
