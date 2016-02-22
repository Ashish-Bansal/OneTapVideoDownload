package com.phantom.onetapvideodownload.databasehandlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.phantom.onetapvideodownload.downloader.downloadinfo.BrowserDownloadInfo;
import com.phantom.onetapvideodownload.downloader.downloadinfo.DownloadInfo;
import com.phantom.onetapvideodownload.downloader.downloadinfo.YoutubeDownloadInfo;

import java.util.ArrayList;
import java.util.List;

public class DownloadDatabase extends SQLiteOpenHelper {
    private static final int DOWNLOAD_TYPE_BROWSER = 0;
    private static final int DOWNLOAD_TYPE_YOUTUBE = 1;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DownloadDatabase";

    private static final String TABLE_VIDEO_DOWNLOAD_LIST = "download_list";
    private static final String TABLE_BROWSER_DOWNLOAD_LIST = "browser_download_list";
    private static final String TABLE_YOUTUBE_DOWNLOAD_LIST = "youtube_download_list";

    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_URL = "url";
    private static final String KEY_VIDEO_ID = "video_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_VIDEO_ITAG = "itag";
    private static final String KEY_PARAM = "param";
    private static final String KEY_DOWNLOAD_LOCATION = "download_path";

    private static DownloadDatabase mDownloadDatabase;
    private Context mContext;

    public static DownloadDatabase getDatabase(Context context) {
        if (mDownloadDatabase == null) {
            mDownloadDatabase = new DownloadDatabase(context);
            mDownloadDatabase.mContext = context;
        }

        return mDownloadDatabase;
    }

    private DownloadDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String downloadListTable = "CREATE TABLE " + TABLE_VIDEO_DOWNLOAD_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TYPE + " INTEGER )";

        String browserDownloadListTable = "CREATE TABLE " + TABLE_BROWSER_DOWNLOAD_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_VIDEO_ID + " INTEGER,"
                + KEY_URL + " TEXT,"
                + KEY_DOWNLOAD_LOCATION + " TEXT,"
                + KEY_FILENAME + " TEXT" + ")";

