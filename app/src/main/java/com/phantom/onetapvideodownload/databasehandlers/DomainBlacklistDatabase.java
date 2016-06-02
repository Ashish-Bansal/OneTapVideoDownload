package com.phantom.onetapvideodownload.databasehandlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class DomainBlacklistDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DomainBlacklistDatabase";
    private static final String TABLE_DOMAIN_LIST = "blacklist_domain_list";
    private static final String KEY_ID = "id";
    private static final String KEY_URL = "url";

    private static DomainBlacklistDatabase mDomainBlacklistDatabase;

    public static DomainBlacklistDatabase getDatabase(Context context) {
        if (mDomainBlacklistDatabase == null) {
            mDomainBlacklistDatabase = new DomainBlacklistDatabase(context);
        }

        return mDomainBlacklistDatabase;
    }

    private DomainBlacklistDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String domainListTable = "CREATE TABLE " + TABLE_DOMAIN_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_URL + " TEXT)";

        db.execSQL(domainListTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOMAIN_LIST);
        onCreate(db);
    }

    public long addOrUpdateUrl(String oldUrl, String newUrl) {
        long id = alreadyExists(oldUrl);
        if (id == -1) {
            return addUrl(newUrl);
        } else {
            return updateUrl(id, newUrl);
        }
    }

    public long addUrl(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        long urlId;
        ContentValues urlValues = new ContentValues();
        urlValues.put(KEY_URL, url);
        urlId = db.insert(TABLE_DOMAIN_LIST, null, urlValues);
        db.close();
        return urlId;
    }

    public String getUrl(long urlId) {
        String selectQuery = "SELECT * FROM " + TABLE_DOMAIN_LIST
                + " WHERE " + KEY_ID + "=" + urlId;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor urlListCursor = db.rawQuery(selectQuery, null);
        if (urlListCursor.moveToFirst()) {
            String url = urlListCursor.getString(1);
            urlListCursor.close();
            return url;
        }
        urlListCursor.close();
        return null;
    }

    public List<Pair<Long, String>> getAllUrls() {
        List<Pair<Long, String>> urlList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_DOMAIN_LIST;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor urlListCursor = db.rawQuery(selectQuery, null);
        if (urlListCursor.moveToFirst()) {
            do {
                Long id = urlListCursor.getLong(0);
                String url = urlListCursor.getString(1);
                urlList.add(new Pair<>(id, url));
            } while (urlListCursor.moveToNext());
        }
        urlListCursor.close();
        return urlList;
    }

    public int getUrlCount() {
        String countQuery = "SELECT  * FROM " + TABLE_DOMAIN_LIST;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int urlCount = cursor.getCount();
        cursor.close();
        return urlCount;
    }

    public long alreadyExists(String url) {
        return getUrlId(url);
    }

    public long updateUrl(long id, String url) {
        deleteUrl(id);
        return addUrl(url);
    }

    public long updateUrl(String oldUrl, String newUrl) {
        long urlId = getUrlId(oldUrl);
        if (urlId != -1) {
            return updateUrl(urlId, newUrl);
        }
        return -1;
    }

    public void deleteUrl(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOMAIN_LIST, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    public long getUrlId(String url) {
        List<Pair<Long, String>> urlList = getAllUrls();
        for (Pair<Long, String> p: urlList) {
            if (p.second.equals(url)) {
                return p.first;
            }
        }
        return -1;
    }

    public void deleteUrl(String url) {
        long urlId = getUrlId(url);
        if (urlId != -1) {
            deleteUrl(urlId);
        }
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOMAIN_LIST, null, null);
    }
}
