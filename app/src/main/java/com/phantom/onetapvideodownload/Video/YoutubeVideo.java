package com.phantom.onetapvideodownload.Video;

import android.support.v4.util.Pair;
import android.support.v4.util.SparseArrayCompat;

import com.phantom.onetapvideodownload.Global;

import java.util.ArrayList;
import java.util.List;

public class YoutubeVideo implements Video {
    private String mTitle, mParam;
    private long mDatabaseId = -1;
    public static List<Pair<Integer, String>> itagMapping = new ArrayList<>();

    static {
        itagMapping.add(Pair.create(22, "MP4 - 720p"));
        itagMapping.add(Pair.create(18, "MP4 - 360p"));
        itagMapping.add(Pair.create(43, "WebM - 360p"));
        itagMapping.add(Pair.create(36, "3GB - 240p"));
        itagMapping.add(Pair.create(5, "FLV - 240p"));
        itagMapping.add(Pair.create(17, "3GP - 144p"));
        itagMapping.add(Pair.create(141, "M4A - 256 kbit/s"));
        itagMapping.add(Pair.create(140, "M4A - 128 kbit/s"));
        itagMapping.add(Pair.create(251, "WebM - 160 kbit/s"));
        itagMapping.add(Pair.create(171, "WebM - 128 kbit/s"));
        itagMapping.add(Pair.create(250, "WebM - 64 kbit/s"));
        itagMapping.add(Pair.create(249, "WebM - 48 kbit/s"));
    }

    public class Format {
        public int itag;
        public String url;
        public boolean dashAudio;
    }

    private SparseArrayCompat<Format> mFormatList = new SparseArrayCompat<>();

    public YoutubeVideo(String title, String param) {
        mTitle = title;
        mParam = param;
    }

    public void addFormat(String videoUrl, int itag) {
        Format format = new Format();
        format.url = videoUrl;
        format.itag = itag;
        format.dashAudio = false;
        for (Pair p : itagMapping) {
            if (p.first == itag && p.second.toString().contains("kbit")) {
                format.dashAudio = true;
            }
        }
        mFormatList.put(itag, format);
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getUrl() {
        return getBestVideoFormat().url;
    }

    @Override
    public long getDatabaseId() {
        return mDatabaseId;
    }

    @Override
    public void setDatabaseId(long databaseId) {
        mDatabaseId = databaseId;
    }

    public String getParam() {
        return mParam;
    }

    public String getFormatDescription(int itag) {
        for (Pair p : itagMapping) {
            if (p.first == itag) {
                return p.second.toString();
            }
        }

        return "No Description Found";
    }

    public String getVideoUrl(int index) {
        Format format = mFormatList.get(index);
        if (format == null) {
            return "";
        }

        return format.url;
    }

    public boolean urlsForbidden() {
        return mFormatList.size() <= 0 && Global.isValidUrl(mFormatList.get(0).url);
    }

    public Format getBestVideoFormat() {
        for (Pair p : itagMapping) {
            if (p.second.toString().contains("kbit")) {
                continue;
            }

            Format format = mFormatList.get((int) p.first);
            if (format != null) {
                return format;
            }
        }
        return null;
    }

    public Format getBestAudioFormat() {
        for (Pair p : itagMapping) {
            if (!p.second.toString().contains("kbit")) {
                continue;
            }

            Format format = mFormatList.get((int) p.first);
            if (format != null) {
                return format;
            }
        }
        return null;
    }

    public ArrayList<Format> getAllFormats() {
        ArrayList<Format> formats = new ArrayList<>();
        for (Pair p : itagMapping) {
            Format format = mFormatList.get((int) p.first);
            if (format != null) {
                formats.add(format);
            }
        }
        return formats;
    }
}
