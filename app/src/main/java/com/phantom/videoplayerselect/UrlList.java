package com.phantom.videoplayerselect;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class UrlList {
    private UrlList() {}
    public static final String PREFS_NAME = "SavedUrls";
    public static UrlList mUrlList;
    List<Url> mList = new ArrayList<>();
    private Context mContext;

    public static UrlList getUrlListSingleton(Context context) {
        if (mUrlList != null) {
            return mUrlList;
        }

        mUrlList = new UrlList();
        mUrlList.mContext = context;
        mUrlList.loadSavedUrls();

        return mUrlList;
    }

    public void addUrl(Url url) {
        mList.add(url);

    }

    public void addUrl(int pos, Url url) {
        mList.add(pos, url);
    }

    public int removeUrl(int pos) {
        assert pos < mList.size();
        mList.remove(pos);
        return pos;
    }

    public int removeUrl(Url url) {
        int pos = mList.indexOf(url);
        assert pos != -1;

        mList.remove(pos);
        removeUrlFromSavedPerf(pos);
        return pos;
    }

    public int size() {
        return mList.size();
    }

    public Url getUrl(int pos) {
        assert pos < mList.size();
        return mList.get(pos);
    }

    public void loadSavedUrls() {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        int urlSavedCount = settings.getInt("count", 0);
        for(int i = 0; i < urlSavedCount*2; i+=2) {
            String url = settings.getString(Integer.toString(i+1), "");
            String metadata = settings.getString(Integer.toString(i+2), "");
            if (url.isEmpty() || metadata.isEmpty()) {
                continue;
            }

            Url urlObj = new Url(url, metadata);
            addUrl(urlObj);
        }
    }

    public void removeUrlFromSavedPerf(int pos) {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(Integer.toString(pos*2+1));
        editor.remove(Integer.toString(pos*2+2));
        editor.apply();
    }
}
