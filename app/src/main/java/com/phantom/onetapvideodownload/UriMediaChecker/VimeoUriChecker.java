package com.phantom.onetapvideodownload.UriMediaChecker;

import com.phantom.onetapvideodownload.utils.Global;

import org.json.JSONObject;

public class VimeoUriChecker implements AbstractUriChecker {

    @Override
    public String checkUrl(String url) {
        if (!url.contains("player.vimeo.com")) {
            return null;
        }

        try {
            String vimeoConfig = Global.getResponseBody(url);
            JSONObject json = new JSONObject(vimeoConfig);
            return json.getJSONObject("request")
                    .getJSONObject("files")
                    .getJSONArray("progressive")
                    .getJSONObject(0)
                    .getString("url");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