        String youtubeDownloadListTable = "CREATE TABLE " + TABLE_YOUTUBE_DOWNLOAD_LIST + "("
                + KEY_PARAM + " TEXT, "
                + KEY_VIDEO_ID + " INTEGER,"
                + KEY_VIDEO_ITAG + " INTEGER,"
                + KEY_URL + " TEXT,"
                + KEY_FILENAME + " TEXT,"
                + KEY_DOWNLOAD_LOCATION + " TEXT,"
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT)";

        db.execSQL(downloadListTable);
        db.execSQL(browserDownloadListTable);
        db.execSQL(youtubeDownloadListTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEO_DOWNLOAD_LIST);
        onCreate(db);
    }

    public long addOrUpdateDownload(DownloadInfo download) {
        long id = downloadAlreadyExists(download);
        if (id == -1) {
            return addDownload(download);
        } else {
            return updateDownload(id, download);
        }
    }

    public long addDownload(DownloadInfo download) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        long downloadId = -1;
        if (download instanceof BrowserDownloadInfo) {
            ContentValues videoListValues = new ContentValues();
            videoListValues.put(KEY_TYPE, DOWNLOAD_TYPE_BROWSER);
            downloadId = db.insert(TABLE_VIDEO_DOWNLOAD_LIST, null, videoListValues);
            assert(downloadId != -1);

            values.put(KEY_FILENAME, download.getFilename());
            values.put(KEY_URL, download.getUrl());
            values.put(KEY_VIDEO_ID, downloadId);
            values.put(KEY_DOWNLOAD_LOCATION, download.getDownloadLocation());
            db.insert(TABLE_BROWSER_DOWNLOAD_LIST, null, values);
        } else if (download instanceof YoutubeDownloadInfo) {
            ContentValues downloadListValues = new ContentValues();
            downloadListValues.put(KEY_TYPE, DOWNLOAD_TYPE_YOUTUBE);
            db.insert(TABLE_VIDEO_DOWNLOAD_LIST, null, downloadListValues);
            downloadId = db.insert(TABLE_VIDEO_DOWNLOAD_LIST, null, downloadListValues);
            assert(downloadId != -1);

            values.put(KEY_FILENAME, download.getFilename());
            values.put(KEY_VIDEO_ID, downloadId);
            values.put(KEY_DOWNLOAD_LOCATION, download.getDownloadLocation());
            values.put(KEY_URL, download.getUrl());

            YoutubeDownloadInfo youtubeDownloadInfo = (YoutubeDownloadInfo)download;
            values.put(KEY_PARAM, youtubeDownloadInfo.getParam());
            values.put(KEY_VIDEO_ITAG, youtubeDownloadInfo.getItag());
            db.insert(TABLE_YOUTUBE_DOWNLOAD_LIST, null, values);
        }

        db.close();
        return downloadId;
    }

    public DownloadInfo getDownload(long downloadId) {
        int categoryId = getCategory(downloadId);
        return getDownload(categoryId, downloadId);
    }

    public DownloadInfo getDownload(int downloadCategory, long downloadId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String downloadQuery;
        Cursor downloadQueryCursor;
        switch(downloadCategory) {
            case DOWNLOAD_TYPE_BROWSER :
                downloadQuery = "SELECT * FROM " + TABLE_BROWSER_DOWNLOAD_LIST + " WHERE "
                        + KEY_VIDEO_ID + "=" + downloadId;
                downloadQueryCursor = db.rawQuery(downloadQuery, null);
                if (downloadQueryCursor.moveToFirst()) {
                    String url = downloadQueryCursor.getString(2);
                    String downloadPath = downloadQueryCursor.getString(3);
                    String title = downloadQueryCursor.getString(4);
                    DownloadInfo downloadInfo = new BrowserDownloadInfo(mContext, title, url, downloadPath);
                    downloadInfo.setDatabaseId(downloadId);
                    downloadQueryCursor.close();
                    return downloadInfo;
                }
                break;
            case DOWNLOAD_TYPE_YOUTUBE :
                downloadQuery = "SELECT * FROM " + TABLE_YOUTUBE_DOWNLOAD_LIST
                        + " WHERE " + KEY_VIDEO_ID + "=" + downloadId;
                downloadQueryCursor = db.rawQuery(downloadQuery, null);
                if (downloadQueryCursor.moveToFirst()) {
                    String param = downloadQueryCursor.getString(0);
                    int itag = downloadQueryCursor.getInt(2);
                    String url = downloadQueryCursor.getString(3);
                    String title = downloadQueryCursor.getString(4);
                    String downloadPath = downloadQueryCursor.getString(5);
                    DownloadInfo downloadInfo = new YoutubeDownloadInfo(mContext, title, url, downloadPath, param, itag);
                    downloadInfo.setDatabaseId(downloadId);
                    downloadQueryCursor.close();
                    return downloadInfo;
                }
                break;
        }

        return null;
    }

    public List<DownloadInfo> getAllDownloads() {
        List<DownloadInfo> downloadList = new ArrayList<>();
        downloadList.addAll(getDownloadsOfType(DOWNLOAD_TYPE_YOUTUBE));
        downloadList.addAll(getDownloadsOfType(DOWNLOAD_TYPE_BROWSER));
        return downloadList;
    }

    public List<DownloadInfo> getDownloadsOfType(int categoryType) {
        List<DownloadInfo> downloadInfos = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_VIDEO_DOWNLOAD_LIST
                + " WHERE " + KEY_TYPE + "=" + categoryType;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor downloadListCursor = db.rawQuery(selectQuery, null);

        if (downloadListCursor.moveToFirst()) {
            do {
                int downloadId = downloadListCursor.getInt(0);
                int downloadCategory = downloadListCursor.getInt(1);
                Log.e("getDownloadsOfType", " Download Category " + downloadCategory + " Id " + downloadId);
                DownloadInfo downloadInfo = getDownload(downloadCategory, downloadId);
                if (downloadInfo != null) {
                    downloadInfo.setDatabaseId(downloadId);
                    downloadInfos.add(downloadInfo);
                }
            } while (downloadListCursor.moveToNext());
        }

        downloadListCursor.close();
        return downloadInfos;
    }

    public int getDownloadCount() {
        String countQuery = "SELECT  * FROM " + TABLE_VIDEO_DOWNLOAD_LIST;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }

    public long downloadAlreadyExists(DownloadInfo download) {
        List<DownloadInfo> downloadInfos;
        if (download instanceof BrowserDownloadInfo) {
            downloadInfos = getDownloadsOfType(DOWNLOAD_TYPE_BROWSER);
            for(DownloadInfo downloadInfo : downloadInfos) {
                if (downloadInfo.getUrl().equals(download.getUrl())) {
                    return downloadInfo.getDatabaseId();
                }
            }
        } else if (download instanceof YoutubeDownloadInfo) {
            downloadInfos = getDownloadsOfType(DOWNLOAD_TYPE_YOUTUBE);
            for(DownloadInfo downloadInfo : downloadInfos) {
                if (((YoutubeDownloadInfo)downloadInfo).getParam().equals(((YoutubeDownloadInfo)download).getParam())) {
                    return downloadInfo.getDatabaseId();
                }
            }
        }

        return -1;
    }

    public long updateDownload(long id, DownloadInfo downloadInfo) {
        deleteDownload(id);
        return addDownload(downloadInfo);
    }

    public int getCategory(long downloadId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_VIDEO_DOWNLOAD_LIST
                + " WHERE " + KEY_ID + "=" + downloadId;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            int categoryId = cursor.getInt(1);
            cursor.close();
            return categoryId;
        }

        assert(false);
        return -1;
    }

    public void deleteDownload(long id) {
        int categoryId = getCategory(id);
        if (categoryId == -1) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        switch (categoryId) {
            case DOWNLOAD_TYPE_BROWSER :
                db.delete(TABLE_BROWSER_DOWNLOAD_LIST, KEY_VIDEO_ID + " = ?",
                        new String[] { String.valueOf(id) });
                break;
            case DOWNLOAD_TYPE_YOUTUBE :
                db.delete(TABLE_YOUTUBE_DOWNLOAD_LIST, KEY_VIDEO_ID + " = ?",
                        new String[] { String.valueOf(id) });
                break;
        }

        db.delete(TABLE_VIDEO_DOWNLOAD_LIST, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VIDEO_DOWNLOAD_LIST, null, null);
        db.delete(TABLE_BROWSER_DOWNLOAD_LIST, null, null);
        db.delete(TABLE_YOUTUBE_DOWNLOAD_LIST, null, null);
    }
}
