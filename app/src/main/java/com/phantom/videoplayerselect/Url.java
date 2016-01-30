package com.phantom.videoplayerselect;

import android.util.Log;

import java.text.DateFormat;
import java.util.Date;

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

    public int compareTo(Url urlObj) {
        Url url = (Url)urlObj;
        try {
            Date thisDate = DateFormat.getDateTimeInstance().parse(mMetadata);
            Date urlObjDate = DateFormat.getDateTimeInstance().parse(url.getMetadata());
            return thisDate.compareTo(urlObjDate);
        } catch(java.text.ParseException e) {
            Log.e("URL", e.getStackTrace().toString());
        }
        return 0;
    }
}
