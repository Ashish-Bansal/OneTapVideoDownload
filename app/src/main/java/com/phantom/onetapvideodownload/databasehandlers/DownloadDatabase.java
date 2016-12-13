package com.phantom.onetapvideodownload.databasehandlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_VIDEO_ITAG = "itag";
    private static final String KEY_PARAM = "param";
    private static final String KEY_DOWNLOAD_LOCATION = "download_path";
    private static final String KEY_STATUS = "download_status";
    private static final String KEY_CONTENT_LENGTH = "content_length";
    private static final String KEY_DOWNLOADED_LENGTH = "downloaded_length";
    private static final String KEY_PACKAGE_NAME = "package_name";

    private static DownloadDatabase mDownloadDatabase;
    private Context mContext;
    private SQLiteDatabase mSQLiteDatabase;

    public static synchronized DownloadDatabase getDatabase(Context context) {
        if (mDownloadDatabase == null) {
            mDownloadDatabase = new DownloadDatabase(context);
            mDownloadDatabase.mContext = context;
            mDownloadDatabase.mSQLiteDatabase = mDownloadDatabase.getWritableDatabase();
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
                + KEY_FILENAME + " TEXT,"
                + KEY_STATUS + " INTEGER,"
                + KEY_CONTENT_LENGTH + " INTEGER,"
                + KEY_DOWNLOADED_LENGTH + " INTEGER,"
                + KEY_PACKAGE_NAME + " TEXT )";

        String youtubeDownloadListTable = "CREATE TABLE " + TABLE_YOUTUBE_DOWNLOAD_LIST + "("
                + KEY_PARAM + " TEXT, "
                + KEY_VIDEO_ID + " INTEGER,"
                + KEY_VIDEO_ITAG + " INTEGER,"
                + KEY_URL + " TEXT,"
                + KEY_FILENAME + " TEXT,"
                + KEY_DOWNLOAD_LOCATION + " TEXT,"
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_STATUS + " INTEGER,"
                + KEY_CONTENT_LENGTH + " INTEGER,"
                + KEY_DOWNLOADED_LENGTH + " INTEGER,"
                + KEY_PACKAGE_NAME + " TEXT )";

        db.execSQL(downloadListTable);
        db.execSQL(browserDownloadListTable);
        db.execSQL(youtubeDownloadListTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEO_DOWNLOAD_LIST);
        onCreate(db);
    }

    @Override
    public void finalize() throws Throwable{
        super.finalize();
        if (mDownloadDatabase != null) {
            mDownloadDatabase.close();
        }

        if (mSQLiteDatabase != null) {
            mSQLiteDatabase.close();
        }
    }

    public long addDownload(DownloadInfo download) {
        long downloadId = -1;
        if (download instanceof BrowserDownloadInfo) {
            ContentValues videoListValues = new ContentValues();
            videoListValues.put(KEY_TYPE, DOWNLOAD_TYPE_BROWSER);
            downloadId = mSQLiteDatabase.insert(TABLE_VIDEO_DOWNLOAD_LIST, null, videoListValues);
            assert(downloadId != -1);
            ContentValues values = getContentValuesForBrowserVideo(download, downloadId);
            mSQLiteDatabase.insert(TABLE_BROWSER_DOWNLOAD_LIST, null, values);
        } else if (download instanceof YoutubeDownloadInfo) {
            ContentValues downloadListValues = new ContentValues();
            downloadListValues.put(KEY_TYPE, DOWNLOAD_TYPE_YOUTUBE);
            mSQLiteDatabase.insert(TABLE_VIDEO_DOWNLOAD_LIST, null, downloadListValues);
            downloadId = mSQLiteDatabase.insert(TABLE_VIDEO_DOWNLOAD_LIST, null, downloadListValues);
            assert(downloadId != -1);
            ContentValues values = getContentValuesForYoutubeVideo(download, downloadId);
            mSQLiteDatabase.insert(TABLE_YOUTUBE_DOWNLOAD_LIST, null, values);
        }

        return downloadId;
    }

    public ContentValues getContentValuesForBrowserVideo(DownloadInfo downloadInfo, long downloadId) {
        ContentValues values = new ContentValues();
        values.put(KEY_FILENAME, downloadInfo.getFilename());
        values.put(KEY_VIDEO_ID, downloadId);
        values.put(KEY_DOWNLOAD_LOCATION, downloadInfo.getDownloadLocation());
        values.put(KEY_URL, downloadInfo.getUrl());
        values.put(KEY_STATUS, downloadInfo.getStatus().ordinal());
        values.put(KEY_CONTENT_LENGTH, downloadInfo.getContentLength());
        values.put(KEY_DOWNLOADED_LENGTH, downloadInfo.getDownloadedLength());
        values.put(KEY_PACKAGE_NAME, downloadInfo.getPackageName());
        return values;
    }

    public ContentValues getContentValuesForYoutubeVideo(DownloadInfo downloadInfo, long downloadId) {
        ContentValues values = new ContentValues();
        values.put(KEY_FILENAME, downloadInfo.getFilename());
        values.put(KEY_URL, downloadInfo.getUrl());
        values.put(KEY_VIDEO_ID, downloadId);
        values.put(KEY_DOWNLOAD_LOCATION, downloadInfo.getDownloadLocation());
        values.put(KEY_STATUS, downloadInfo.getStatus().ordinal());
        values.put(KEY_CONTENT_LENGTH, downloadInfo.getContentLength());
        values.put(KEY_DOWNLOADED_LENGTH, downloadInfo.getDownloadedLength());
        values.put(KEY_PACKAGE_NAME, downloadInfo.getPackageName());

        YoutubeDownloadInfo youtubeDownloadInfo = (YoutubeDownloadInfo)downloadInfo;
        values.put(KEY_PARAM, youtubeDownloadInfo.getParam());
        values.put(KEY_VIDEO_ITAG, youtubeDownloadInfo.getItag());
        return values;
    }

    public DownloadInfo getDownload(long downloadId) {
        int categoryId = getCategory(downloadId);
        return getDownload(categoryId, downloadId);
    }

    public DownloadInfo getDownload(int downloadCategory, long downloadId) {
        String downloadQuery;
        Cursor downloadQueryCursor = null;
        switch(downloadCategory) {
            case DOWNLOAD_TYPE_BROWSER :
                downloadQuery = "SELECT * FROM " + TABLE_BROWSER_DOWNLOAD_LIST + " WHERE "
                        + KEY_VIDEO_ID + "=" + downloadId;
                downloadQueryCursor = mSQLiteDatabase.rawQuery(downloadQuery, null);
                if (downloadQueryCursor.moveToFirst()) {
                    String url = downloadQueryCursor.getString(2);
                    String downloadPath = downloadQueryCursor.getString(3);
                    String title = downloadQueryCursor.getString(4);
                    Integer status = downloadQueryCursor.getInt(5);
                    Long contentLength = downloadQueryCursor.getLong(6);
                    Long downloadedLength = downloadQueryCursor.getLong(7);
                    String packageName = downloadQueryCursor.getString(8);

                    DownloadInfo downloadInfo = new BrowserDownloadInfo(mContext, title, url, downloadPath);
                    downloadInfo.setDatabaseId(downloadId);
                    downloadInfo.setStatus(DownloadInfo.Status.values()[status]);
                    downloadInfo.setContentLength(contentLength);
                    downloadInfo.setDownloadedLength(downloadedLength);
                    downloadInfo.setPackageName(packageName);
                    downloadQueryCursor.close();
                    return downloadInfo;
                }
                break;
            case DOWNLOAD_TYPE_YOUTUBE :
                downloadQuery = "SELECT * FROM " + TABLE_YOUTUBE_DOWNLOAD_LIST
                        + " WHERE " + KEY_VIDEO_ID + "=" + downloadId;
                downloadQueryCursor = mSQLiteDatabase.rawQuery(downloadQuery, null);
                if (downloadQueryCursor.moveToFirst()) {
                    String param = downloadQueryCursor.getString(0);
                    int itag = downloadQueryCursor.getInt(2);
                    String url = downloadQueryCursor.getString(3);
                    String title = downloadQueryCursor.getString(4);
                    String downloadPath = downloadQueryCursor.getString(5);
                    Integer status = downloadQueryCursor.getInt(7);
                    if (DownloadInfo.Status.Downloading == DownloadInfo.Status.values()[status]) {
                        status = DownloadInfo.Status.Stopped.ordinal();
                    }

                    Long contentLength = downloadQueryCursor.getLong(8);
                    Long downloadedLength = downloadQueryCursor.getLong(9);
                    String packageName = downloadQueryCursor.getString(10);

                    DownloadInfo downloadInfo = new YoutubeDownloadInfo(mContext, title, url, downloadPath, param, itag);
                    downloadInfo.setDatabaseId(downloadId);
                    downloadInfo.setStatus(DownloadInfo.Status.values()[status]);
                    downloadInfo.setContentLength(contentLength);
                    downloadInfo.setDownloadedLength(downloadedLength);
                    downloadInfo.setPackageName(packageName);
                    downloadQueryCursor.close();
                    return downloadInfo;
                }
                break;
        }

        if (downloadQueryCursor != null) {
            downloadQueryCursor.close();
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

        Cursor downloadListCursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if (downloadListCursor.moveToFirst()) {
            do {
                int downloadId = downloadListCursor.getInt(0);
                int downloadCategory = downloadListCursor.getInt(1);
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
        Cursor cursor = mSQLiteDatabase.rawQuery(countQuery, null);
        int downloadCount = cursor.getCount();
        cursor.close();

        return downloadCount;
    }

    public long updateDownload(long id, DownloadInfo downloadInfo) {
        if (downloadExistsById(id)) {
            if (downloadInfo instanceof BrowserDownloadInfo) {
                ContentValues values = getContentValuesForBrowserVideo(downloadInfo, id);
                mSQLiteDatabase.update(TABLE_BROWSER_DOWNLOAD_LIST, values, KEY_VIDEO_ID + "=" + id, null);
            } else if (downloadInfo instanceof YoutubeDownloadInfo) {
                ContentValues values = getContentValuesForYoutubeVideo(downloadInfo, id);
                mSQLiteDatabase.update(TABLE_YOUTUBE_DOWNLOAD_LIST, values, KEY_VIDEO_ID + "=" + id, null);
            }
        } else {
            return -1;
        }

        return id;
    }

    public boolean downloadExistsById(long id) {
        String selectQuery = "SELECT * FROM " + TABLE_VIDEO_DOWNLOAD_LIST
                + " WHERE " + KEY_ID + "=" + id;

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }

    public int getCategory(long downloadId) {
        String selectQuery = "SELECT * FROM " + TABLE_VIDEO_DOWNLOAD_LIST
                + " WHERE " + KEY_ID + "=" + downloadId;

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            int categoryId = cursor.getInt(1);
            cursor.close();
            return categoryId;
        }

        cursor.close();
        return -1;
    }

    public void deleteDownload(long id) {
        int categoryId = getCategory(id);
        if (categoryId == -1) {
            return;
        }

        mSQLiteDatabase.delete(TABLE_VIDEO_DOWNLOAD_LIST, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        switch (categoryId) {
            case DOWNLOAD_TYPE_BROWSER :
                mSQLiteDatabase.delete(TABLE_BROWSER_DOWNLOAD_LIST, KEY_VIDEO_ID + " = ?",
                        new String[] { String.valueOf(id) });
                break;
            case DOWNLOAD_TYPE_YOUTUBE :
                mSQLiteDatabase.delete(TABLE_YOUTUBE_DOWNLOAD_LIST, KEY_VIDEO_ID + " = ?",
                        new String[] { String.valueOf(id) });
                break;
        }

        mSQLiteDatabase.delete(TABLE_VIDEO_DOWNLOAD_LIST, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    public void clearDatabase() {
        mSQLiteDatabase.delete(TABLE_VIDEO_DOWNLOAD_LIST, null, null);
        mSQLiteDatabase.delete(TABLE_BROWSER_DOWNLOAD_LIST, null, null);
        mSQLiteDatabase.delete(TABLE_YOUTUBE_DOWNLOAD_LIST, null, null);
    }
}
