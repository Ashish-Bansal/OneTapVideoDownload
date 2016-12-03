package com.phantom.onetapvideodownload.UriMediaChecker;

import android.content.Context;

import com.phantom.onetapvideodownload.Video.BrowserVideo;
import com.phantom.onetapvideodownload.Video.Video;
import com.phantom.utils.Global;

import org.json.JSONObject;

public class VimeoUriChecker implements AbstractUriChecker {
    private Context mContext;

    public VimeoUriChecker(Context context) {
        mContext = context;
    }
    @Override
    public Video checkUrl(String url) {
        if (!url.contains("player.vimeo.com")) {
            return null;
        }

        try {
            String vimeoConfig = Global.getResponseBody(url);
            JSONObject json = new JSONObject(vimeoConfig);
            String videoUrl = json.getJSONObject("request")
                    .getJSONObject("files")
                    .getJSONArray("progressive")
                    .getJSONObject(0)
                    .getString("url");

            if (videoUrl != null && !videoUrl.isEmpty()) {
                return new BrowserVideo(videoUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
