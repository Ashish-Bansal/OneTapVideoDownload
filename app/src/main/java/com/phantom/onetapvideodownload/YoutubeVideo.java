package com.phantom.onetapvideodownload;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class YoutubeVideo {
    private String mTitle;

    private class Format {
        public String extension;
        public String videoUrl;
        public String formatDescription;
    }

    private ArrayList<Format> mFormatList = new ArrayList<>();

    public YoutubeVideo(String title) {
        mTitle = title;
    }

    public void addFormat(String extension, String url, String formatDescription) {
        Format format = new Format();
        format.extension = extension;
        format.videoUrl = url;
        format.formatDescription = formatDescription;
        mFormatList.add(format);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getFormatDescription(int index) {
        return mFormatList.get(index).formatDescription;
    }

    public String getVideoUrl(int index) {
        return mFormatList.get(index).videoUrl;
    }

    public String getExtension(int index) {
        return mFormatList.get(index).extension;
    }

    public boolean urlsForbidden() {
        if (mFormatList.size() <= 0) {
            return false;
        }

        URL url;
        HttpURLConnection urlConnection;
        try {
            url = new URL(mFormatList.get(0).videoUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode/100 == 2) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
