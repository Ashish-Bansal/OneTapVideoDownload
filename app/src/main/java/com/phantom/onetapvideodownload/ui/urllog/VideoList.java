package com.phantom.onetapvideodownload.ui.urllog;

import android.content.Context;

import com.phantom.onetapvideodownload.Video.Video;
import com.phantom.onetapvideodownload.databasehandlers.VideoDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class VideoList {
    private VideoList() {
    }

    public static final String PREFS_NAME = "SavedUrls";
    private static VideoList mUrlList;
    private List<Video> mList = new ArrayList<>();
    private static VideoDatabase mVideoDatabase;

    static VideoList getVideoListSingleton(Context context) {
        if (mUrlList != null) {
            return mUrlList;
        }

        mUrlList = new VideoList();
        mVideoDatabase = VideoDatabase.getDatabase(context);
        mUrlList.loadSavedVideos();

        return mUrlList;
    }

    void sortList() {
        Collections.sort(mList, new Comparator<Video>() {
            @Override
            public int compare(Video lhs, Video rhs) {
                return lhs.getDatabaseId() < rhs.getDatabaseId() ? 1 : 0;
            }
        });
    }

    void addVideo(Video video) {
        long videoId = mVideoDatabase.addOrUpdateVideo(video);
        video.setDatabaseId(videoId);
        sortList();
    }

    void removeVideo(Video video) {
        mVideoDatabase.deleteVideo(video.getDatabaseId());
        mList.remove(video);
    }

    int size() {
        return mList.size();
    }

    Video getVideo(int pos) {
        return mList.get(pos);
    }

    void clearLocalList() {
        mList.clear();
    }

    void clearSavedVideos() {
        mVideoDatabase.clearDatabase();
    }

    void loadSavedVideos() {
        mList = mVideoDatabase.getAllVideos();
    }

    ArrayList<Video> getVideoList() {
        return new ArrayList<>(mList);
    }

    boolean isEmpty() {
        return size() <= 0;
    }

    void reloadVideos() {
        clearLocalList();
        loadSavedVideos();
    }
}
