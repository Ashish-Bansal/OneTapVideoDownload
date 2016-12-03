package com.phantom.onetapvideodownload.databasehandlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.phantom.onetapvideodownload.Video.BrowserVideo;
import com.phantom.onetapvideodownload.Video.Video;
import com.phantom.onetapvideodownload.Video.YoutubeVideo;

import java.util.ArrayList;
import java.util.List;

public class VideoDatabase extends SQLiteOpenHelper {
    public static final int VIDEO_TYPE_BROWSER = 0;
    public static final int VIDEO_TYPE_YOUTUBE = 1;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "VideoDatabase";

    private static final String TABLE_VIDEO_LIST = "video_list";
    private static final String TABLE_BROWSER_VIDEO_LIST = "browser_video_list";
    private static final String TABLE_YOUTUBE_VIDEO_LIST = "youtube_video_list";

    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_URL = "url";
    private static final String KEY_VIDEO_ID = "video_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_VIDEO_ITAG = "itag";
    private static final String KEY_PARAM = "param";
    private static final String KEY_PACKAGE_NAME = "package_name";

    private static VideoDatabase mVideoDatabase;
    private SQLiteDatabase mSQLiteDatabase;

    public static synchronized VideoDatabase getDatabase(Context context) {
        if (mVideoDatabase == null) {
            mVideoDatabase = new VideoDatabase(context);
            mVideoDatabase.mSQLiteDatabase = mVideoDatabase.getWritableDatabase();
        }

        return mVideoDatabase;
    }

    private VideoDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String urlListTable = "CREATE TABLE " + TABLE_VIDEO_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TYPE + " INTEGER)";

        String generalVideoList = "CREATE TABLE " + TABLE_BROWSER_VIDEO_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_VIDEO_ID + " INTEGER,"
                + KEY_URL + " TEXT,"
                + KEY_TITLE + " TEXT,"
                + KEY_PACKAGE_NAME + " TEXT )";

        String youtubeVideoList = "CREATE TABLE " + TABLE_YOUTUBE_VIDEO_LIST + "("
                + KEY_PARAM + " TEXT, "
                + KEY_VIDEO_ID + " INTEGER,"
                + KEY_VIDEO_ITAG + " INTEGER,"
                + KEY_URL + " TEXT,"
                + KEY_TITLE + " TEXT,"
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PACKAGE_NAME + " TEXT )";

