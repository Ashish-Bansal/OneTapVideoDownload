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

    @Override
    public boolean equals(Object urlObj) {
        if (urlObj == null) {
            return false;
        }

        Url otherUrl = (Url) urlObj;
        if (mUrl.equals(otherUrl.getUrl()) && mMetadata.equals(otherUrl.getMetadata())) {
            return true;
        }
        return false;
    }

}
