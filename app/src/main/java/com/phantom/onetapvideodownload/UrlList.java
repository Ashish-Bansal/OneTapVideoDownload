package com.phantom.onetapvideodownload;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Collections;
import java.util.Vector;

public class UrlList {
    private UrlList() {
    }

    public static final String PREFS_NAME = "SavedUrls";
    public static UrlList mUrlList;
    Vector<Url> mList = new Vector<>();
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

    public void loadUrlIntoList(Url url) {
        mList.add(url);
        Collections.sort(mList, new UrlComparator());
    }

    public void addUrl(Url url) {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        int urlSavedCount = settings.getInt("count", 0);
        for (int i = 0; i < urlSavedCount * 2; i += 2) {
            String savedUrl = settings.getString(Integer.toString(i + 1), "");
            if (savedUrl.equals(url.getUrl())) {
                editor.putString(Integer.toString(i + 2), url.getMetadata());
                return;
            }
        }

        editor.putString(Integer.toString(urlSavedCount * 2 + 1), url.getUrl());
        editor.putString(Integer.toString(urlSavedCount * 2 + 2), url.getMetadata());
        editor.putInt("count", urlSavedCount + 1);
        editor.apply();
        loadUrlIntoList(url);
    }

    public int removeUrl(Url url) {
        int pos = -1;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.elementAt(i).equals(url)) {
                pos = i;
                break;
            }
        }

        assert pos != -1;

        removeUrlFromSavedPerf(url.getUrl(), url.getMetadata());
        mList.remove(pos);
        return pos;
    }

    public int size() {
        return mList.size();
    }

    public Url getUrl(int pos) {
        assert pos < mList.size();
        return mList.get(pos);
    }


    public void clearLocalList() {
        mList.clear();
    }

    public void clearSavedList() {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    public void loadSavedUrls() {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        int urlSavedCount = settings.getInt("count", 0);
        for (int i = 0; i < urlSavedCount * 2; i += 2) {
            String url = settings.getString(Integer.toString(i + 1), "");
            String metadata = settings.getString(Integer.toString(i + 2), "");
            if (url.isEmpty() || metadata.isEmpty()) {
                continue;
            }

            Log.e("URLList", url);
            Url urlObj = new Url(url, metadata);
            loadUrlIntoList(urlObj);
        }
    }

    public void removeUrlFromSavedPerf(String url, String metadata) {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        int urlSavedCount = settings.getInt("count", 0);
        for (int i = 0; i < urlSavedCount * 2; i += 2) {
            String savedUrl = settings.getString(Integer.toString(i + 1), "");
            String savedMetadata = settings.getString(Integer.toString(i + 2), "");
            if (savedUrl.equals(url) && savedMetadata.equals(metadata)) {
                editor.remove(Integer.toString(i + 1));
                editor.remove(Integer.toString(i + 2));
            }
        }
        editor.apply();
    }

    public Vector<Url> getUrlList() {
        return new Vector<Url>(mList);
    }

    public boolean isEmpty() {
        return size() > 0 ? false : true;
    }
}
