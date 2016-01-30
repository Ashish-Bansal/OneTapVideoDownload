package com.phantom.videoplayerselect;

public class Url {
    private String mUrl, mMetadata;

    public Url(String url, String metadata) {
        this.mUrl= url;
        this.mMetadata = metadata;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getMetadata() {
        return mMetadata;
    }
}