        db.execSQL(urlListTable);
        db.execSQL(generalVideoList);
        db.execSQL(youtubeVideoList);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEO_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BROWSER_VIDEO_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOUTUBE_VIDEO_LIST);
        onCreate(db);
    }

    @Override
    protected void finalize() throws Throwable{
        super.finalize();
        if (mVideoDatabase != null) {
            mVideoDatabase.close();
        }

        if (mSQLiteDatabase != null) {
            mSQLiteDatabase.close();
        }
    }

    public long addOrUpdateVideo(Video video) {
        long id = alreadyExists(video);
        if (id == -1) {
            return addVideo(video);
        } else {
            return updateVideo(id, video);
        }
    }

    public long addVideo(Video video) {
        ContentValues values = new ContentValues();
        long videoId = -1;
        if (video instanceof BrowserVideo) {
            ContentValues videoListValues = new ContentValues();
            videoListValues.put(KEY_TYPE, VIDEO_TYPE_BROWSER);
            videoId = mSQLiteDatabase.insert(TABLE_VIDEO_LIST, null, videoListValues);
            assert(videoId != -1);

            values.put(KEY_TITLE, video.getTitle());
            values.put(KEY_URL, video.getUrl());
            values.put(KEY_VIDEO_ID, videoId);
            values.put(KEY_PACKAGE_NAME, video.getPackageName());
            mSQLiteDatabase.insert(TABLE_BROWSER_VIDEO_LIST, null, values);
        } else if (video instanceof YoutubeVideo) {
            ContentValues videoListValues = new ContentValues();
            videoListValues.put(KEY_TYPE, VIDEO_TYPE_YOUTUBE);
            mSQLiteDatabase.insert(TABLE_VIDEO_LIST, null, videoListValues);
            videoId = mSQLiteDatabase.insert(TABLE_VIDEO_LIST, null, videoListValues);
            assert(videoId != -1);

            YoutubeVideo youtubeVideo = (YoutubeVideo)video;
            values.put(KEY_TITLE, youtubeVideo.getTitle());
            values.put(KEY_VIDEO_ID, videoId);
            values.put(KEY_PARAM, youtubeVideo.getParam());
            values.put(KEY_PACKAGE_NAME, video.getPackageName());

            List<YoutubeVideo.Format> formats = youtubeVideo.getAllFormats();
            for(YoutubeVideo.Format format : formats) {
                values.put(KEY_URL, format.url);
                values.put(KEY_VIDEO_ID, videoId);
                values.put(KEY_VIDEO_ITAG, format.itag);
                mSQLiteDatabase.insert(TABLE_YOUTUBE_VIDEO_LIST, null, values);
            }
        }

        return videoId;
    }

    public Video getVideo(long videoId) {
        int categoryId = getCategory(videoId);
        return getVideo(categoryId, videoId);
    }

    public Video getVideo(int videoCategory, long videoId) {
        String videoQuery;
        Cursor videoQueryCursor = null;
        switch(videoCategory) {
            case VIDEO_TYPE_BROWSER :
                videoQuery = "SELECT * FROM " + TABLE_BROWSER_VIDEO_LIST + " WHERE "
                        + KEY_VIDEO_ID + "=" + videoId;
                videoQueryCursor = mSQLiteDatabase.rawQuery(videoQuery, null);
                if (videoQueryCursor.moveToFirst()) {
                    String url = videoQueryCursor.getString(2);
                    String title = videoQueryCursor.getString(3);
                    String packageName = videoQueryCursor.getString(4);
                    Video video = new BrowserVideo(url, title);
                    video.setDatabaseId(videoId);
                    video.setPackageName(packageName);
                    videoQueryCursor.close();
                    return video;
                }
                break;
            case VIDEO_TYPE_YOUTUBE :
                videoQuery = "SELECT * FROM " + TABLE_YOUTUBE_VIDEO_LIST
                        + " WHERE " + KEY_VIDEO_ID + "=" + videoId;
                videoQueryCursor = mSQLiteDatabase.rawQuery(videoQuery, null);
                if (videoQueryCursor.moveToFirst()) {
                    String title = videoQueryCursor.getString(4);
                    String param = videoQueryCursor.getString(0);
                    String packageName = videoQueryCursor.getString(6);
                    YoutubeVideo youtubeVideo = new YoutubeVideo(title, param);
                    youtubeVideo.setDatabaseId(videoId);
                    youtubeVideo.setPackageName(packageName);
                    do {
                        int itag = videoQueryCursor.getInt(2);
                        String url = videoQueryCursor.getString(3);
                        youtubeVideo.addFormat(url, itag);
                    } while (videoQueryCursor.moveToNext());
                    videoQueryCursor.close();
                    return youtubeVideo;
                }
                break;
        }

        if (videoQueryCursor != null) {
            videoQueryCursor.close();
        }
        return null;
    }

    public List<Video> getAllVideos() {
        List<Video> videoList = new ArrayList<>();
        videoList.addAll(getVideosOfType(VIDEO_TYPE_YOUTUBE));
        videoList.addAll(getVideosOfType(VIDEO_TYPE_BROWSER));
        return videoList;
    }

    public List<Video> getVideosOfType(int categoryType) {
        List<Video> videoList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_VIDEO_LIST
                + " WHERE " + KEY_TYPE + "=" + categoryType;

        Cursor videoListCursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if (videoListCursor.moveToFirst()) {
            do {
                int videoId = videoListCursor.getInt(0);
                int videoCategory = videoListCursor.getInt(1);
                Video video = getVideo(videoCategory, videoId);
                if (video != null) {
                    video.setDatabaseId(videoId);
                    videoList.add(video);
                }
            } while (videoListCursor.moveToNext());
        }

        videoListCursor.close();
        return videoList;
    }

    public int getVideoCount() {
        String countQuery = "SELECT  * FROM " + TABLE_VIDEO_LIST;
        Cursor cursor = mSQLiteDatabase.rawQuery(countQuery, null);
        int videoCount = cursor.getCount();
        cursor.close();

        return videoCount;
    }

    public long alreadyExists(Video video) {
        List<Video> videos;
        if (video instanceof BrowserVideo) {
            videos = getVideosOfType(VIDEO_TYPE_BROWSER);
            for(Video v : videos) {
                if (v.getUrl().equals(video.getUrl())) {
                    return v.getDatabaseId();
                }
            }
        } else if (video instanceof YoutubeVideo) {
            videos = getVideosOfType(VIDEO_TYPE_YOUTUBE);
            for(Video v : videos) {
                if (((YoutubeVideo)v).getParam().equals(((YoutubeVideo)video).getParam())) {
                    return v.getDatabaseId();
                }
            }
        }

        return -1;
    }

    public long updateVideo(long id, Video video) {
        deleteVideo(id);
        return addVideo(video);
    }

    public int getCategory(long videoId) {
        String selectQuery = "SELECT * FROM " + TABLE_VIDEO_LIST
                + " WHERE " + KEY_ID + "=" + videoId;

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            int categoryId = cursor.getInt(1);
            cursor.close();
            return categoryId;
        }

        cursor.close();
        return -1;
    }

    public void deleteVideo(long id) {
        int categoryId = getCategory(id);
        if (categoryId == -1) {
            return;
        }

        switch (categoryId) {
            case VIDEO_TYPE_BROWSER :
                mSQLiteDatabase.delete(TABLE_BROWSER_VIDEO_LIST, KEY_VIDEO_ID + " = ?",
                        new String[] { String.valueOf(id) });
                break;
            case VIDEO_TYPE_YOUTUBE :

                mSQLiteDatabase.delete(TABLE_YOUTUBE_VIDEO_LIST, KEY_VIDEO_ID + " = ?",
                        new String[] { String.valueOf(id) });
                break;
        }

        mSQLiteDatabase.delete(TABLE_VIDEO_LIST, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    public void clearDatabase() {
        mSQLiteDatabase.delete(TABLE_VIDEO_LIST, null, null);
        mSQLiteDatabase.delete(TABLE_BROWSER_VIDEO_LIST, null, null);
        mSQLiteDatabase.delete(TABLE_YOUTUBE_VIDEO_LIST, null, null);
    }
}
